package service.cn.com.rxdownload.function;

import org.reactivestreams.Publisher;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.entity.DownloadBean;
import service.cn.com.rxdownload.entity.DownloadStatus;
import service.cn.com.rxdownload.entity.DownloadType;
import service.cn.com.rxdownload.entity.TemporaryRecord;
import service.cn.com.rxdownload.utils.Utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static service.cn.com.rxdownload.utils.Constant.DOWNLOAD_URL_EXISTS;
import static service.cn.com.rxdownload.utils.Constant.NORMAL_RETRY_HINT;
import static service.cn.com.rxdownload.utils.Constant.REQUEST_RETRY_HINT;
import static service.cn.com.rxdownload.utils.Constant.TEST_RANGE_SUPPORT;
import static service.cn.com.rxdownload.utils.Constant.URL_ILLEGAL;
import static service.cn.com.rxdownload.utils.Utils.formatStr;
import static service.cn.com.rxdownload.utils.Utils.retry;

/**
 * 下载的帮助，下载的api，下载文件的记录，数控等
 * Created by lanjl on 2018/7/13.
 */
public class DownloadHelper {
    private int maxRetryCount = 3;
    private int maxThreads = 3;

    private DownloadApi downloadApi;


    private String defaultSavePath;//默认的下载路径

    private TemporaryRecordTableList recordTable;

    private DataBaseHelper dataBaseHelper;//数据库帮助

    public DownloadHelper(Context context) {
        downloadApi = DownApiRetrofitProvider.INSTANCE.getInstance().create(DownloadApi.class);

        defaultSavePath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        recordTable = new TemporaryRecordTableList();
        dataBaseHelper = DataBaseHelper.getSingleton(context.getApplicationContext());
    }


    /**
     * dispatch download
     *
     * @param bean download bean
     * @return DownloadStatus
     */
    public Observable<DownloadStatus> downloadDispatcher(final DownloadBean bean) {
        return Observable.just(1)
                .observeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        addTempRecord(bean);
                    }
                })
//                .observeOn(Schedulers.io())
                .flatMap(new Function<Integer, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Integer integer) throws Exception {
                        return getDownloadType(bean.getUrl());
                    }
                })
                .flatMap(new Function<DownloadType, ObservableSource<DownloadStatus>>() {
                    @Override
                    public ObservableSource<DownloadStatus> apply(DownloadType type) throws Exception {
                        return download(type);
                    }
                })
//                .flatMap(new Function<DownloadType, ObservableSource<DownloadStatus>>() {
//                    @Override
//                    public ObservableSource<DownloadStatus> apply(DownloadType type) throws Exception {
//
//                        return download(bean);
//                    }
//                })

                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //logError(throwable);

                        throwable.printStackTrace();
//                        System.out.println("s:"+throwable.printStackTrace(););
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        recordTable.delete(bean.getUrl());
                    }
                })
                ;
    }
    /**
     * get download type. 确认下载类型，和一些准备工作，比如数据库保存
     *
     * @param url url
     * @return download type
     */
    private Observable<DownloadType> getDownloadType(final String url) {
        return Observable.just(1)
                .flatMap(new Function<Integer, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(Integer integer)
                            throws Exception {
                        return checkUrl(url);//确认大小
                    }
                })
                .flatMap(new Function<Object, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(Object o) throws Exception {
                        return checkRange(url);//确认支持支持分块
                    }
                })
                .doOnNext(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        recordTable.init(url, maxThreads, maxRetryCount, defaultSavePath,
                                downloadApi, dataBaseHelper);//初始化给个下载任务的细节 记录在TemporaryRecord
                    }
                })
                .flatMap(new Function<Object, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Object o) throws Exception {


                        //重点来了
                        return recordTable.fileExists(url) ? existsType(url) : nonExistsType(url);
//                        return Observable.just(recordTable.generateFileExistsType(url));
//                        new DownloadType.NormalDownload(map.get(url));
                    }
                })

                ;
    }


    /**
     * Gets the download type of file non-existence.
     *
     * @param url file url
     * @return Download Type
     */
    private Observable<DownloadType> nonExistsType(final String url) {
        return Observable.just(1)
                .flatMap(new Function<Integer, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Integer integer)
                            throws Exception {
                        return Observable.just(recordTable.generateNonExistsType(url));
                    }
                });
    }
    /**
     * Gets the download type of file existence.
     *
     * @param url file url
     * @return Download Type
     */
    private Observable<DownloadType> existsType(final String url) {
        return Observable.just(1)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return recordTable.readLastModify(url);//更新时间
                    }
                })
                .flatMap(new Function<String, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(String s) throws Exception {
                        return checkFile(url, s);//服务端是否改变过
                    }
                })
                .flatMap(new Function<Object, ObservableSource<DownloadType>>() {
                    @Override
                    public ObservableSource<DownloadType> apply(Object o)
                            throws Exception {
                        return Observable.just(recordTable.generateFileExistsType(url));
                    }
                });
    }
    /**
     * http checkRangeByHead request,checkRange need info, check whether if server file has changed.
     *
     * @param url url
     * @return empty Observable
     */
    private ObservableSource<Object> checkFile(final String url, String lastModify) {
        return downloadApi.checkFileByHead(lastModify, url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        recordTable.saveFileState(url, response);
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, maxRetryCount));
    }

    /**
     * http checkRangeByHead request,checkRange need info.
     *
     * @param url url
     * @return empty Observable
     */
    private ObservableSource<Object> checkRange(final String url) {
        return downloadApi.checkRangeByHead(TEST_RANGE_SUPPORT, url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        recordTable.saveRangeInfo(url, response);
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, maxRetryCount));
    }


    /**
     * check url
     *
     * @param url url
     * @return empty
     */
    private ObservableSource<Object> checkUrl(final String url) {
        return downloadApi.check(url)
                .flatMap(new Function<Response<Void>, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(@NonNull Response<Void> resp)
                            throws Exception {
                        if (!resp.isSuccessful()) {
                            return checkUrlByGet(url);
                        } else {
                            recordTable.saveFileInfo(url, resp);
                            return Observable.just(new Object());
                        }
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, maxRetryCount));
    }

    private ObservableSource<Object> checkUrlByGet(final String url) {
        return downloadApi.checkByGet(url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        if (!response.isSuccessful()) {
                            throw new IllegalArgumentException(formatStr(URL_ILLEGAL, url));
                        } else {
                            recordTable.saveFileInfo(url, response);
                        }
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, maxRetryCount));
    }

    /**
     * Add a temporary record to the record recordTable.
     * 将临时记录添加到记录记录表中。
     * @param bean download bean
     */
    private void addTempRecord(DownloadBean bean) {
        if (recordTable.contain(bean.getUrl())) {
            throw new IllegalArgumentException(formatStr(DOWNLOAD_URL_EXISTS, bean.getUrl()));
        }
        recordTable.add(bean.getUrl(), new TemporaryRecord(bean));//把url加到下载列表中
    }






    private ObservableSource<DownloadStatus> download(DownloadType downloadType)
            throws IOException, ParseException {
//        downloadType.prepareDownload();
//        return Observable.just(1).flatMap(new Function<Integer, ObservableSource<DownloadStatus>>() {
//            @Override
//            public ObservableSource<DownloadStatus> apply(Integer integer) throws Exception {
//                return startDownload(bean);
//            }
//        });


        //每种都去下载
//        recordTable.getMap().get(bean.getUrl()).prepareRangeDownload();//创建文件

        //这边判断下师傅文件以及下载了的如果以及下载的，不需求创建文件了，继续下载

        downloadType.prepareDownload();//普通下载和支持分块下载的情况不同
        return downloadType.startDownload();
    }

//    private ObservableSource<DownloadStatus> download(final DownloadBean bean)
//            throws IOException, ParseException {
////        downloadType.prepareDownload();
////        return Observable.just(1).flatMap(new Function<Integer, ObservableSource<DownloadStatus>>() {
////            @Override
////            public ObservableSource<DownloadStatus> apply(Integer integer) throws Exception {
////                return startDownload(bean);
////            }
////        });
//
//
//        //每种都去下载
//        recordTable.getMap().get(bean.getUrl()).prepareRangeDownload();//创建文件
//
//        //这边判断下师傅文件以及下载了的如果以及下载的，不需求创建文件了，继续下载
//
//        return startDownload(bean);
//    }


//    public Observable<DownloadStatus> startDownload(final DownloadBean bean) {
//        return Flowable.just(1)
//                .doOnSubscribe(new Consumer<Subscription>() {
//                    @Override
//                    public void accept(Subscription subscription) throws Exception {
////                        log(startLog());
////                        record.start();
//                    }
//                })
//                .flatMap(new Function<Integer, Publisher<DownloadStatus>>() {
//                    @Override
//                    public Publisher<DownloadStatus> apply(Integer integer) throws Exception {
//                        System.out.println("call1111111111111111:"+Thread.currentThread().getName());
//                        return downloadFile(bean);
//                    }
//                })
//                .observeOn(Schedulers.io())
//                .map(new Function<DownloadStatus, DownloadStatus>() {
//                    @Override
//                    public DownloadStatus apply(@NonNull DownloadStatus status) throws Exception {
////                        System.out.println("1111111111111111133333333333333");
//                        return status;
//                    }
//                })
//
//                .doOnError(new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        throwable.printStackTrace();
//                    }
//                })
//                .doOnComplete(new Action() {
//                    @Override
//                    public void run() throws Exception {
//
//                    }
//                })
//                .doOnCancel(new Action() {
//                    @Override
//                    public void run() throws Exception {
//
//                    }
//                })
//                .doFinally(new Action() {
//                    @Override
//                    public void run() throws Exception {
//
//                    }
//                })
//                .toObservable();
//    }

    protected Publisher<DownloadStatus> downloadFile(final DownloadBean bean) {
        return downloadApi.download(null, bean.getUrl())
//                .subscribeOn(Schedulers.io())  //Important!
                .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
                    @Override
                    public Publisher<DownloadStatus> apply(Response<ResponseBody> response) throws Exception {
                        System.out.println("11111111111111downloadFile");
                        return save(bean,response);
                    }
                })
                .compose(Utils.<DownloadStatus>retry2(NORMAL_RETRY_HINT, 3));

    }
    private Publisher<DownloadStatus> save(final DownloadBean bean,final Response<ResponseBody> response) {
        return Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
            @Override
            public void subscribe(FlowableEmitter<DownloadStatus> e) throws Exception {
                recordTable.getMap().get(bean.getUrl()).save(e, response);
//                fileHelper.saveFile(e, file(), response);
            }
        }, BackpressureStrategy.LATEST);
    }
    public File file() {
        return new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()+"/sas");
    }

}
