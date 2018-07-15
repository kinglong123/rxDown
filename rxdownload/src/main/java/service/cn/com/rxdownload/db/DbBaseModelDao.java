//package service.cn.com.rxdownload.db;
//
////import com.nd.hy.android.commons.util.Ln;
////import com.nd.hy.android.elearning.specialtycourse.db.DbFlowDataBase;
//
//import com.kinglong.baseapp.mybaseapp.db.DbFlowDataBase;
//import com.raizlabs.android.dbflow.config.FlowManager;
//import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
//import com.raizlabs.android.dbflow.sql.language.SQLCondition;
//import com.raizlabs.android.dbflow.sql.language.Select;
//import com.raizlabs.android.dbflow.sql.language.Where;
//import com.raizlabs.android.dbflow.structure.BaseModel;
//import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
//
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
//public class DbBaseModelDao<T extends BaseModel> {
//
//
//    public static final int MODE_REPLACE = 0x1;
//
//    public static final int MODE_MORE = 0x2;
//
//    private Class<T> clazz;
//
//    SQLCondition[] selectionArgs;
//
//    public DbBaseModelDao(Class<T> clazz) {
//        this.clazz = clazz;
//    }
//
//    public DbBaseModelDao(Class<T> clazz, SQLCondition[] selectionArgs) {
//        this.clazz = clazz;
//        this.selectionArgs = selectionArgs;
//    }
//
//    public DbBaseModelDao(Class<T> clazz, ConditionGroup conditions) {
//        this.clazz = clazz;
//        selectionArgs = conditions.getConditions()
//                .toArray(new SQLCondition[conditions.size()]);
//    }
//
//    public final void updateList(List<T> data) {
//        updateList(data, 0, 0);
//    }
//
//    public final void updateList(List<T> data, int mode) {
//        if (data == null) {
//            return;
//        }
//        DatabaseWrapper database = FlowManager.getDatabase(DbFlowDataBase.class)
//                .getWritableDatabase();
//        database.beginTransaction();
//        try {
//            if (mode == MODE_MORE) {
//                for (T entry : data) {
//                    entry.save();
//                }
//            } else if (mode == MODE_REPLACE) {
//                doUpdateList(data, 0, 0);
//            }
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
////            Ln.d(e.getMessage());
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//
//    }
//
//    /**
//     * @param limit  每页数据的大小 size
//     * @param offset 数据偏移量， size * curentpage 如第二页为 2 x size
//     */
//    public final void updateList(List<T> data, int limit, int offset) {
//        DatabaseWrapper database = FlowManager.getDatabase(DbFlowDataBase.class)
//                .getWritableDatabase();
//        database.beginTransaction();
//        try {
//            doUpdateList(data, limit, offset);
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//    }
//
//    public final void update(T data) {
//        update(data, 0, 0);
//    }
//
//    public final void update(final T data, int mode) {
//
//        DatabaseWrapper database = FlowManager.getDatabase(DbFlowDataBase.class)
//                .getWritableDatabase();
//        database.beginTransaction();
//        try {
//            if (mode == MODE_MORE) {
//
//                // saveModel(data);
//                data.save();
//            } else if (mode == MODE_REPLACE) {
//                doUpdate(data, 0, 0);
//            }
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//    }
//
//    public final void update(T data, int limit, int offset) {
//        DatabaseWrapper database = FlowManager.getDatabase(DbFlowDataBase.class)
//                .getWritableDatabase();
//        database.beginTransaction();
//        try {
//            doUpdate(data, limit, offset);
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//
//    }
//
//    protected void doUpdate(T data, int limit, int offset) {
//        Where<T> from;
//        if (selectionArgs != null) {
//            from = new Select().from(clazz).where(selectionArgs);
//        } else {
//            from = new Select().from(clazz).where();
//        }
//        if (limit > 0) {
//            from.limit(limit).offset(offset);
//        }
//        doUpdate(data, from);
//    }
//
//    protected void doUpdateList(List<T> data, int limit, int offset) {
//        Where<T> from;
//        if (selectionArgs != null) {
//            from = new Select().from(clazz).where(selectionArgs);
//        } else {
//            from = new Select().from(clazz).where();
//        }
//        if (limit > 0) {
//            from.limit(limit).offset(offset);
//        }
//        doUpdateList(data, from);
//    }
//
//    protected void doUpdate(T data, Where<T> from) {
//        if (from == null) {
//            return;
//        }
//        List<T> entries = from.queryList();
//        if (entries != null && entries.size() > 0) {
////            TransactionManager.getInstance().addTransaction(
////                    new DeleteModelListTransaction<>(ProcessModelInfo.withModels(entries)));
////            deleteModel(entries);
//            for (T entry : entries) {
//                entry.delete();
//            }
//        }
//        if (data == null) {
//            return;
//        }
////        TransactionManager.getInstance().addTransaction(
////                new SaveModelTransaction<>(ProcessModelInfo.withModels(data)));
////        saveModel(data);
////        for (T entry : data) {
//        data.save();
////        }
//
//    }
//
//    protected void doUpdateList(List<T> data, Where<T> from) {
//        if (from == null) {
//            return;
//        }
//        List<T> entries = from.queryList();
//        if (entries != null && entries.size() > 0) {
////            TransactionManager.getInstance().addTransaction(
////                    new DeleteModelListTransaction<>(ProcessModelInfo.withModels(entries)));
////            deleteModel(entries);
//            for (T entry : entries) {
//                entry.delete();
//            }
//        }
//        if (data == null) {
//            return;
//        }
////        TransactionManager.getInstance().addTransaction(
////                new SaveModelTransaction<>(ProcessModelInfo.withModels(data)));
////        saveModel(data);
//        for (T entry : data) {
//            entry.save();
//        }
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
//                T data =  FlowManager.getModelAdapter(clazz).loadFromCursor(cursor);
////                T data = FlowManager.getModelAdapter(clazz).getSingleModelLoader()
////                        .convertToData(cursor, null);
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
