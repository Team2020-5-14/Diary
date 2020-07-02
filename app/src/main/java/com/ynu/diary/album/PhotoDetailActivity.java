package com.ynu.diary.album;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ynu.diary.R;
import com.ynu.diary.album.adapter.HorizontalScrollViewAdapter;
import com.ynu.diary.album.view.MyHorizontalScrollView;
import com.ynu.diary.shared.FileManager;
import com.ynu.diary.shared.ThemeManager;
import com.ynu.diary.shared.statusbar.ChinaPhoneHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ynu.diary.album.utils.ImagesScaner.getAlbumPhotos;
import static com.ynu.diary.shared.FileManager.DIARY_ROOT_DIR;

/**
 * 查看照片细节的activity
 * 点击照片后进入这个界面查看照片细节
 * Created by me on 16-12-20.
 */
// tf
// some method may be used about db or others
// adapter && view

public class PhotoDetailActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String PHOTO_OVERVIEW_TOPIC_ID = "PHOTOOVERVIEW_TOPIC_ID";
    public final static String PHOTO_OVERVIEW_DIARY_ID = "PHOTOOVERVIEW_DIARY_ID";

    private long topicId;
    private long diaryId;

    private ArrayList<String> diaryPhotoFileList;

    // which image
    int position_now = -1;
    // image url
    String url = null;
    // has been init
    boolean init = false;
    // 自定义的布局， 实现下面缩略图，上面大图
    private MyHorizontalScrollView mHorizontalScrollView;
    // 适配器
    private HorizontalScrollViewAdapter mAdapter;
    private ImageView mImg;
    // 照片数组。照片在drawable文件夹中，名字为a.png ...
    private List<Map> mDatas;
    private String type = null;

    private int position_tmp;
    private int count;

    // topbar
    private ImageView iv_photo_detail_back;
    private ImageView iv_photo_info;

    private Handler myHandler = new Handler() {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x21:

                    Glide
                            .with(PhotoDetailActivity.this)
                            .load((String) mDatas.get(position_tmp).get("_data"))
                            .error(R.drawable.error)
                            .thumbnail(0.1f)
                            .into(mImg);

                    break;

                case 0x22:

                    break;
            }
        }
    };

    public PhotoDetailActivity() {

    }

    // 重写创建活动的方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_detail);
        ChinaPhoneHelper.setStatusBar(this, true);
        iv_photo_detail_back = (ImageView) findViewById(R.id.iv_photo_detail_back);
        iv_photo_info = (ImageView) findViewById(R.id.iv_photo_info);
        iv_photo_detail_back.setColorFilter(ThemeManager.getInstance().getThemeDarkColor(this));
        iv_photo_info.setColorFilter(ThemeManager.getInstance().getThemeDarkColor(this));
        iv_photo_detail_back.setOnClickListener(this);
        iv_photo_info.setOnClickListener(this);

        //get topic id
        topicId = getIntent().getLongExtra(PHOTO_OVERVIEW_TOPIC_ID, -1);
        //get topic fail , close this activity
        if (topicId == -1) {
            Toast.makeText(this, getString(R.string.photo_viewer_topic_fail)
                    , Toast.LENGTH_LONG).show();
            finish();
        }
        diaryId = getIntent().getLongExtra(PHOTO_OVERVIEW_DIARY_ID, -1);
        Log.d("PDetail:topicID", String.valueOf(topicId));
        Log.d("PDetail:diaryId", String.valueOf(diaryId));


//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("");

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
//        getSupportActionBar().setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#11000000")));
        // 绑定textview按钮
//        bindViews();
        // get photo list
        initPhoto();
        if (type != null)
            mDatas = getAlbumPhotos(this, this.type);
            // 下面设置下面缩略图上面大图。
        else {
            //Load the data
            List<Map> result = new ArrayList<>();
            loadDiaryImageData(topicId, diaryId);
            Map<String, String> tmp;
            for (String url : diaryPhotoFileList) {
                tmp = new HashMap<>();
//                tmp.put("album_name", "ALL");
//                tmp.put("url", url);
                tmp.put("_data", url);
                result.add(tmp);
            }
            mDatas = result;
        }
        count = mDatas.size();
        //Load the data
//            loadDiaryImageData(topicId, diaryId);
//        mDatas = getMediaImageInfo(this.getBaseContext());
        mImg = (ImageView) findViewById(R.id.id_content);

        Glide.with(PhotoDetailActivity.this).load((String) mDatas.get(position_now).get("_data")).into(mImg);
        mHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.id_horizontalScrollView);
        mAdapter = new HorizontalScrollViewAdapter(this, mDatas);
        //添加滚动回调
        mHorizontalScrollView
                .setCurrentImageChangeListener(new MyHorizontalScrollView.CurrentImageChangeListener() {
                    @Override
                    public void onCurrentImgChanged(int position,
                                                    View viewIndicator) {
                        if (!init) {
                            position = position_now;
                            init = true;
                        }
                        Log.d("PhotoDetail: ", "Image change to: " + position);
                        try {
                            position_tmp = position;
                            myHandler.sendEmptyMessage(0x21);
                        } catch (Exception e) {
                            ;
                        }

                        viewIndicator.setBackgroundColor(Color.parseColor("#AA024DA4"));
                    }
                });
        //添加点击回调
        mHorizontalScrollView.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener() {

            @Override
            public void onClick(View view, int position) {
                if (position >= count) {
                    ;
                } else {
                    if (mDatas.get(position) != null) {
                        mImg.setImageURI(Uri.fromFile(new File((String) mDatas.get(position).get("_data"))));
                        view.setBackgroundColor(Color.parseColor("#AA024DA4"));
                    }
                }
            }
        });
        //设置适配器
        mHorizontalScrollView.initDatas(mAdapter, position_now);
    }

    private void loadDiaryImageData(long topicId, long diaryId) {
        FileManager diaryRoot = new FileManager(PhotoDetailActivity.this, DIARY_ROOT_DIR);
        File topicRootFile;
        if (diaryId != -1) {
            topicRootFile = new File(diaryRoot.getDirAbsolutePath() + "/" + topicId + "/" + diaryId);
            Log.d("Wel:topicRootFile", topicRootFile.getPath());
        } else {
            topicRootFile = new File(diaryRoot.getDirAbsolutePath() + "/" + topicId);
            Log.d("Wel:topicRootFile", topicRootFile.getPath());
        }
        //Load all file form topic dir
        diaryPhotoFileList = new ArrayList<>();
        for (File photoFile : getFilesList(topicRootFile)) {
            diaryPhotoFileList.add(photoFile.getPath());
        }
    }

    private List<File> getFilesList(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getFilesList(file));
            } else {
                if (file.getName().split("_")[0].equals("photo")) {
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private void initPhoto() {
        Intent intent = getIntent();
        try {
            position_now = intent.getIntExtra("position", -1);
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");

        } catch (Exception e) {
            Log.d("ERROR: ", "" + e);
        }
        Log.d("Test-----------------: ", "" + position_now + " " + url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo_detail_back:
                this.finish();
                break;
            case R.id.iv_photo_info:
                Intent intent = new Intent(this, PhotoInfoActivity.class);
                int position;
                if (!init) {
                    position = position_now;
                } else {
                    position = mHorizontalScrollView.getmShowIndex();
                }
                // send args
                intent.putExtra("position", position);
                intent.putExtra("url", (String) mDatas.get(position).get("_data"));
                startActivity(intent);
                break;
        }
    }
}
