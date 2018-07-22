package service.cn.com.rxdownload.function;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.db.DownTableData;
import service.cn.com.rxdownload.entity.DownloadEvent;
import service.cn.com.rxdownload.entity.DownloadMission;
import service.cn.com.rxdownload.entity.DownloadStatus;
import service.cn.com.rxdownload.entity.SingleMission;
import service.cn.com.rxdownload.utils.Constant;

import static service.cn.com.rxdownload.function.DownloadEventFactory.createEvent;
import static service.cn.com.rxdownload.function.DownloadEventFactory.normal;
import static service.cn.com.rxdownload.utils.Constant.WAITING_FOR_MISSION_COME;
import static service.cn.com.rxdownload.utils.Utils.createProcessor;
import static service.cn.com.rxdownload.utils.Utils.dispose;
import static service.cn.com.rxdownload.utils.Utils.getFiles;

/**
 * Created by lanjl on 2018/7/21.
 */
public class DownloadService  extends Service {
    private DownloadBinder mBinder;
    private Disposable disposable;
    public static final String INTENT_KEY = "zlc_season_rxdownload_max_download_number";
    private BlockingQueue<DownloadMission> downloadQueue;

    private Semaphore semaphore;
    private Map<String, DownloadMission> missionMap;
    private Map<String, FlowableProcessor<DownloadEvent>> processorMap;

    private DataBaseHelper dataBaseHelper;
    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new DownloadBinder();
        downloadQueue = new LinkedBlockingQueue<>();
        processorMap = new ConcurrentHashMap<>();
        missionMap = new ConcurrentHashMap<>();

        dataBaseHelper = DataBaseHelper.getSingleton(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("start Download Service");
        System.out.println("111111111111111onStartCommand");
        dataBaseHelper.repairErrorFlag();//启动时把开始状态的设置未暂停
        if (intent != null) {
            int maxDownloadNumber = intent.getIntExtra(INTENT_KEY, 5);
            semaphore = new Semaphore(maxDownloadNumber);
        }
        return super.onStartCommand(intent, flags, startId);
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("bind Download Service");
        startDispatch();
        return mBinder;
    }
    /**
     * start dispatch download queue.
     */
    private void startDispatch() {
        disposable = Observable
                .create(new ObservableOnSubscribe<DownloadMission>() {
                    @Override
                    public void subscribe(ObservableEmitter<DownloadMission> emitter) throws Exception {
                        DownloadMission mission;
                        while (!emitter.isDisposed()) {
                            try {
                                System.out.println(WAITING_FOR_MISSION_COME);
                                System.out.println("11111111111");
                                mission = downloadQueue.take();
                                System.out.println("11111111111222222");
                                System.out.println(Constant.MISSION_COMING);
                            } catch (InterruptedException e) {
                                System.out.println("Interrupt blocking queue.");
                                continue;
                            }
                            emitter.onNext(mission);
                        }
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<DownloadMission>() {
                    @Override
                    public void accept(DownloadMission mission) throws Exception {
                        mission.start(semaphore);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println(throwable);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("destroy Download Service");
        destroy();

    }
    /**
     * Call when service is onDestroy.
     */
    private void destroy() {
        dispose(disposable);
        for (DownloadMission each : missionMap.values()) {
            each.pause(dataBaseHelper);
        }
        downloadQueue.clear();
    }



    /**
     * Add this mission into download queue.
     *
     * @param mission mission
     * @throws InterruptedException Blocking queue
     */
    public void addDownloadMission(DownloadMission mission) throws InterruptedException {
        mission.init(missionMap, processorMap);
        mission.insertOrUpdate(dataBaseHelper);
        mission.sendWaitingEvent(dataBaseHelper);

        downloadQueue.put(mission);
    }

    /**
     * Pause download.
     * <p>
     * Pause a url or all tasks belonging to missionId.
     *
     * @param url url or missionId
     */
    public void pauseDownload(String url) {
        DownloadMission mission = missionMap.get(url);
        if (mission != null && mission instanceof SingleMission) {
            mission.pause(dataBaseHelper);
        }
    }




    /**
     * Receive the url download event.
     * @param url url
     * @return DownloadEvent
     */
    public FlowableProcessor<DownloadEvent> receiveDownloadEvent(String url) {
        FlowableProcessor<DownloadEvent> processor = createProcessor(url, processorMap);
        DownloadMission mission = missionMap.get(url);
        if (mission == null) {  //Not yet add this url mission.
            DownTableData record = dataBaseHelper.readSingleRecord(url);
            System.out.println("11111111111111:record");
            if (record == null) {
                processor.onNext(normal(null));
            } else {
                File file = getFiles(record.getSaveName(), record.getSavePath())[0];
                if (file.exists()) {
                    processor.onNext(createEvent((int) record.getDownloadFlag(),
                            new DownloadStatus(record.isChunked(), record.getDownloadSize(), record.getTotalSize())));
                } else {
                    processor.onNext(normal(null));
                }
            }
        }
        System.out.println("11111111111111:FlowableProcessor");
        return processor;
    }




}
