package com.zego.videofilter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.zego.videofilter.R;
import com.zego.videofilter.databinding.ActivityVideoFilterMainBinding;
import com.zego.videofilter.faceunity.FURenderer;
import com.zego.videofilter.faceunity.authpack;

import im.zego.common.ui.BaseActivity;
import im.zego.common.util.AppLogger;
import im.zego.common.widgets.CustomPopWindow;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;

public class VideoFilterMainUI extends BaseActivity implements View.OnClickListener {

    private ActivityVideoFilterMainBinding binding;

    private FUBeautyActivity.FilterType mFilterType = FUBeautyActivity.FilterType.FilterType_SurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_filter_main);

        // 获取选定的前处理传递数据的类型
        setCheckedFilterTypeListener();

        // 前处理传递数据类型说明的点击事件监听
        binding.RadioMemTexture2D.setOnClickListener(this);

        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (null == authpack.A()) {
            binding.authpack.setText(R.string.tx_has_no_fu_authpack);
            binding.loginBtn.setVisibility(View.INVISIBLE);
        } else {
            // 初始化 FaceUnity
            FURenderer.initFURenderer(this);
        }
    }

    // 获取选定的前处理传递数据的类型
    public void setCheckedFilterTypeListener(){
        binding.RadioMemTexture2D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zegoEngineConfig.customVideoCaptureMainConfig = new ZegoCustomVideoCaptureConfig();
                zegoEngineConfig.customVideoCaptureMainConfig.bufferType = ZegoVideoBufferType.SURFACE_TEXTURE;
                mFilterType = FUBeautyActivity.FilterType.FilterType_SurfaceTexture;
            }
        });
    }

    ZegoEngineConfig zegoEngineConfig = new ZegoEngineConfig();
    public void onClickLoginRoomAndPublish(View view) {
        String roomID = binding.edRoomId.getText().toString();
        if (!"".equals(roomID)) {
            ZegoExpressEngine.setEngineConfig(zegoEngineConfig);
            // 跳转到创建并登录房间的页面
            FUBeautyActivity.actionStart(VideoFilterMainUI.this, roomID, mFilterType);
        } else {
            Toast.makeText(VideoFilterMainUI.this, "room id is no null", Toast.LENGTH_SHORT).show();
            AppLogger.getInstance().i("room id is no null");
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 供其他Activity调用，进入本专题的方法
     * For other activities to call into the methods of this topic
     * @param activity
     */
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, VideoFilterMainUI.class);
        activity.startActivity(intent);
    }

    // 前处理传递数据类型的描述
    // Preprocessing passes a description of the data type
    @Override
    public void onClick(View v) {
        int id = v.getId();
         if (id == R.id.memTexture2D_describe) {
            showPopWindows(getString(R.string.memTexture2D_describe), v);
        }
    }

    /**
     * 显示描述窗口
     * Display description window
     * @param msg  显示内容 Display content
     * @param view
     */
    private void showPopWindows(String msg, View view) {

        new CustomPopWindow.PopupWindowBuilder(this)
                .enableBackgroundDark(true)
                .setBgDarkAlpha(0.7f)
                .create()
                .setMsg(msg)
                .showAsDropDown(view, 0, 20);
    }
}
