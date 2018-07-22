package down.service.cn.com.rxdown;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import service.cn.com.rxdownload.RxDownload;
import service.cn.com.rxdownload.db.DataBaseHelper;
import service.cn.com.rxdownload.entity.DownloadBean;
import service.cn.com.rxdownload.entity.DownloadEvent;
import service.cn.com.rxdownload.entity.DownloadFlag;
import service.cn.com.rxdownload.entity.DownloadStatus;

import static android.widget.Toast.LENGTH_SHORT;
import static service.cn.com.rxdownload.utils.Utils.dispose;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.textView)
    TextView mTextView;

    @InjectView(R.id.finish)
    Button mFinish;

    @InjectView(R.id.img)
    ImageView mImg;

    @InjectView(R.id.status)
    TextView mStatus;

    @InjectView(R.id.percent)
    TextView mPercent;

    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @InjectView(R.id.size)
    TextView mSize;

    @InjectView(R.id.action)
    Button mAction;

    @InjectView(R.id.insert)
    Button mInsert;
    @InjectView(R.id.update)
    Button mUpdate;
    @InjectView(R.id.select)
    Button mSelect;
    @InjectView(R.id.tvstatus)
    Button mTvstatus;


    @InjectView(R.id.serviceAction)
    Button mServiceAction;
    @InjectView(R.id.pauseService)
    Button mPauseService;


    @InjectView(R.id.content_basic_download)
    RelativeLayout mContentBasicDownload;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });


        mInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadBean downloadBean = new DownloadBean();
                downloadBean.setExtra1("111");
                downloadBean.setSaveName("adqa");
                downloadBean.setUrl("http:asda");
                downloadBean.setSavePath("asda");

                DataBaseHelper.getSingleton(getApplicationContext()).insertRecord(downloadBean,12);
            }
        });
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadBean downloadBean = new DownloadBean();
                downloadBean.setExtra1("111");
                downloadBean.setSaveName("adqa");
                downloadBean.setUrl("http:asda");
                downloadBean.setSavePath("asda");
                DataBaseHelper.getSingleton(getApplicationContext()).updateRecord(downloadBean.getUrl(),"你","你",50);
            }
        });

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean s = DataBaseHelper.getSingleton(getApplicationContext()).recordNotExists("http:asda");
                System.out.println("111111111111:"+s);
            }
        });

        mServiceAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        mPauseService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPause();
            }
        });
    }

    public final static String URL = "http://dldir1.qq.com/weixin/android/weixin6330android920.apk";
    private void pause() {

        dispose(disposable);
    }
    private void start() {


        RxDownload.getInstance(this).download(new DownloadBean.Builder(URL)
                .setSaveName(null).setSavePath(null).build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadStatus>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
//                        downloadController.setState(new DownloadController.Started());
                    }

                    @Override
                    public void onNext(DownloadStatus status) {

                        System.out.println("111111111111111start:" + status.getPercent());
                        mProgress.setProgress((int) (status.getDownloadSize()*100/status.getTotalSize()));
                        mPercent.setText((int) (status.getDownloadSize()*100/status.getTotalSize())+"");

                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getApplicationContext(),"错误",Toast.LENGTH_LONG).show();
                        mTvstatus.setText("错误");
//                        downloadController.setState(new DownloadController.Paused());
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(getApplicationContext(),"完成",Toast.LENGTH_LONG).show();
                        mTvstatus.setText("完成");

//                        downloadController.setState(new DownloadController.Completed());
                    }
                });

    }


    private void startService() {
        System.out.println("1111111111111111startService");

        RxDownload.getInstance(this).serviceDownload(new DownloadBean.Builder(URL)
                .setSaveName(null).setSavePath(null).build())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        Toast.makeText(MainActivity.this, "下载开始", LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                    }
                });

    }

    private void startPause(){

        RxDownload.getInstance(this).pauseServiceDownload(URL)
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object o) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }



    @Override
    protected void onResume() {
        super.onResume();
        RxDownload.getInstance(this).receiveDownloadStatus(URL)//改用huyu那个，用广播方式来通知，然后自己去查询数据库状态
                .subscribe(new Consumer<DownloadEvent>() {
                    @Override
                    public void accept(DownloadEvent downloadEvent) throws Exception {
                        if (downloadEvent.getFlag() == DownloadFlag.FAILED) {
                            Throwable throwable = downloadEvent.getError();
                            throwable.printStackTrace();


                        }
//                        setEvent(downloadEvent);
//                        updateProgress(downloadEvent);

                        DownloadStatus downloadStatus = downloadEvent.getDownloadStatus();

                        System.out.println("111111111111111startService:" + downloadStatus.getPercent());
                        if(downloadStatus.getTotalSize() >0) {
                            mProgress.setProgress(
                                    (int) (downloadStatus.getDownloadSize() * 100 / downloadStatus
                                            .getTotalSize()));
                            mPercent.setText(
                                    (int) (downloadStatus.getDownloadSize() * 100 / downloadStatus
                                            .getTotalSize()) + "");
                        }
                    }
                });
    }



}
