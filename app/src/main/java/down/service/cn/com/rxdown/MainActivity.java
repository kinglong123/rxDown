package down.service.cn.com.rxdown;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import service.cn.com.rxdownload.RxDownload;
import service.cn.com.rxdownload.entity.DownloadBean;
import service.cn.com.rxdownload.entity.DownloadStatus;

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

                        System.out.println("111111111111111:" + status.getPercent());
                        mProgress.setProgress((int) (status.getDownloadSize()*100/status.getTotalSize()));

                    }

                    @Override
                    public void onError(Throwable e) {
//                        downloadController.setState(new DownloadController.Paused());
                    }

                    @Override
                    public void onComplete() {
//                        downloadController.setState(new DownloadController.Completed());
                    }
                });

    }


}
