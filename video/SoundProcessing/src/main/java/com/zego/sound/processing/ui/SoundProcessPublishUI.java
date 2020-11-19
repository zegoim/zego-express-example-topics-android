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
import im.zego.zegoexpress.constants.ZegoReverbPreset;
import im.zego.zegoexpress.constants.ZegoVoiceChangerPreset;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoReverbAdvancedParam;
import im.zego.zegoexpress.entity.ZegoReverbEchoParam;
import im.zego.zegoexpress.entity.ZegoReverbParam;
import im.zego.zegoexpress.entity.ZegoVoiceChangerParam;

import static im.zego.zegoexpress.constants.ZegoAudioConfigPreset.STANDARD_QUALITY_STEREO;

/**
 * Created by zego on 2019/4/22.
 */

public class SoundProcessPublishUI extends SoundProcessPublishBaseUI {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化视图回调代理
        initViewCallback();
    }

    ZegoReverbAdvancedParam zegoReverbAdvancedParam = new ZegoReverbAdvancedParam();
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

            @Override
            public void onVoiceChangePreset(ZegoVoiceChangerPreset mode) {
                ZegoExpressEngine.getEngine().setVoiceChangerPreset(mode);

            }
        });

        // 设置混响控件变化监听器
        getSoundEffectDialog().setOnReverberationChangeListener(new SoundEffectViewAdapter.OnReverberationChangeListener() {
            @Override
            public void onAudioReverbModeChange(boolean enable, ZegoReverbPreset mode) {
                ZegoExpressEngine.getEngine().setReverbPreset(mode);
            }

            @Override
            public void onRoomSizeChange(float param) {
                zegoReverbAdvancedParam.roomSize = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);
            }

            @Override
            public void onDryWetRationChange(float param) {

            }

            @Override
            public void onDamping(float param) {
                zegoReverbAdvancedParam.damping = param;
                /**
                 * 设置 SDK 混响阻尼
                 */
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);
            }

            @Override
            public void onReverberance(float param) {
                zegoReverbAdvancedParam.reverberance = param;
                /**
                 * 设置 SDK 余响
                 */
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);
            }

            @Override
            public void wetOnly(boolean enable) {
                zegoReverbAdvancedParam.wetOnly = enable;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);
            }

            @Override
            public void wetGain(float param) {
                zegoReverbAdvancedParam.wetGain = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);
            }

            @Override
            public void dryGain(float param) {
                zegoReverbAdvancedParam.dryGain = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);

            }

            @Override
            public void toneLow(float param) {
                zegoReverbAdvancedParam.toneLow = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);

            }

            @Override
            public void toneHigh(float param) {
                zegoReverbAdvancedParam.toneHigh = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);

            }

            @Override
            public void preDelay(float param) {
                zegoReverbAdvancedParam.preDelay = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);

            }

            @Override
            public void stereoWidth(float param) {
                zegoReverbAdvancedParam.stereoWidth = param;
                ZegoExpressEngine.getEngine().setReverbAdvancedParam(zegoReverbAdvancedParam);

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
        getSoundEffectDialog().setOnReverberationEchoListener(new SoundEffectViewAdapter.OnReverberationEchoListener() {
            @Override
            public void onReverbEchoModeChange(ZegoReverbEchoParam mode) {
                ZegoExpressEngine.getEngine().setReverbEchoParam(mode);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        // 恢复音效混响等默认设置, 避免在其他模块还会出现变声的效果
        // 恢复变声
        ZegoExpressEngine.getEngine().setReverbAdvancedParam(new ZegoReverbAdvancedParam());
        ZegoExpressEngine.getEngine().enableVirtualStereo(false, 0);
        ZegoExpressEngine.getEngine().setAudioConfig(new ZegoAudioConfig());
        ZegoExpressEngine.getEngine().setEventHandler(null);
        // 关闭耳返
        ZegoExpressEngine.getEngine().enableHeadphoneMonitor(false);
        ZegoExpressEngine.getEngine().setVoiceChangerPreset(ZegoVoiceChangerPreset.NONE);
        ZegoExpressEngine.getEngine().setReverbEchoParam(getReverbEchoParamNone());
        ZegoExpressEngine.getEngine().setVoiceChangerParam(new ZegoVoiceChangerParam());
    }

    private ZegoReverbEchoParam getReverbEchoParamNone() {
        ZegoReverbEchoParam echoParam = new ZegoReverbEchoParam();
        echoParam.inGain = 1;
        echoParam.outGain = 1;
        echoParam.numDelays = 0;
        for (int i = 0; i < 7; i++) {
            echoParam.delay[i] = 0;
        }
        for (int i = 0; i < 7; i++) {
            echoParam.decay[i] = 0;
        }
        return echoParam;
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
