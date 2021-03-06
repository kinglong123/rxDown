package service.cn.com.rxdownload.db;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.List;

import rx.schedulers.Schedulers;
import service.cn.com.rxdownload.entity.DownloadBean;
import service.cn.com.rxdownload.entity.DownloadFlag;
import service.cn.com.rxdownload.entity.DownloadStatus;

import static service.cn.com.rxdownload.db.DBColumn.insert;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/14
 * Time: 10:02
 * FIXME
 */
public class DataBaseHelper {
    private volatile static DataBaseHelper singleton;
    BriteDatabase mBriteDatabase;

    private DataBaseHelper(Context context) {

    }

    public static DataBaseHelper getSingleton(Context context) {
        if (singleton == null) {
            synchronized (DataBaseHelper.class) {
                if (singleton == null) {
                    singleton = new DataBaseHelper(context);
                }
            }
        }
        return singleton;
    }

    public void init(String name){
        if(mBriteDatabase != null){
            return;
        }
        DatabaseDefinition databaseDefinition = FlowManager.getDatabase(name);

        SQLiteOpenHelper helper = null;
        OpenHelper openHelper = databaseDefinition.getHelper();
        if (openHelper instanceof SQLiteOpenHelper) {
            helper = (SQLiteOpenHelper) openHelper;
        }
        SqlBrite sqlBrite = SqlBrite.create(new SqlBrite.Logger() {
            @Override
            public void log(String message) {
//                Timber.tag("Database").v(message);
//                System.out.println(message);
                System.out.println("Database"+message);
            }
        });
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io());
        db.setLoggingEnabled(true);
        mBriteDatabase = db;
    }

    public BriteDatabase getBriteDatabase() {
        if(mBriteDatabase == null){
            init(DbFlowDataBase.NAME);
        }
        return mBriteDatabase;
    }


    public boolean recordNotExists(String url) {


        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        DbBaseBrite<DownTableData> dao = new DbBaseBrite<DownTableData>(
                DownTableData.class, group);

       List<DownTableData> downTableData=  dao.selectDate();

       return downTableData.size() == 0;



    }


    public void insertRecord(DownloadBean downloadBean, int flag, String missionId) {
//        return getWritableDatabase().insert(TABLE_NAME, null, insert(downloadBean, flag, missionId));

        DownTableData downTableData = insert(downloadBean, flag, null);

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(downloadBean.getUrl()));

        DbBaseBrite<DownTableData> dao = new DbBaseBrite<DownTableData>(
                DownTableData.class, group);

        dao.setBriteDatabase(getBriteDatabase());

        dao.update(downTableData);
    }



    public void insertRecord(DownloadBean downloadBean, int flag) {
        DownTableData downTableData = insert(downloadBean, flag, null);

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(downloadBean.getUrl()));

        DbBaseBrite<DownTableData> dao = new DbBaseBrite<DownTableData>(
                DownTableData.class, group);

        dao.setBriteDatabase(getBriteDatabase());

        dao.update(downTableData);

    }

    public void updateRecord(String url, int flag,String missionId) {

        System.out.println("11111111updateRecord:"+flag);

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        ConditionGroup setValue =  ConditionGroup.clause();
        setValue.and(DownTableData_Table.downloadFlag.eq(flag));
        setValue.and(DownTableData_Table.missionId.eq(missionId));

        List<SQLCondition> sqlConditions = setValue.getConditions();
        SQLCondition[] setValueArray= sqlConditions.toArray(new SQLCondition[sqlConditions.size()]);
        DbBaseBrite.updateField(DownTableData.class,setValueArray,group);

//        return getWritableDatabase().update(TABLE_NAME, update(flag),
//                COLUMN_URL + "=?", new String[]{url});
    }

    public void updateRecord(String url, int flag) {

        System.out.println("11111111updateRecord:"+flag);

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        ConditionGroup setValue =  ConditionGroup.clause();
        setValue.and(DownTableData_Table.downloadFlag.eq(flag));

        List<SQLCondition> sqlConditions = setValue.getConditions();
        SQLCondition[] setValueArray= sqlConditions.toArray(new SQLCondition[sqlConditions.size()]);
        DbBaseBrite.updateField(DownTableData.class,setValueArray,group);

//        return getWritableDatabase().update(TABLE_NAME, update(flag),
//                COLUMN_URL + "=?", new String[]{url});
    }


    public void updateRecord(String url, String saveName, String savePath, int flag) {

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        ConditionGroup setValue =  ConditionGroup.clause().and(DownTableData_Table.saveName.eq(saveName));
        setValue.and(DownTableData_Table.savePath.eq(savePath))
                .and(DownTableData_Table.downloadFlag.eq(flag));
        List<SQLCondition> sqlConditions = setValue.getConditions();
        SQLCondition[] setValueArray= sqlConditions.toArray(new SQLCondition[sqlConditions.size()]);
        DbBaseBrite.updateField(DownTableData.class,setValueArray,group);



        //        String s = "UPDATE `DownTableData` SET (`saveName`='sa') WHERE (`url`='http:asda')";
//
//        SQLite.update(DownTableData.class)
//                .set(sss)
//
//                .where(group)
//                .query();
//
//        return getWritableDatabase().update(TABLE_NAME, update(flag),
//                COLUMN_URL + "=?", new String[]{url});
    }

//    public long updateRecord(String url, String saveName, String savePath, int flag) {
//        return getWritableDatabase().update(TABLE_NAME, update(saveName, savePath, flag),
//                COLUMN_URL + "=?", new String[]{url});
//    }

    public void updateStatus(String url, DownloadStatus status) {

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        ConditionGroup setValue =  ConditionGroup.clause().and(DownTableData_Table.isChunked.eq(status.isChunked));
        setValue.and(DownTableData_Table.downloadSize.eq(status.getDownloadSize()))
                .and(DownTableData_Table.totalSize.eq(status.getTotalSize()));
        List<SQLCondition> sqlConditions = setValue.getConditions();
        SQLCondition[] setValueArray= sqlConditions.toArray(new SQLCondition[sqlConditions.size()]);
        DbBaseBrite.updateField(DownTableData.class,setValueArray,group);



    }


    public void repairErrorFlag() {

        System.out.println("111111111111111repairErrorFlag");
        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.downloadFlag.eq(DownloadFlag.STARTED));

        ConditionGroup setValue =  ConditionGroup.clause();
        setValue.and(DownTableData_Table.downloadFlag.eq(DownloadFlag.STARTED));
        List<SQLCondition> sqlConditions = setValue.getConditions();
        SQLCondition[] setValueArray= sqlConditions.toArray(new SQLCondition[sqlConditions.size()]);
        DbBaseBrite.updateField(DownTableData.class,setValueArray,group);



    }
    /**
     * Read the url's download status.
     *
     * @param url url
     * @return download status
     */
    public DownloadStatus readStatus(String url) {



        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        DbBaseBrite<DownTableData> dao = new DbBaseBrite<DownTableData>(
                DownTableData.class, group);

        List<DownTableData> downTableData=  dao.selectDate();
        DownloadStatus mDownloadStatus = new DownloadStatus();

        if(downTableData == null ||downTableData.size() ==0){
//            return mDownloadStatus;
        }else {
            DownTableData downTableData1 =  downTableData.get(0);

            mDownloadStatus.setDownloadSize(downTableData1.getDownloadSize());
            mDownloadStatus.setTotalSize(downTableData1.getTotalSize());
            mDownloadStatus.setChunked(downTableData1.isChunked());

        }
        return mDownloadStatus;
    }

    /**
     * Read single Record.
     *
     * @param url url
     * @return Record
     */
    @Nullable
    public DownTableData readSingleRecord(String url) {

        ConditionGroup group =  ConditionGroup.clause().and(DownTableData_Table.url.eq(url));

        DbBaseBrite<DownTableData> dao = new DbBaseBrite<DownTableData>(
                DownTableData.class, group);

        List<DownTableData> downTableData=  dao.selectDate();
        if(downTableData == null || downTableData.size() ==0){
            return null;
        }else {
            return downTableData.get(0);
        }

    }

}