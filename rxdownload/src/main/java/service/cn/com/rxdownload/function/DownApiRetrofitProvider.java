package service.cn.com.rxdownload.function;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import service.cn.com.rxdownload.RxDownload;

//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by lanjl on 2018/7/13.
 */
public enum DownApiRetrofitProvider {
    INSTANCE;

    private static String ENDPOINT = "http://example.com/api/";

    private static Retrofit DownApiRetrofit;

    public static Retrofit getInstance() {

        if (DownApiRetrofit == null) {
            synchronized (RxDownload.class) {
                if (DownApiRetrofit == null) {
                    DownApiRetrofit =  buildApi();
                }
            }
        }
        return DownApiRetrofit;
    }

    private static Retrofit buildApi() {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(9, TimeUnit.SECONDS);

//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        builder.addInterceptor(interceptor);
        builder.addNetworkInterceptor(new StethoInterceptor());


        return new Retrofit.Builder().baseUrl(ENDPOINT)
                .client(builder.build())

                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

    }
}
