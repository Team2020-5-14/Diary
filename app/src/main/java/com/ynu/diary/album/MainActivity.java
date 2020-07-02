package com.ynu.diary.album;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ynu.diary.R;
import com.ynu.diary.album.dao.MyDatabaseOperator;
import com.ynu.diary.shared.ThemeManager;
import com.ynu.diary.shared.statusbar.ChinaPhoneHelper;

import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.IOException;

import static com.ynu.diary.album.utils.ImageDealer.do_tensorflow;
import static com.ynu.diary.album.utils.ImageDealer.insertImageIntoDB;

/**
 * created by dongchangzhang
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String PHOTO_OVERVIEW_TOPIC_ID = "PHOTOOVERVIEW_TOPIC_ID";
    public final static String PHOTO_OVERVIEW_DIARY_ID = "PHOTOOVERVIEW_DIARY_ID";

    private long topicId;
    private long diaryId;
    //UI Object
    private TextView txt_photos;
    private TextView txt_albums;
    //Fragment Object
    private Photos photos;
    private Albums albums;
    private FragmentManager fManager;

    // notification
    private int NOTI_CODE_HAVE_NEW = 1;
    private int NOTI_CODE_CLASSIFYING = 2;
    private int NOTI_CODE_FINISHED = 3;
    private NotificationManager manager;

    private TensorFlowImageClassifier classifier;

    // topbar
    private ImageView iv_main_back;
    private TextView tv_main_topbar;

    // fragment
    private FragmentTransaction fTransaction;
    private boolean havaInAlbum = false;


    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // scanning images
                case 0x11:
                    albums.onRefresh();
                    break;

            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get topic id
        topicId = getIntent().getLongExtra(PHOTO_OVERVIEW_TOPIC_ID, -1);
        //get topic fail , close this activity
        if (topicId == -1) {
            Toast.makeText(this, getString(R.string.photo_viewer_topic_fail)
                    , Toast.LENGTH_LONG).show();
            finish();
        }
        diaryId = getIntent().getLongExtra(PHOTO_OVERVIEW_DIARY_ID, -1);
        Log.d("MAIN:topicID", String.valueOf(topicId));
        Log.d("MAIN:diaryId", String.valueOf(diaryId));

        setContentView(R.layout.activity_main_n);
        tv_main_topbar = (TextView) findViewById(R.id.tv_main_topbar);
        tv_main_topbar.setTextColor(ThemeManager.getInstance().getThemeDarkColor(this));
        iv_main_back = (ImageView) findViewById(R.id.iv_main_back);
        iv_main_back.setOnClickListener(this);
        iv_main_back.setColorFilter(ThemeManager.getInstance().getThemeDarkColor(this));
        //For set status bar
        ChinaPhoneHelper.setStatusBar(this, true);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // fragment
        fManager = getFragmentManager();
        bindViews();
        txt_photos.performClick();

        classifyImagesAtBackground();
    }

    private void sendMessages(String title, String message, int code) {
        // show notification about tf information of image
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentText(message);
        mBuilder.setFullScreenIntent(null, true);
        mBuilder.setAutoCancel(true);

        manager.notify(code, mBuilder.build());
    }

    private void sendMessages(int now, int sum, int code) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.camera));
        //禁止用户点击删除按钮删除
        builder.setAutoCancel(true);
        //禁止滑动删除
        builder.setOngoing(false);
        //取消右上角的时间显示
        builder.setShowWhen(true);
        if (now != sum)
            builder.setContentTitle("正在新处理图片...   " + now + "/" + sum);
        else {
            builder.setContentTitle("图片处理完成");
            sendMessages("图片处理完成", "享受您的精彩之旅吧！", NOTI_CODE_FINISHED);
        }
        builder.setProgress(sum, now, false);
        //builder.setContentInfo(progress+"%");
        builder.setOngoing(true);
        builder.setShowWhen(false);
        //Intent intent = new Intent(this,DownloadService.class);
        //intent.putExtra("command",1);
        Notification notification = builder.build();
        NotificationManager manger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(code, notification);
    }

    // classify images at background
    private void classifyImagesAtBackground() {
        if (Config.needToBeClassified == null) {
            // do nothing
            Log.d("Main-TF", "null");
        } else if (Config.needToBeClassified.size() == 0) {
            // do nothing
            Log.d("Main-TF", "0");
        } else {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    Looper.prepare();
                    Bitmap bitmap;
                    // init tensorflow
                    if (classifier == null) {
                        // get permission
                        classifier = new TensorFlowImageClassifier();
                        try {
                            classifier.initializeTensorFlow(
                                    getAssets(), Config.MODEL_FILE, Config.LABEL_FILE,
                                    Config.NUM_CLASSES, Config.INPUT_SIZE, Config.IMAGE_MEAN,
                                    Config.IMAGE_STD, Config.INPUT_NAME, Config.OUTPUT_NAME);
                        } catch (final IOException e) {

                        }
                    }
                    ContentValues value = new ContentValues();
                    MyDatabaseOperator myoperator = new MyDatabaseOperator(MainActivity.this,
                            Config.DB_NAME, Config.dbversion);
                    int now = 1;
                    sendMessages("正在为您处理" + Config.needToBeClassified.size()
                            + "张新的图片", "您可以到通知中心查看处理进度", NOTI_CODE_HAVE_NEW);
                    for (String image : Config.needToBeClassified) {
                        Log.d("classifyImages", image);


                        sendMessages(now++, Config.needToBeClassified.size(), NOTI_CODE_CLASSIFYING);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                        bitmap = BitmapFactory.decodeFile(image, options);
                        insertImageIntoDB(image, do_tensorflow(bitmap, classifier), myoperator, value);
                        if (havaInAlbum) {
                            myHandler.sendEmptyMessage(0x11);
                        }

                    }
                    Config.needToBeClassified = null;
                    myoperator.close();
                    Looper.loop();
                }
            }).start();
        }
    }

    //UI组件初始化与事件绑定
    private void bindViews() {
        // 定位textview
        txt_photos = (TextView) findViewById(R.id.all_photos);
        txt_albums = (TextView) findViewById(R.id.all_albums);
        // 对其设置监听动作
        txt_photos.setOnClickListener(this);
        txt_albums.setOnClickListener(this);
    }

    //重置所有文本的选中状态为未点击状态
    private void setSelected() {
        txt_photos.setSelected(false);
        txt_albums.setSelected(false);
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (photos != null) fragmentTransaction.hide(photos);
        if (albums != null) fragmentTransaction.hide(albums);
    }

    @Override
    public void onClick(View v) {
        fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (v.getId()) {
            // 照片
            case R.id.all_photos:
                tv_main_topbar.setText("照片");
                setSelected();
                txt_photos.setSelected(true);
                // 暂时使用弹出堆栈，以避免从相簿进入相册无法返回
                // 可以使用其他方法，这个方法不好，下面相同
                // https://blog.csdn.net/qq_35988274/article/details/100518346
                getFragmentManager().popBackStack();
                if (photos == null) {
                    photos = new Photos(topicId);
                    fTransaction.add(R.id.ly_content, photos);
                } else {
                    fTransaction.show(photos);
                }
                break;
            // 相册
            case R.id.all_albums:
                tv_main_topbar.setText("相册");
                setSelected();
                txt_albums.setSelected(true);
                getFragmentManager().popBackStack();
                if (albums == null) {
                    albums = new Albums();
                    fTransaction.add(R.id.ly_content, albums);
                } else {
                    albums.onRefresh();
                    fTransaction.show(albums);
                }
                havaInAlbum = true;
                break;
            case R.id.iv_main_back:
                this.finish();
                break;
        }
        fTransaction.commit();
    }
}
