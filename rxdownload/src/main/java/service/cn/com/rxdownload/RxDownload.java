package service.cn.com.rxdownload;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import service.cn.com.rxdownload.entity.DownloadBean;
import service.cn.com.rxdownload.entity.DownloadEvent;
import service.cn.com.rxdownload.entity.DownloadStatus;
import service.cn.com.rxdownload.entity.SingleMission;
import service.cn.com.rxdownload.function.DownloadHelper;
import service.cn.com.rxdownload.function.DownloadService;

/**
 * Created by lanjl on 2018/7/13.
 */
public class RxDownload {

    private volatile static RxDownload instance;
    private volatile static boolean bound = false;//是否已经绑定了
    private Context context;
    private DownloadHelper downloadHelper;
    private Semaphore semaphore;
    private static final Object object = new Object();



    private int maxDownloadNumber = 5;

    private DownloadService downloadService;
    private RxDownload(Context context) {
        this.context = context.getApplicationContext();
        downloadHelper = new DownloadHelper(context);
        semaphore = new Semaphore(1);

    }
    static {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof InterruptedException) {

                } else if (throwable instanceof InterruptedIOException) {

                } else if (throwable instanceof SocketException) {

                }
                throwable.printStackTrace();
            }
        });
    }

    /**
     * Return RxDownload Instance
     *
     * @param context context
     * @return RxDownload
     */
    public static RxDownload getInstance(Context context) {
        if (instance == null) {
            synchronized (RxDownload.class) {
                if (instance == null) {
                    instance = new RxDownload(context);
                }
            }
        }
        return instance;
    }


    /**
     * Normal download.
     * <p>
     * You can construct a DownloadBean to save extra data to the database.
     *
     * @param downloadBean download bean.
     * @return Observable<DownloadStatus>
     */
    public Observable<DownloadStatus> download(DownloadBean downloadBean) {
        return downloadHelper.downloadDispatcher(downloadBean);
    }



    /**
     * Using Service to download.
     *
     * @param bean download bean
     * @return Observable<DownloadStatus>
     */
    public Observable<?> serviceDownload(final DownloadBean bean) {
        return createGeneralObservable(new GeneralObservableCallback() {
            @Override
            public void call() throws InterruptedException {
                downloadService.addDownloadMission(new SingleMission(RxDownload.this, bean));
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * return general observable
     *
     * @param callback Called when observable created.
     * @return Observable
     */
    private Observable<?> createGeneralObservable(final GeneralObservableCallback callback) {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(final ObservableEmitter<Object> emitter) throws Exception {
                if (!bound) {
                    semaphore.acquire();
                    if (!bound) {
                        startBindServiceAndDo(new ServiceConnectedCallback() {
                            @Override
                            public void call() {
                                doCall(callback, emitter);
                                semaphore.release();
                            }
                        });
                    } else {
                        doCall(callback, emitter);
                        semaphore.release();
                    }
                } else {
                    doCall(callback, emitter);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private void doCall(GeneralObservableCallback callback, ObservableEmitter<Object> emitter) {
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }
        emitter.onNext(object);
        emitter.onComplete();
    }

    /**
     * start and bind service.
     *
     * @param callback Called when service connected.
     */
    private void startBindServiceAndDo(final ServiceConnectedCallback callback) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_KEY, maxDownloadNumber);
        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadService.DownloadBinder downloadBinder
                        = (DownloadService.DownloadBinder) binder;
                downloadService = downloadBinder.getService();
                context.unbindService(this);//为什么要解除绑定呢
                bound = true;
                callback.call();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //注意!!这个方法只会在系统杀掉Service时才会调用!!
                bound = false;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public Observable<DownloadEvent> receiveDownloadStatus(final String url) {
        return createGeneralObservable(null)
                .flatMap(new Function<Object, ObservableSource<DownloadEvent>>() {
                    @Override
                    public ObservableSource<DownloadEvent> apply(Object o) throws Exception {
                        return downloadService.receiveDownloadEvent(url).toObservable();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Pause download.
     * <p>
     * Pause a download.
     *
     * @param url url
     */
    public Observable<?> pauseServiceDownload(final String url) {
        return createGeneralObservable(new GeneralObservableCallback() {
            @Override
            public void call() {
                downloadService.pauseDownload(url);
            }
        }).observeOn(AndroidSchedulers.mainThread());

    }

    private interface GeneralObservableCallback {
        void call() throws Exception;
    }
    private interface ServiceConnectedCallback {
        void call();
    }
}
