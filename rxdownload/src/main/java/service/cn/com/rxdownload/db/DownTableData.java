package service.cn.com.rxdownload.db;

/**
 * Created by lanjl on 2018/7/19.
 */

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Bryce on 2015/6/16.
 */
@Table(database = DbFlowDataBase.class)
public class DownTableData extends BaseModel implements Serializable {
    @Column
    @PrimaryKey(autoincrement = true)
    long did;


    @Column(name = "url")
    private String   url;

    @Column
    private String saveName;


    @Column
    private String savePath;

    @Column
    private String createTime;

    @Column
    private long     downloadSize;

    @Column
    private long     totalSize;

    @Column
    private boolean   isChunked;

    @Column
    private long   downloadFlag;

    @Column
    private String   extra1;
    @Column
    private String   extra2;
    @Column
    private String   extra3;
    @Column
    private String   extra4;

    @Column
    private String   extra5;

    @Column
    private Date date;



    @Column
    private String missionId;


    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public boolean isChunked() {
        return isChunked;
    }

    public void setChunked(boolean chunked) {
        isChunked = chunked;
    }

    public long getDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(long downloadFlag) {
        this.downloadFlag = downloadFlag;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3;
    }

    public String getExtra4() {
        return extra4;
    }

    public void setExtra4(String extra4) {
        this.extra4 = extra4;
    }

    public String getExtra5() {
        return extra5;
    }

    public void setExtra5(String extra5) {
        this.extra5 = extra5;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }
}
