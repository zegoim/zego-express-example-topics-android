package com.zego.play.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.zego.common.application.ZegoApplication;
import com.zego.common.util.AppLogger;
import com.zego.common.util.ZegoUtil;
import com.zego.common.widgets.CustomPopWindow;
import com.zego.play.R;
import com.zego.common.ui.BaseActivity;
import com.zego.play.databinding.ActivityPlayInitSdkBinding;
import com.zego.zegoexpress.ZegoExpressEngine;
import com.zego.zegoexpress.constants.ZegoScenario;

/**
 * Created by zego on 2019/3/19.
 */

public class InitSDKPlayActivityUI extends BaseActivity implements View.OnClickListener {

    ActivityPlayInitSdkBinding binding;

    public static ZegoExpressEngine zegoExpressEngine = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_init_sdk);
        binding.roomIDDescribe.setOnClickListener(this);
        binding.streamIDDescribe.setOnClickListener(this);

        /**
         * 创建 SDK 实例
         * 此处的 AppID 和 AppSign 是从即构官网申请的
         * ZEGO Express SDK会根据场景选择最优配置，如果无对应场景，可选择 ZegoScenario.SCENARIO_GENERAL
         */
        zegoExpressEngine = ZegoExpressEngine.createEngine(ZegoUtil.getAppID(), ZegoUtil.getAppSign(), ZegoUtil.getIsTestEnv(), ZegoScenario.SCENARIO_GENERAL, ZegoApplication.zegoApplication, null);

        // 恢复 SDK 默认设置，硬解、开启扬声器等
        PlaySettingActivityUI.setDefaultPlayConfig();
    }

    public void goBackToMainUI(View view) {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        // 释放 ZegoExpressSDK 使用的所有资源
        ZegoExpressEngine.destroyEngine();
        super.onDestroy();
    }

    /**
     * button 点击事件
     * 登录房间
     *
     * @param view
     */
    public void onEnterLive(View view) {
        String roomID = binding.edRoomId.getText().toString();
        String streamID = binding.edStreamId.getText().toString();

        if (roomID.equals("") || streamID.equals("")) {
            AppLogger.getInstance().d(InitSDKPlayActivityUI.class, getString(com.zego.common.R.string.tx_id_cannot_null));
            Toast.makeText(InitSDKPlayActivityUI.this, getString(com.zego.common.R.string.tx_id_cannot_null), Toast.LENGTH_SHORT).show();
            return;
        }

        if (zegoExpressEngine != null) {
            PlayActivityUI.actionStart(InitSDKPlayActivityUI.this, roomID, streamID);
        }
    }

    // 前处理传递数据类型的描述
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.roomID_describe) {
            showPopWindows(getString(R.string.room_id_describe), v);
        } else if (id == R.id.streamID_describe) {
            showPopWindows(getString(R.string.stream_id_describe), v);
        }
    }

    /**
     * 显示描述窗口
     *
     * @param msg  显示内容
     * @param view
     */
    private void showPopWindows(String msg, View view) {
        //创建并显示popWindow
        new CustomPopWindow.PopupWindowBuilder(this)
                .enableBackgroundDark(true) //弹出popWindow时，背景是否变暗
                .setBgDarkAlpha(0.7f) // 控制亮度
                .create()
                .setMsg(msg)
                .showAsDropDown(view, 0, 20);
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, InitSDKPlayActivityUI.class);
        activity.startActivity(intent);

    }

//    /**
//     * button 点击事件
//     * <p>
//     * 跳转到登陆房间指引webView
//     *
//     * @param view
//     */
//    public void goCodeDemo(View view) {
//        WebActivity.actionStart(this, "https://doc.zego.im/CN/625.html", getString(com.zego.common.R.string.tx_login_room_guide));
//    }
}
