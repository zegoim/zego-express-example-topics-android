package com.zego.publish.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zego.common.application.ZegoApplication;
import com.zego.common.util.AppLogger;
import com.zego.common.widgets.log.FloatingView;
import com.zego.publish.R;
import com.zego.zegoexpress.constants.ZegoResolution;
import com.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import com.zego.zegoexpress.constants.ZegoViewMode;
import com.zego.zegoexpress.entity.ZegoCanvas;
import com.zego.zegoexpress.entity.ZegoVideoConfig;

/**
 * Created by zego on 2019/3/21.
 */
public class PublishSettingActivityUI extends FragmentActivity {

    public static String SHARE_PREFERENCE_NAME = "publishSetting";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_setting);
    }

    public static class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private String[] stringArray;
        private ListPreference viewModeListPreference, resolutionListPreference, bitrateListPreference, fpsListPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /*
                SharedPreferences是android提供的一个轻型存储框架，以key-value的方式存取一些系统配置项，
                在本示例中用于SDK相关功能设置的存储
            */
            getPreferenceManager().setSharedPreferencesName(SHARE_PREFERENCE_NAME);
            getPreferenceManager().setSharedPreferencesMode(AppCompatActivity.MODE_PRIVATE);
            //从xml文件加载选项
            addPreferencesFromResource(R.xml.publish_setting_preference);
            stringArray = getResources().getStringArray(R.array.view_setting_describe);

            //注册配置变化事件监听
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            //使用存储的配置在配置界面上显示，如果没有配置过则使用默认值
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            viewModeListPreference = (ListPreference) findPreference("publish_view_mode");
            String mode = sharedPreferences.getString("publish_view_mode", "1");
            viewModeListPreference.setSummary(stringArray[Integer.parseInt(mode)]);
            resolutionListPreference = (ListPreference) findPreference("publish_resolution");
            resolutionListPreference.setSummary(sharedPreferences.getString("publish_resolution", "540x960"));
            bitrateListPreference = (ListPreference) findPreference("publish_bitrate");
            bitrateListPreference.setSummary(sharedPreferences.getString("publish_bitrate", "1200000"));
            fpsListPreference = (ListPreference) findPreference("publish_fps");
            fpsListPreference.setSummary(sharedPreferences.getString("publish_fps", "15"));
        }

        @Override
        public void onDestroy() {
            //取消配置变化事件监听
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onDestroy();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("publish_view_mode".equals(key)) {
                String viewModeStr = sharedPreferences.getString(key, "1");
                int viewMode = Integer.valueOf(viewModeStr);
                viewModeListPreference.setSummary(stringArray[viewMode]);

                if (PublishActivityUI.getPreviewView() != null) {
                    ZegoViewMode zegoViewMode = ZegoViewMode.VIEW_MODE_ASPECT_FILL;
                    switch (viewMode) {
                        case 0:
                            zegoViewMode = ZegoViewMode.VIEW_MODE_ASPECT_FIT;
                            break;
                        case 1:
                            break;
                        case 2:
                            zegoViewMode = ZegoViewMode.VIEW_MODE_SCALE_TO_FILL;
                            break;
                        default:
                            break;
                    }
                    //设置推流预览视图模式
                    ZegoCanvas canvas = new ZegoCanvas(PublishActivityUI.getPreviewView(), ZegoViewMode.VIEW_MODE_ASPECT_FILL);
                    InitSDKPublishActivityUI.zegoExpressEngine.updatePreviewView(canvas);
                }
            } else if ("publish_hardware_encode".equals(key)) {
                boolean enable = sharedPreferences.getBoolean(key, false);

                //启用硬编
                InitSDKPublishActivityUI.zegoExpressEngine.enableHardwareEncoder(enable);
            } else if ("publish_preview_mirror".equals(key)) {
                boolean enable = sharedPreferences.getBoolean(key, true);

                //启用预览镜像
                if (enable) {
                    InitSDKPublishActivityUI.zegoExpressEngine.setVideoMirrorMode(ZegoVideoMirrorMode.VIDEO_MIRROR_MODE_ONLY_PREVIEW_MIRROR);
                }
                else {
                    InitSDKPublishActivityUI.zegoExpressEngine.setVideoMirrorMode(ZegoVideoMirrorMode.VIDEO_MIRROR_MODE_NO_MIRROR);
                }
            } else if ("publish_front_facing_camera".equals(key)) {

                boolean enable = sharedPreferences.getBoolean(key, true);

                //启用前置摄像头
                InitSDKPublishActivityUI.zegoExpressEngine.useFrontCamera(enable);
            } else if (("publish_resolution".equals(key)) || ("publish_bitrate".equals(key)) ||
                    ("publish_fps".equals(key))) {
                String resolution = sharedPreferences.getString("publish_resolution", "540x960");
                resolutionListPreference.setSummary(resolution);
                String[] resolutions = resolution.split("x");
                String bitrate = sharedPreferences.getString("publish_bitrate", "1200000");
                String fps = sharedPreferences.getString("publish_fps", "15");

                // 设置视频配置
                ZegoVideoConfig videoConfig = new ZegoVideoConfig(ZegoResolution.RESOLUTION_540x960);
                videoConfig.setCaptureResolution(Integer.parseInt(resolutions[0]), Integer.parseInt(resolutions[1]));
                videoConfig.setEncodeResolution(Integer.parseInt(resolutions[0]), Integer.parseInt(resolutions[1]));
                videoConfig.setVideoFPS(Integer.parseInt(fps));
                videoConfig.setVideoBitrate(Integer.parseInt(bitrate));
                InitSDKPublishActivityUI.zegoExpressEngine.setVideoConfig(videoConfig);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //在应用内实现悬浮窗
        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在应用内实现悬浮窗
        FloatingView.get().detach(this);
    }

    /**
     * 清空推流配置
     */
    public static void setDefaultPublishConfig() {

        SharedPreferences sharedPreferences = ZegoApplication.zegoApplication.getSharedPreferences(SHARE_PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        if (InitSDKPublishActivityUI.zegoExpressEngine != null) {
            AppLogger.getInstance().i(PublishActivityUI.class, "推流常用功能恢复SDK默认配置");
            //调用SDK接口，恢复SDK默认值设置
            InitSDKPublishActivityUI.zegoExpressEngine.enableHardwareEncoder(false);
            InitSDKPublishActivityUI.zegoExpressEngine.useFrontCamera(true);
            InitSDKPublishActivityUI.zegoExpressEngine.enableMicrophone(true);
            InitSDKPublishActivityUI.zegoExpressEngine.enableCamera(true);
            ZegoVideoConfig videoConfig = new ZegoVideoConfig(ZegoResolution.RESOLUTION_540x960);
            InitSDKPublishActivityUI.zegoExpressEngine.setVideoConfig(videoConfig);
            InitSDKPublishActivityUI.zegoExpressEngine.setVideoMirrorMode(ZegoVideoMirrorMode.VIDEO_MIRROR_MODE_NO_MIRROR);
        }
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, PublishSettingActivityUI.class);
        activity.startActivity(intent);
    }

}