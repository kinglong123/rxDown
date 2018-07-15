package service.cn.com.rxdownload.function;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.entity.DownloadType;
import service.cn.com.rxdownload.entity.TemporaryRecord;

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
//        if (fileChanged(url)) {//
            type = getNormalType(url);
//        } else {
//            type = getServerFileChangeType(url);
//        }
        return type;
    }


    private DownloadType getNormalType(String url) {
        DownloadType type;
//        if (supportRange(url)) {
//            type = new MultiThreadDownload(map.get(url));
//        } else {
            type = new DownloadType.NormalDownload(map.get(url));
//        }
        return type;
    }




}
