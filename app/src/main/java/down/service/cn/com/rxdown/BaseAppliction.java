package down.service.cn.com.rxdown;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import android.app.Application;

/**
 * Created by lanjl on 2016/12/5.
 */

public class BaseAppliction extends Application {


    public final void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
        initDbFlow();
        initComponent();

    }

    private void initComponent() {

//        ComponentHolder.setAppComponent(
//                DaggerAppComponent.builder().dataLayerModule(new DataLayerModule(this)).build()
//        );
//        SqlbriteHelper.init("DbFlowDataBase");

    }

    private void initDbFlow(){
        FlowManager.init(new FlowConfig.Builder(this).build());
    }

}
