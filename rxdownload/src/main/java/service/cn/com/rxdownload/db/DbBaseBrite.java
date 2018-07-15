//package service.cn.com.rxdownload.db;
//
////import com.nd.hy.android.commons.util.Ln;
////import com.nd.hy.android.elearning.specialtycourse.db.DbFlowDataBase;
//
//import com.raizlabs.android.dbflow.config.FlowManager;
//import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
//import com.raizlabs.android.dbflow.sql.language.SQLCondition;
//import com.raizlabs.android.dbflow.sql.language.Select;
//import com.raizlabs.android.dbflow.sql.language.Where;
//import com.raizlabs.android.dbflow.structure.BaseModel;
//import com.raizlabs.android.dbflow.structure.ModelAdapter;
//import com.squareup.sqlbrite.BriteDatabase;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * BaseDao
// *
// * @version 2016/12/12
// * @anthor lanjl
// */
//public class DbBaseBrite<T extends BaseModel> {
//
//
//    public static final int MODE_REPLACE = 0x1;
//
//    public static final int MODE_MORE = 0x2;
//
//    private Class<T> clazz;
//    private final ModelAdapter<T> mAdapter;
//
//    ConditionGroup mConditionGroup;
//    BriteDatabase mBriteDatabase;
//
//    public void setBriteDatabase(BriteDatabase briteDatabase) {
//        mBriteDatabase = briteDatabase;
//    }
//
//
//    public DbBaseBrite(Class<T> clazz) {
//        this.clazz = clazz;
//        mAdapter = FlowManager.getModelAdapter(clazz);
//    }
//
//    public DbBaseBrite(Class<T> clazz, SQLCondition[] selectionArgs) {
//        this.clazz = clazz;
//        mConditionGroup= ConditionGroup.clause().andAll(selectionArgs);
//        mAdapter = FlowManager.getModelAdapter(clazz);
//
//    }
//
//    public DbBaseBrite(Class<T> clazz, ConditionGroup conditions) {
//        this.clazz = clazz;
//        mConditionGroup = conditions;
//        mAdapter = FlowManager.getModelAdapter(clazz);
//    }
//
//    public static <T extends BaseModel> ContentValues createValues(T model) {
//        ContentValues values = new ContentValues();
//        model.getModelAdapter().bindToInsertValues(values, model);
//        return values;
//    }
//
//
//    static  <T extends BaseModel> String getTableNameBase(Class<T> modelClass) {
//        ModelAdapter  adapter = FlowManager.getModelAdapter(modelClass);
//        String tableName = adapter.getTableName();
//        if (tableName.startsWith("`")) {
//            return tableName.substring(1, tableName.length() - 1);
//        }
//        return tableName;
//    }
//
//
//    public final void updateList(List<T> data) {
//        updateList(data, 0, 0);
//    }
//
//    public final void updateList(List<T> data, int mode) {
//        if (data == null) {
//            return;
//        }
//        BriteDatabase.Transaction transaction = mBriteDatabase.newTransaction();
//        try {
//            if (mode == MODE_MORE) {
//                for (T entry : data) {
//                    mBriteDatabase.insert(getTableNameBase(clazz),createValues(entry));
//                }
//            } else if (mode == MODE_REPLACE) {
//               doUpdateList(data, 0, 0);
//
//            }
//            transaction.markSuccessful();
//        } catch (Exception e) {
////            Ln.d(e.getMessage());
//            e.printStackTrace();
//        } finally {
//            transaction.end();
//        }
//
//    }
//
//    /**
//     * @param limit  每页数据的大小 size
//     * @param offset 数据偏移量， size * curentpage 如第二页为 2 x size
//     */
//    public final void updateList(List<T> data, int limit, int offset) {
//        BriteDatabase.Transaction transaction = mBriteDatabase.newTransaction();
//
//        try {
//            doUpdateList(data, limit, offset);
//            transaction.markSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            transaction.end();
//        }
//    }
//
//    public final void update(T data) {
//        update(data, 0, 0);
//    }
//
//    public final void update(final T data, int mode) {
//
//
//        BriteDatabase.Transaction transaction = mBriteDatabase.newTransaction();
//        try {
//            if (mode == MODE_MORE) {
//
//                // saveModel(data);
////                data.save();
//                mBriteDatabase.insert(getTableNameBase(clazz),createValues(data));
//            } else if (mode == MODE_REPLACE) {
//                doUpdate(data, 0, 0);
//            }
//            transaction.markSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            transaction.end();
//        }
//    }
//
//    public final void update(T data, int limit, int offset) {
//        BriteDatabase.Transaction transaction = mBriteDatabase.newTransaction();
//        try {
//            doUpdate(data, limit, offset);
//            transaction.markSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            transaction.end();
//        }
//
//    }
//
//    protected void doUpdate(T data, int limit, int offset) {
//        Where<T> from;
//        if (mConditionGroup != null) {
//            from = new Select().from(clazz).where(mConditionGroup);
//        } else {
//            from = new Select().from(clazz).where();
//        }
//        if (limit > 0) {
//            from.limit(limit).offset(offset);
//        }
//        doUpdate(data, from);
//
////        String sql = mConditionGroup.getQuery();
////        if (limit > 0) {
//////            queryBuilder.appendQualifier("LIMIT", String.valueOf(limit));
////            sql =sql   + " LIMIT "+String.valueOf(limit)+ " OFFSET "+String.valueOf(offset);
////        }
////
////        mBriteDatabase.update(getTableNameBase(clazz),createValues(data),sql);
//    }
//
//    protected void doUpdateList(List<T> data, int limit, int offset) {
//        Where<T> from;
//        if (mConditionGroup != null) {
//            from = new Select().from(clazz).where(mConditionGroup);
//        } else {
//            from = new Select().from(clazz).where();
//        }
//        if (limit > 0) {
//            from.limit(limit).offset(offset);
//        }
//        doUpdateList(data, from);
//
//      //  SELECT did FROM Message  ORDER BY did DESC LIMIT 3
//
////        mBriteDatabase.createQuery()
///*
//        String sql = mConditionGroup.getQuery();
//        if (limit > 0) {
////            queryBuilder.appendQualifier("LIMIT", String.valueOf(limit));
////            if(StringUtils.isNullOrEmpty(sql)){
////                sql ="1==1";
////            }
////            sql = sql   + " limit "+String.valueOf(limit);
//
//
//        }
//
//         sql  = "DELETE FROM "
//                 + getTableNameBase(clazz)
//                 + " WHERE did IN "
//                 + "(SELECT did FROM "
//                 + getTableNameBase(clazz)
//                 + " LIMIT "
//                 + limit
//                 + ","
//                 + offset
//                 + ")";
//
//        sql  =  " did IN "
//                + "(SELECT did FROM "
//                + getTableNameBase(clazz)
//                + " LIMIT "
//                + limit
//                + ","
//                + offset
//                + ")";
//
////        sql = " DELETE FROM Message where " +sql ;
////        mBriteDatabase.delete()
//
//        mBriteDatabase.delete(getTableNameBase(clazz),sql);
//
//        mBriteDatabase.delete(getTableNameBase(clazz), mAdapter.getPrimaryConditionClause(model).getQuery());
//        for (T entry : data) {
//            mBriteDatabase.insert(getTableNameBase(clazz),createValues(entry));
//        }
//*/
//
//    }
//
//
//
//    protected void doUpdate(T data, Where<T> from) {
//        if (from == null) {
//            return;
//        }
//        List<T> entries = from.queryList();
//        if (entries != null && entries.size() > 0) {
//            for (T entry : entries) {
////                entry.delete();
//                mBriteDatabase.delete(getTableNameBase(clazz), mAdapter.getPrimaryConditionClause(entry).getQuery());
//
////                mBriteDatabase.delete()
//            }
//        }
////        mBriteDatabase.delete()
//        if (data == null) {
//            return;
//        }
//        mBriteDatabase.insert(getTableNameBase(clazz),createValues(data));
//
//
////        mBriteDatabase.update(getTableNameBase(clazz),createValues(data),mConditionGroup.getQuery());
//
//    }
//
//    protected void doUpdateList(List<T> data, Where<T> from) {
//        if (from == null) {
//            return;
//        }
//        if (from == null) {
//            return;
//        }
//        List<T> entries = from.queryList();
//        if (entries != null && entries.size() > 0) {
//            for (T entry : entries) {
////                entry.delete();
////                entry.
//               // mBriteDatabase.delete(getTableNameBase(clazz),mConditionGroup.getQuery());
//                mBriteDatabase.delete(getTableNameBase(clazz), mAdapter.getPrimaryConditionClause(entry).getQuery());
//            }
//        }
//        if (data == null) {
//            return;
//        }
//
////        if (limit > VALUE_UNSET) {
////            queryBuilder.appendQualifier("LIMIT", String.valueOf(limit));
////        }
////        if (offset > VALUE_UNSET) {
////            queryBuilder.appendQualifier("OFFSET", String.valueOf(offset));
////        }
////        mBriteDatabase.delete(getTableNameBase(clazz),mConditionGroup.getQuery());
//        for (T entry : data) {
//            mBriteDatabase.insert(getTableNameBase(clazz),createValues(entry));
//        }
//
//    }
//
//    public static <T extends BaseModel> List<T> listFromCursor(Cursor cursor,
//            Class<T> clazz) {
//        if (cursor == null) {
//            return null;
//        }
//        List<T> result = new ArrayList<T>();
//        while (cursor.moveToNext()) {
//            try {
////                T data = clazz.newInstance();
//
////                FlowManager.getModelAdapter(clazz).loadFromCursor()
//                T data = FlowManager.getModelAdapter(clazz).getSingleModelLoader()
//                        .convertToData(cursor, null);
//                result.add(data);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }
//
//
//    public static <T extends BaseModel> List<T> listFromCursor1(Cursor cursor,
//            Class<T> clazz) {
//        if (cursor == null) {
//            return null;
//        }
//        List<T> result = new ArrayList<T>();
//        try {
//
//            result = FlowManager.getModelAdapter(clazz).getListModelLoader()
//                    .convertToData(cursor, null);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
////            catch (IllegalAccessException e) {
////                e.printStackTrace();
////            }
////        }
//        return result;
//    }
//
//
//    private void TestTransaction() {
//        /*
//        //自定义Transaction 第一
//        BaseTransaction<ProjectInfo> tesT1BaseTransaction
//                = new BaseTransaction<ProjectInfo>() {
//            @Override
//            public ProjectInfo onExecute() {
//
//                ProjectInfo people = new Select().from(ProjectInfo.class)
//                        .where(ProjectInfo_Table.projectId.eq("1021")).querySingle();
//                people.setTitle("aaaaa");
//                people.save();
////                if(true) {
////                  int a=  1 / 0;
////                }
//
//                return people;
//            }
//
//            @Override
//            public void onPostExecute(ProjectInfo projectInfo) {
//                super.onPostExecute(projectInfo);
//            }
//
//        };
//        TransactionManager.getInstance().addTransaction(tesT1BaseTransaction);
//
//        //--------- 第二
//        TransactionManager.transact(CompulsoryBase.NAME, new Runnable() {
//            @Override
//            public void run() {
//                ProjectInfo people = new Select().from(ProjectInfo.class)
//                        .where(ProjectInfo_Table.projectId.eq("1021")).querySingle();
//                people.setTitle("aaaaa");
//                people.save();
//            }
//        });
//        //---------第三
//        DatabaseWrapper database = FlowManager.getDatabase(CompulsoryBase.NAME)
//                .getWritableDatabase();
//        database.beginTransaction();
//        try {
//            ProjectInfo people1 = new Select().from(ProjectInfo.class)
//                    .where(ProjectInfo_Table.projectId.eq("1021")).querySingle();
//            people1.setTitle("fffff11111");
//            people1.save();
////                if(true) {
////                  int a=  1 / 0;
////                }
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            Ln.e(e.toString());
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//        //---------第四
//        Where<ProjectInfo> update = SQLite.update(ProjectInfo.class)
//                .set(ProjectInfo_Table.projectId.eq("other"))
//                .where(ProjectInfo_Table.projectId.is("1024"));
//        update.execute();//1
//
//        TransactionManager
//                .getInstance()
//                .addTransaction(
//                        new QueryTransaction(
//                                DBTransactionInfo.create(BaseTransaction.PRIORITY_UI),
//                                update));//2
//                                */
//    }
//
//
//}
