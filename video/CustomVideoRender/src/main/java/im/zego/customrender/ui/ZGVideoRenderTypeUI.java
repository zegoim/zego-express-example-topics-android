package im.zego.customrender.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cc.customrender.R;

import im.zego.common.ui.BaseActivity;
import im.zego.common.util.SettingDataUtil;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormatSeries;
import im.zego.zegoexpress.entity.ZegoCustomVideoRenderConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;

/**
 * 外部渲染返回视频数据的类型选择
 */

/**
 * Type selection of video data returned by external rendering
 */
public class ZGVideoRenderTypeUI extends BaseActivity {

    private RadioGroup mRenderTypeGroup;


    // 是否已开启外部渲染
    // Whether external rendering is enabled
    private boolean isEnableExternalRender = false;


    private ZegoCustomVideoRenderConfig zegoCustomVideoRenderConfig = new ZegoCustomVideoRenderConfig();

    // 加载c++ so
    static {
        System.loadLibrary("nativeCutPlane");
    }

    public static void actionStart(Activity mainActivity) {
        Intent intent = new Intent(mainActivity, ZGVideoRenderTypeUI.class);
        mainActivity.startActivity(intent);
    }

    CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_render_type);

        ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);


        mCheckBox = (CheckBox) findViewById(R.id.checkboxNotDecode);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isGivenDecodeCallback = true;
                } else {
                    isGivenDecodeCallback = false;
                }
            }
        });
        mRenderTypeGroup = (RadioGroup) findViewById(R.id.RenderTypeGroup);
        final int[] radioRenderTypeBtns = {R.id.RadioDecodeRGB, R.id.RadioDecodeYUV};

        // 设置RadioGroup组件的事件监听
        mRenderTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {

                if (radioRenderTypeBtns[0] == radioGroup.getCheckedRadioButtonId()) {
                    // 外部渲染时抛出rgb格式的视频数据
                    // Rgb format video data is thrown during external rendering
                    zegoCustomVideoRenderConfig.frameFormatSeries = ZegoVideoFrameFormatSeries.RGB;
                    zegoCustomVideoRenderConfig.bufferType = ZegoVideoBufferType.RAW_DATA;
                } else if (radioRenderTypeBtns[1] == radioGroup.getCheckedRadioButtonId()) {
                    // 外部渲染时抛出I420格式的视频数据
                    // Throws I420 format video data during external rendering
                    zegoCustomVideoRenderConfig.frameFormatSeries = ZegoVideoFrameFormatSeries.YUV;
                    zegoCustomVideoRenderConfig.bufferType = ZegoVideoBufferType.RAW_DATA;
                }
                // 推流处开启外部采集功能
                // Turn on the external acquisition function
            }
        });

        RadioButton radioButton = (RadioButton) findViewById(R.id.RadioDecodeRGB);
        radioButton.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 若开启过视频外部渲染，此处关闭
        // If you have enabled external video rendering, turn it off here
        if (isEnableExternalRender) {
            // 关闭外部渲染功能
            // Turn off external rendering
            ZegoExpressEngine.setEngineConfig(null);
        }
    }

    private boolean isGivenDecodeCallback = false;

    public void JumpPublish(View view) {
        if (isGivenDecodeCallback) {
            if (Build.VERSION.SDK_INT > 19) {
                zegoCustomVideoRenderConfig.bufferType = ZegoVideoBufferType.ENCODED_DATA;
                zegoCustomVideoRenderConfig.enableEngineRender = true;
            } else {
                Toast.makeText(this, R.string.no_supported_decodetx, Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 开启外部渲染功能
        // Turn on external rendering
        isEnableExternalRender = true;
        ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        ZegoExpressEngine.getEngine().enableCustomVideoRender(true, zegoCustomVideoRenderConfig);
        Intent intent = new Intent(ZGVideoRenderTypeUI.this, ZGVideoRenderUI.class);
        intent.putExtra("IsUseNotDecode", isGivenDecodeCallback);
        intent.putExtra("RenderType", zegoCustomVideoRenderConfig.frameFormatSeries.value());
        ZGVideoRenderTypeUI.this.startActivity(intent);
    }
}
