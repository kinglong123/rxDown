package service.cn.com.rxdownload.entity;

import java.io.IOException;
import java.text.ParseException;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/3
 * Time: 09:44
 * Download Type
 */
public abstract class DownloadType {
    protected TemporaryRecord record;

    private DownloadType(TemporaryRecord record) {
        this.record = record;
    }

    public void prepareDownload() throws IOException, ParseException {
        System.out.println(prepareLog());
    }

    long downloadSize = 0;
//
//    public Observable<DownloadStatus> startDownload() {
//        return Flowable.just(1)
//                .doOnSubscribe(new Consumer<Subscription>() {
//                    @Override
//                    public void accept(Subscription subscription) throws Exception {
//                        System.out.println(startLog());
//                        record.start();
//                    }
//                })
//                .flatMap(new Function<Integer, Publisher<DownloadStatus>>() {
//                    @Override
//                    public Publisher<DownloadStatus> apply(Integer integer) throws Exception {
//                        return download();
//                    }
//                })
//                .observeOn(Schedulers.io())
//                .map(new Function<DownloadStatus, DownloadStatus>() {
//                    @Override
//                    public DownloadStatus apply(@NonNull DownloadStatus status) throws Exception {
//                        if (status.getDownloadSize() - downloadSize > 100000L) {
//                            System.out.println("Thread: " + Thread.currentThread().getName() + " update DB: " + status.getDownloadSize());
//                            downloadSize = status.getDownloadSize();
//                        }
//                        System.out.println("1111111111111111133333333333333");
//                        record.update(status);
//                        return status;
//                    }
//                })
//                .doOnError(new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        System.out.println(errorLog());
//                        record.error();
//                    }
//                })
//                .doOnComplete(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        System.out.println(completeLog());
//                        record.complete();
//                    }
//                })
//                .doOnCancel(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        System.out.println(cancelLog());
//                        record.cancel();
//                    }
//                })
//                .doFinally(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        System.out.println(finishLog());
//                        record.finish();
//                    }
//                })
//                .toObservable();
//    }
//
//    protected abstract Publisher<DownloadStatus> download();
//
    protected String prepareLog() {
        return "";
    }
//
//    protected String startLog() {
//        return "";
//    }
//
//    protected String completeLog() {
//        return "";
//    }
//
//    protected String errorLog() {
//        return "";
//    }
//
//    protected String cancelLog() {
//        return "";
//    }
//
//    protected String finishLog() {
//        return "";
//    }
//
    public static class NormalDownload extends DownloadType {
//
        public NormalDownload(TemporaryRecord record) {
            super(record);
        }
//
//        @Override
//        public void prepareDownload() throws IOException, ParseException {
//            super.prepareDownload();
//            record.prepareNormalDownload();
//        }
//
//        @Override
//        protected Publisher<DownloadStatus> download() {
//            return record.download()
//                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
//                        @Override
//                        public Publisher<DownloadStatus> apply(Response<ResponseBody> response) throws
//                                Exception {
//                            return save(response);
//                        }
//                    })
//                    .compose(Utils.<DownloadStatus>retry2(NORMAL_RETRY_HINT, record.getMaxRetryCount()));
//        }
//
//        @Override
//        protected String prepareLog() {
//            return NORMAL_DOWNLOAD_PREPARE;
//        }
//
//        @Override
//        protected String startLog() {
//            return NORMAL_DOWNLOAD_STARTED;
//        }
//
//        @Override
//        protected String completeLog() {
//            return NORMAL_DOWNLOAD_COMPLETED;
//        }
//
//        @Override
//        protected String errorLog() {
//            return NORMAL_DOWNLOAD_FAILED;
//        }
//
//        @Override
//        protected String cancelLog() {
//            return NORMAL_DOWNLOAD_CANCEL;
//        }
//
//        @Override
//        protected String finishLog() {
//            return NORMAL_DOWNLOAD_FINISH;
//        }
//
//        private Publisher<DownloadStatus> save(final Response<ResponseBody> response) {
//            return Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
//                @Override
//                public void subscribe(FlowableEmitter<DownloadStatus> e) throws Exception {
//                    record.save(e, response);
//                }
//            }, BackpressureStrategy.LATEST);
//        }
    }
//
//    public static class ContinueDownload extends DownloadType {
//
//        public ContinueDownload(TemporaryRecord record) {
//            super(record);
//        }
//
//        @Override
//        protected Publisher<DownloadStatus> download() {
//            List<Publisher<DownloadStatus>> tasks = new ArrayList<>();
//            for (int i = 0; i < record.getMaxThreads(); i++) {
//                tasks.add(rangeDownload(i));
//            }
//            return Flowable.mergeDelayError(tasks);
//        }
//
//        @Override
//        protected String prepareLog() {
//            return CONTINUE_DOWNLOAD_PREPARE;
//        }
//
//        @Override
//        protected String startLog() {
//            return CONTINUE_DOWNLOAD_STARTED;
//        }
//
//        @Override
//        protected String completeLog() {
//            return CONTINUE_DOWNLOAD_COMPLETED;
//        }
//
//        @Override
//        protected String errorLog() {
//            return CONTINUE_DOWNLOAD_FAILED;
//        }
//
//        @Override
//        protected String cancelLog() {
//            return CONTINUE_DOWNLOAD_CANCEL;
//        }
//
//        @Override
//        protected String finishLog() {
//            return CONTINUE_DOWNLOAD_FINISH;
//        }
//
//        /**
//         * 分段下载任务
//         *
//         * @param index 下载编号
//         * @return Observable
//         */
//        private Publisher<DownloadStatus> rangeDownload(final int index) {
//            return record.rangeDownload(index)
//                    .subscribeOn(Schedulers.io())  //Important!
//                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
//                        @Override
//                        public Publisher<DownloadStatus> apply(Response<ResponseBody> response) throws
//                                Exception {
//                            return save(index, response.body());
//                        }
//                    })
//                    .compose(Utils.<DownloadStatus>retry2(formatStr(RANGE_RETRY_HINT, index), record.getMaxRetryCount()));
//        }
//
//        /**
//         * 保存断点下载的文件,以及下载进度
//         *
//         * @param index    下载编号
//         * @param response 响应值
//         * @return Flowable
//         */
//        private Publisher<DownloadStatus> save(final int index, final ResponseBody response) {
//
//            Flowable<DownloadStatus> flowable = Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
//                @Override
//                public void subscribe(FlowableEmitter<DownloadStatus> emitter) throws Exception {
//                    record.save(emitter, index, response);
//                }
//            }, BackpressureStrategy.LATEST)
//                    .replay(1)
//                    .autoConnect();
//
//            return flowable.throttleFirst(100, TimeUnit.MILLISECONDS).mergeWith(flowable.takeLast(1))
//                    .subscribeOn(Schedulers.newThread());
//        }
//    }
//
//    public static class MultiThreadDownload extends ContinueDownload {
//
//        public MultiThreadDownload(TemporaryRecord record) {
//            super(record);
//        }
//
//        @Override
//        public void prepareDownload() throws IOException, ParseException {
//            super.prepareDownload();
//            record.prepareRangeDownload();
//        }
//
//        @Override
//        protected String prepareLog() {
//            return MULTITHREADING_DOWNLOAD_PREPARE;
//        }
//
//        @Override
//        protected String startLog() {
//            return MULTITHREADING_DOWNLOAD_STARTED;
//        }
//
//        @Override
//        protected String completeLog() {
//            return MULTITHREADING_DOWNLOAD_COMPLETED;
//        }
//
//        @Override
//        protected String errorLog() {
//            return MULTITHREADING_DOWNLOAD_FAILED;
//        }
//
//        @Override
//        protected String cancelLog() {
//            return MULTITHREADING_DOWNLOAD_CANCEL;
//        }
//
//        @Override
//        protected String finishLog() {
//            return MULTITHREADING_DOWNLOAD_FINISH;
//        }
//    }
//
//    public static class AlreadyDownloaded extends DownloadType {
//
//        public AlreadyDownloaded(TemporaryRecord record) {
//            super(record);
//        }
//
//        @Override
//        protected Publisher<DownloadStatus> download() {
//            return Flowable.just(new DownloadStatus(record.getContentLength(), record.getContentLength()));
//        }
//
//        @Override
//        protected String prepareLog() {
//            return ALREADY_DOWNLOAD_HINT;
//        }
//    }
}