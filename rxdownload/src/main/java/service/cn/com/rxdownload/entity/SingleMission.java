package service.cn.com.rxdownload.entity;

import java.util.Map;
import java.util.concurrent.Semaphore;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import service.cn.com.rxdownload.RxDownload;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.utils.Constant;

import static service.cn.com.rxdownload.entity.DownloadFlag.WAITING;
import static service.cn.com.rxdownload.function.DownloadEventFactory.completed;
import static service.cn.com.rxdownload.function.DownloadEventFactory.failed;
import static service.cn.com.rxdownload.function.DownloadEventFactory.paused;
import static service.cn.com.rxdownload.function.DownloadEventFactory.started;
import static service.cn.com.rxdownload.function.DownloadEventFactory.waiting;
import static service.cn.com.rxdownload.utils.Utils.createProcessor;
import static service.cn.com.rxdownload.utils.Utils.dispose;
import static service.cn.com.rxdownload.utils.Utils.formatStr;

/**
 * Created by lanjl on 2018/7/21.
 */
public class SingleMission extends DownloadMission {

    private DownloadBean bean;

    private String missionId;

    public SingleMission(RxDownload rxdownload, DownloadBean bean) {
        super(rxdownload);
        this.bean = bean;
    }



    @Override
    public String getUrl() {
        return bean.getUrl();
    }

    @Override
    public void init(Map<String, DownloadMission> missionMap,
            Map<String, FlowableProcessor<DownloadEvent>> processorMap) {
        DownloadMission mission = missionMap.get(getUrl());
        if (mission == null) {
            missionMap.put(getUrl(), this);
        } else {
            if (mission.isCanceled()) {
                missionMap.put(getUrl(), this);
            } else {
                throw new IllegalArgumentException(formatStr(Constant.DOWNLOAD_URL_EXISTS, getUrl()));
            }
        }
        this.processor = createProcessor(getUrl(), processorMap);

    }
    @Override
    public void insertOrUpdate(DataBaseHelper dataBaseHelper) {
        if (dataBaseHelper.recordNotExists(getUrl())) {
            dataBaseHelper.insertRecord(bean, WAITING, missionId);
        } else {
            dataBaseHelper.updateRecord(getUrl(), WAITING, missionId);
        }

    }
    private Observer<DownloadStatus> observer;
    protected Disposable disposable;
    protected DownloadStatus status;
    @Override
    public void start(final Semaphore semaphore) throws InterruptedException {
        if (isCanceled()) {
            return;
        }

        semaphore.acquire();

        if (isCanceled()) {
            semaphore.release();
            return;
        }

        disposable = rxdownload.download(bean)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (observer != null) {
                            observer.onSubscribe(disposable);
                        }
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("finally and release...");
                        setCanceled(true);
                        semaphore.release();
                    }
                })
                .subscribe(new Consumer<DownloadStatus>() {
                    @Override
                    public void accept(DownloadStatus value) throws Exception {
                        status = value;
                        processor.onNext(started(value));
                        if (observer != null) {
                            observer.onNext(value);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        processor.onNext(failed(status, throwable));
                        if (observer != null) {
                            observer.onError(throwable);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        processor.onNext(completed(status));
                        setCompleted(true);

                        if (observer != null) {
                            observer.onComplete();
                        }
                    }
                });
    }

    @Override
    public void pause(DataBaseHelper dataBaseHelper) {
        dispose(disposable);
        setCanceled(true);
        if (processor != null && !isCompleted()) {
            processor.onNext(paused(dataBaseHelper.readStatus(getUrl())));
        }
    }

    @Override
    public void delete(DataBaseHelper dataBaseHelper, boolean deleteFile) {

    }

    @Override
    public void sendWaitingEvent(DataBaseHelper dataBaseHelper) {
        processor.onNext(waiting(dataBaseHelper.readStatus(getUrl())));//是不是直接数据库读出？
    }
}
