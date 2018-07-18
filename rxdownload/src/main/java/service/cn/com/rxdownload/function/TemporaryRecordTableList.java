package service.cn.com.rxdownload.function;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.entity.DownloadType;
import service.cn.com.rxdownload.entity.TemporaryRecord;

import static service.cn.com.rxdownload.utils.Constant.DOWNLOAD_RECORD_FILE_DAMAGED;
import static service.cn.com.rxdownload.utils.Utils.contentLength;
import static service.cn.com.rxdownload.utils.Utils.empty;
import static service.cn.com.rxdownload.utils.Utils.fileName;
import static service.cn.com.rxdownload.utils.Utils.lastModify;
import static service.cn.com.rxdownload.utils.Utils.notSupportRange;



/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2017/2/4
 * FIXME
 */
public class TemporaryRecordTableList {
    private Map<String, TemporaryRecord> map;

    public TemporaryRecordTableList() {
        this.map = new HashMap<>();
    }

    public Map<String, TemporaryRecord> getMap() {
        return map;
    }

    public void add(String url, TemporaryRecord record) {
        map.put(url, record);
    }

    public boolean contain(String url) {
        return map.get(url) != null;
    }

    public void delete(String url) {
        map.remove(url);
    }

    /**
     * Save file info
     *
     * @param url      key
     * @param response response
     */
    public void saveFileInfo(String url, Response<?> response) {
        TemporaryRecord record = map.get(url);
        if (empty(record.getSaveName())) {
            record.setSaveName(fileName(url, response));
        }
        record.setContentLength(contentLength(response));
        record.setLastModify(lastModify(response));
    }

    /**
     * Save range info
     *
     * @param url      key
     * @param response response
     */
    public void saveRangeInfo(String url, Response<?> response) {
        map.get(url).setRangeSupport(!notSupportRange(response));
    }

    /**
     * Init necessary info
     *
     * @param url             url
     * @param maxThreads      max threads
     * @param maxRetryCount   retry count
     * @param defaultSavePath default save path
     * @param downloadApi     api
     * @param dataBaseHelper  DataBaseHelper
     */
    public void init(String url, int maxThreads, int maxRetryCount, String defaultSavePath,
                     DownloadApi downloadApi, DataBaseHelper dataBaseHelper) {
        map.get(url).init(maxThreads, maxRetryCount, defaultSavePath, downloadApi, dataBaseHelper);
    }




    /**
     * return file exists download type
     *
     * @param url key
     * @return download type
     */
    public DownloadType generateFileExistsType(String url) {
        DownloadType type;
        if (fileChanged(url)) {//相当于不存在时的逻辑
            type = getNormalType(url);
        } else {
            type = getServerFileChangeType(url);
        }
        return type;
    }

    /**
     * return file not exists download type.
     *
     * @param url key
     * @return download type
     */
    public DownloadType generateNonExistsType(String url) {
        return getNormalType(url);
    }


    private DownloadType getServerFileChangeType(String url) {
        if (supportRange(url)) {
            return supportRangeType(url);
        } else {
            return notSupportRangeType(url);//不支持分块
        }
    }
    private DownloadType notSupportRangeType(String url) {
        try {
            if (normalDownloadNotComplete(url)) {//是否下载完了 不支持分页的NormalDownload这样判断是有问题的，因为第二次开始，判断都是ok的。
                // no不支持分页的，判断是不是下载完了，也应该有一个tem记录
                return new DownloadType.NormalDownload(map.get(url));
            } else {
                return new DownloadType.AlreadyDownloaded(map.get(url));
            }
        } catch (IOException e) {
            return new DownloadType.NormalDownload(map.get(url));
        }
    }
    private boolean normalDownloadNotComplete(String url)  throws IOException{
        return !map.get(url).fileComplete();
    }

    private DownloadType supportRangeType(String url) {
        if (needReDownload(url)) {//temp 不存在，或者大小变了，则重新下载
            return new DownloadType.MultiThreadDownload(map.get(url));
        }
        try {
            if (multiDownloadNotComplete(url)) {//temp 存在，没下载完，继续下载
                return new DownloadType.ContinueDownload(map.get(url));
            }
        } catch (IOException e) {
            return new DownloadType.MultiThreadDownload(map.get(url));
        }
        return new DownloadType.AlreadyDownloaded(map.get(url));
    }


    private boolean multiDownloadNotComplete(String url) throws IOException {
        return map.get(url).fileNotComplete();
    }

    private boolean needReDownload(String url) {
        return tempFileNotExists(url) || tempFileDamaged(url);
    }

    private boolean tempFileDamaged(String url) {
        try {
            return map.get(url).tempFileDamaged();
        } catch (IOException e) {
            System.out.println(DOWNLOAD_RECORD_FILE_DAMAGED);
            return true;
        }
    }

    private boolean tempFileNotExists(String url) {
        return !map.get(url).tempFile().exists();
    }




    private boolean supportRange(String url) {
        return map.get(url).isSupportRange();
    }

    private boolean fileChanged(String url) {
        return map.get(url).isFileChanged();
    }

    private DownloadType getNormalType(String url) {
        DownloadType type;
//        if (supportRange(url)) {
//            type = new DownloadType.MultiThreadDownload(map.get(url));
//        } else {
            type = new DownloadType.NormalDownload(map.get(url));
//        }
        return type;
    }


    public boolean fileExists(String url) {
        return map.get(url).file().exists();
    }


    /**
     * read last modify string
     *
     * @param url key
     * @return last modify
     */
    public String readLastModify(String url) {
        try {
            return map.get(url).readLastModify();
        } catch (IOException e) {
            //TODO log
            //If read failed,return an empty string.
            //If we send empty last-modify,server will response 200.
            //That means file changed.
            return "";
        }
    }
    /**
     * Save file state, change or not change.
     *
     * @param url      key
     * @param response response
     */
    public void saveFileState(String url, Response<Void> response) {
        if (response.code() == 304) {
            map.get(url).setFileChanged(false);
        } else if (response.code() == 200) {
            map.get(url).setFileChanged(true);
        }
    }


}
