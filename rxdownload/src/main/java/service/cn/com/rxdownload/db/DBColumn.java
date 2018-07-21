package service.cn.com.rxdownload.db;

import java.util.Date;

import service.cn.com.rxdownload.entity.DownloadBean;

/**
 * Created by lanjl on 2018/7/19.
 */
public class DBColumn {
    static final String COLUMN_ID = "id";
    static final String COLUMN_URL = "url";
    static final String COLUMN_SAVE_NAME = "saveName";
    static final String COLUMN_SAVE_PATH = "savePath";
    static final String COLUMN_DOWNLOAD_SIZE = "downloadSize";
    static final String COLUMN_TOTAL_SIZE = "totalSize";
    static final String COLUMN_IS_CHUNKED = "isChunked";
    static final String COLUMN_DOWNLOAD_FLAG = "downloadFlag";
    static final String COLUMN_EXTRA1 = "extra1";
    static final String COLUMN_EXTRA2 = "extra2";
    static final String COLUMN_EXTRA3 = "extra3";
    static final String COLUMN_EXTRA4 = "extra4";
    static final String COLUMN_EXTRA5 = "extra5";
    static final String COLUMN_DATE = "date";
    static final String COLUMN_MISSION_ID = "missionId";

    static DownTableData insert(DownloadBean bean, int flag, String missionId) {
        DownTableData values = new DownTableData();
        values.setUrl(bean.getUrl());
        values.setSaveName(bean.getSaveName());
        values.setSavePath(bean.getSavePath());
        values.setDownloadFlag(flag);

        values.setExtra1(bean.getExtra1());
        values.setExtra2(bean.getExtra2());
        values.setExtra3(bean.getExtra3());
        values.setExtra4(bean.getExtra4());
        values.setExtra5(bean.getExtra5());
        values.setDate(new Date());
        values.setMissionId(missionId);


        return values;
    }
}
