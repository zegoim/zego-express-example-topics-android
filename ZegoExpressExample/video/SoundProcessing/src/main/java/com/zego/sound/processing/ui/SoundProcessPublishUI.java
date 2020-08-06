package com.zego.sound.processing.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zego.sound.processing.R;
import com.zego.sound.processing.adapter.SoundEffectViewAdapter;
import com.zego.sound.processing.base.SoundProcessPublishBaseUI;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoReverbParam;
import im.zego.zegoexpress.entity.ZegoVoiceChangerParam;

import static im.zego.zegoexpress.constants.ZegoAudioConfigPreset.STANDARD_QUALITY_STEREO;

/**
 * Created by zego on 2019/4/22.
 *
 */

public class SoundProcessPublishUI extends SoundProcessPublishBaseUI {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化视图回调代理
        initViewCallback();
    }

    ZegoReverbParam zegoReverbParam = new  ZegoReverbParam();
    ZegoVoiceChangerParam zegoVoiceChangerParam = new ZegoVoiceChangerParam();
    private void initViewCallback() {
        // 在推流前必须先打开SDK双声道才能使用虚拟立体声功能
        ZegoExpressEngine.getEngine().setAudioConfig(new ZegoAudioConfig(STANDARD_QUALITY_STEREO));
        getSoundEffectDialog().setOnSoundEffectAuditionCheckedListener(new SoundEffectViewAdapter.OnSoundEffectAuditionCheckedListener() {

            /**
             * 勾选了界面上的音效试听会触发此回调
             *
             * @param isChecked
             */
            @Override
            public void onSoundEffectAuditionChecked(boolean isChecked) {
                // 开启SDK耳返，此时可以听到自己的声音
                ZegoExpressEngine.getEngine().enableHeadphoneMonitor(isChecked);

                if (!isChecked) {
                    Toast.makeText(SoundProcessPublishUI.this, R.string.sound_effect_audition_close_tip, Toast.LENGTH_LONG).show();
                }
            }

        });

        // 设置变声控件变化监听器
        getSoundEffectDialog().setOnVoiceChangeListener(new SoundEffectViewAdapter.OnVoiceChangeListener() {
            @Override
            public void onVoiceChangeParam(float param) {
                zegoVoiceChangerParam.pitch = param;
                ZegoExpressEngine.getEngine().setVoiceChangerParam(zegoVoiceChangerParam);
            }
        });

        // 设置混响控件变化监听器
        getSoundEffectDialog().setOnReverberationChangeListener(new SoundEffectViewAdapter.OnReverberationChangeListener() {
            @Override
            public void onAudioReverbModeChange(boolean enable, ZegoReverbParam mode) {
                ZegoExpressEngine.getEngine().setReverbParam(mode);
            }

            @Override
            public void onRoomSizeChange(float param) {
                zegoReverbParam.roomSize = param;
                ZegoExpressEngine.getEngine().setReverbParam(zegoReverbParam);
            }

            @Override
            public void onDryWetRationChange(float param) {
                zegoReverbParam.dryWetRatio = param;
                /**
                 * 设置 SDK 干湿比
                 */
                ZegoExpressEngine.getEngine().setReverbParam(zegoReverbParam);
            }

            @Override
            public void onDamping(float param) {
                zegoReverbParam.damping = param;
                /**
                 * 设置 SDK 混响阻尼
                 */
                ZegoExpressEngine.getEngine().setReverbParam(zegoReverbParam);
            }

            @Override
            public void onReverberance(float param) {
                zegoReverbParam.reverberance = param;
                /**
                 * 设置 SDK 余响
                 */
                ZegoExpressEngine.getEngine().setReverbParam(zegoReverbParam);
            }
        });

        // 设置立体声角度变化监听器
        getSoundEffectDialog().setOnStereoChangeListener(new SoundEffectViewAdapter.OnStereoChangeListener() {
            @Override
            public void onStereoChangeParam(int param) {

                /**
                 * 设置 SDK 虚拟立体声
                 */
                ZegoExpressEngine.getEngine().enableVirtualStereo(true, param);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        // 恢复音效混响等默认设置, 避免在其他模块还会出现变声的效果
        // 恢复变声
        ZegoExpressEngine.getEngine().setReverbParam(new ZegoReverbParam());
        ZegoExpressEngine.getEngine().enableVirtualStereo(false, 0);
        ZegoExpressEngine.getEngine().setAudioConfig(new ZegoAudioConfig());
        ZegoExpressEngine.getEngine().setEventHandler(null);
        // 关闭耳返
        ZegoExpressEngine.getEngine().enableHeadphoneMonitor(false);
    }



    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SoundProcessPublishUI.class);
        activity.startActivity(intent);
    }

    /**
     * Button 点击事件
     * 音效处理
     *
     * @param view
     */
    public void onSoundProcess(View view) {
        getSoundEffectDialog().show();
    }

}
