package service.cn.com.rxdownload.entity;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.function.DownloadApi;
import service.cn.com.rxdownload.function.FileHelper;

import static android.text.TextUtils.concat;
import static java.io.File.separator;
import static service.cn.com.rxdownload.utils.Constant.CACHE;
import static service.cn.com.rxdownload.utils.Constant.RANGE_DOWNLOAD_STARTED;
import static service.cn.com.rxdownload.utils.Utils.empty;
import static service.cn.com.rxdownload.utils.Utils.getPaths;
import static service.cn.com.rxdownload.utils.Utils.mkdirs;


/**
 * 每个下载对用的信息都保存在整理，包括大小啊，路径啊。并通过fileHelper去保存文件 通过
 * 通过DataBaseHelper记录数据库
 * 通过downloadApi去下载
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2017/2/4
 * FIXME
 */
public class TemporaryRecord {
    private DownloadBean bean;

    private String filePath;
    private String tempPath;
    private String lmfPath;

    private int maxRetryCount;
    private int maxThreads;

    private long contentLength;
    private String lastModify;

    private boolean rangeSupport = false;
    private boolean serverFileChanged = false;

    private DataBaseHelper dataBaseHelper;
    private FileHelper fileHelper;//文件的操作，都是把文件传进去，大小问题
    private DownloadApi downloadApi;

    public TemporaryRecord(DownloadBean bean) {
        this.bean = bean;
    }

    /**
     * init needs info
     *
     * @param maxThreads      Max download threads
     * @param maxRetryCount   Max retry times
     * @param defaultSavePath Default save path;
     * @param downloadApi     API
     * @param dataBaseHelper  DataBaseHelper
     */
    public void init(int maxThreads, int maxRetryCount, String defaultSavePath,
                     DownloadApi downloadApi, DataBaseHelper dataBaseHelper) {
        this.maxThreads = maxThreads;
        this.maxRetryCount = maxRetryCount;
        this.downloadApi = downloadApi;
        this.dataBaseHelper = dataBaseHelper;
        this.fileHelper = new FileHelper(maxThreads);

        String realSavePath;
        if (empty(bean.getSavePath())) {
            realSavePath = defaultSavePath;
            bean.setSavePath(defaultSavePath);
        } else {
            realSavePath = bean.getSavePath();
        }
        String cachePath = concat(realSavePath, separator, CACHE).toString();
        mkdirs(realSavePath, cachePath);

        String[] paths = getPaths(bean.getSaveName(), realSavePath);
        filePath = paths[0];
        tempPath = paths[1];
        lmfPath = paths[2];
    }


    /**
     * prepare normal download, create files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareNormalDownload() throws IOException, ParseException {
        fileHelper.prepareDownload(lastModifyFile(), file(), contentLength, lastModify);
    }

    /**
     * prepare range download, create necessary files and save last-modify.
     *
     * @throws IOException
     * @throws ParseException
     */
    public void prepareRangeDownload() throws IOException, ParseException {
        fileHelper.prepareDownload(lastModifyFile(), tempFile(), file(), contentLength, lastModify);
    }
    public File file() {
        return new File(filePath);
    }

    public File lastModifyFile() {
        return new File(lmfPath);
    }
    public File tempFile() {
        return new File(tempPath);
    }
    public String getSaveName() {
        return bean.getSaveName();
    }
    public void setSaveName(String saveName) {
        bean.setSaveName(saveName);
    }
    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public void setRangeSupport(boolean rangeSupport) {
        this.rangeSupport = rangeSupport;
    }

    /**
     * Normal download save.
     *
     * @param e        emitter
     * @param response response
     */
    public void save(FlowableEmitter<DownloadStatus> e, Response<ResponseBody> response) throws Exception{
        fileHelper.saveFile(e, file(), response);
    }

    /**
     * Range download save
     *
     * @param emitter  emitter
     * @param index    download index
     * @param response response
     * @throws IOException
     */
    public void save(FlowableEmitter<DownloadStatus> emitter, int index, ResponseBody response)
            throws IOException {
        fileHelper.saveFile(emitter, index, tempFile(), file(), response);
    }

    public String readLastModify() throws IOException {
        return fileHelper.readLastModify(lastModifyFile());
    }

    public void setFileChanged(boolean serverFileChanged) {
        this.serverFileChanged = serverFileChanged;
    }
    public boolean isFileChanged() {
        return serverFileChanged;
    }
    public boolean isSupportRange() {
        return rangeSupport;
    }

    public boolean tempFileDamaged() throws IOException {
        return fileHelper.tempFileDamaged(tempFile(), contentLength);
    }
    /**
     * Normal download request.
     *
     * @return response
     */
    public Flowable<Response<ResponseBody>> download() {
        return downloadApi.download(null, bean.getUrl());
    }
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
    public int getMaxThreads() {
        return maxThreads;
    }
    /**
     * Range download request
     *
     * @param index download index
     * @return response
     */
    public Flowable<Response<ResponseBody>> rangeDownload(final int index) {
        return Flowable
                .create(new FlowableOnSubscribe<DownloadRange>() {
                    @Override
                    public void subscribe(FlowableEmitter<DownloadRange> e) throws Exception {
                        DownloadRange range = readDownloadRange(index);
                        if (range.legal()) {
                            e.onNext(range);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .flatMap(new Function<DownloadRange, Publisher<Response<ResponseBody>>>() {
                    @Override
                    public Publisher<Response<ResponseBody>> apply(DownloadRange range)
                            throws Exception {
                        System.out.println("Thread: " + Thread.currentThread().getName() + "; " + RANGE_DOWNLOAD_STARTED + "; " + index + "; " + range.start + "; " + range.end);
                        String rangeStr = "bytes=" + range.start + "-" + range.end;
                        return downloadApi.download(rangeStr, bean.getUrl());
                    }
                });
    }

    /**
     * Read download range from record file.
     *
     * @param index index
     * @return
     * @throws IOException
     */
    public DownloadRange readDownloadRange(int index) throws IOException {
        return fileHelper.readDownloadRange(tempFile(), index);
    }

    public boolean fileNotComplete() throws IOException {
        return fileHelper.fileNotComplete(tempFile());
    }
    public boolean fileComplete() {
        return file().length() == contentLength;
    }
}
