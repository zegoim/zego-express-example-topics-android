package com.zego.sound.processing.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.zego.sound.processing.R;

import im.zego.common.ui.BaseActivity;
import im.zego.common.util.SettingDataUtil;
import im.zego.zegoexpress.ZegoExpressEngine;

public class SoundProcessingMainUI extends BaseActivity {

    public static void actionStart(Activity mainActivity) {
        Intent intent = new Intent(mainActivity, SoundProcessingMainUI.class);
        mainActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_main_sound_process);
        ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
    }

    /**
     * 发起推流的 Button 点击事件
     *
     * @param view
     */
    public void startPublish(View view) {

        // 必须初始化SDK完成才能进行以下操作

        SoundProcessPublishUI.actionStart(this);

    }
}
