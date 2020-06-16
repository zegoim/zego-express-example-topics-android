package im.zego.express.mixing.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import com.zego.mixing.R;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import im.zego.common.ui.BaseActivity;
import im.zego.common.util.SettingDataUtil;
import im.zego.express.mixing.ZGMixingDemo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoAudioMixingHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.entity.ZegoAudioMixingData;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class ZGMixingDemoUI extends BaseActivity {

    private CheckBox mAuxCheckBox;
    private TextView mAuxTxt;
    private TextView mErrorTxt;
    private TextView mHintTxt;
    private Button mPublishBtn;
    private TextureView mPreview;
    private String userID;
    private String userName;
    private String mRoomID = "ZEGO_TOPIC_MIXING";
    private boolean isLoginRoomSuccess = false;

    private String mMP3FilePath = "";
    private String mPCMFilePath = "";
    private Thread convertThread = null;

    private String hintStr = "! 推流后请用另一个设备进入“拉流”功能，登录“ZEGO_TOPIC_MIXING”房间，填写“ZEGO_TOPIC_MIXING”流名来查看该流";

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, ZGMixingDemoUI.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zgmixing);

        mAuxTxt = (TextView) findViewById(R.id.aux_txt);
        mAuxCheckBox = (CheckBox) findViewById(R.id.CheckboxAux);
        mPreview = (TextureView) findViewById(R.id.pre_view);
        mErrorTxt = (TextView) findViewById(R.id.error_txt);
        mHintTxt = (TextView) findViewById(R.id.hint);
        mPublishBtn = (Button) findViewById(R.id.publish_btn);

        String randomSuffix = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
        userID = "user" + randomSuffix;
        userName = "user" + randomSuffix;

        String dirPath = this.getExternalCacheDir().getPath();
        mPCMFilePath = dirPath + "/mixdemo.pcm";
        mMP3FilePath = ZGMixingDemo.sharedInstance().getPath(this, "road.mp3");

        // 设置如何查看混音效果的说明
        mHintTxt.setText(hintStr);
        // 获取mp3文件采样率，声道
        ZGMixingDemo.sharedInstance().getMP3FileInfo(mMP3FilePath);

        // 生成pcm数据文件
        File file = new File(mPCMFilePath);
        if (!file.exists()) {
            mAuxTxt.setVisibility(View.INVISIBLE);
            mAuxCheckBox.setVisibility(View.INVISIBLE);
            convertThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ZGMixingDemo.sharedInstance().MP3ToPCM(mMP3FilePath, mPCMFilePath);
                    runOnUiThread(() -> {
                        mAuxTxt.setVisibility(View.VISIBLE);
                        mAuxCheckBox.setVisibility(View.VISIBLE);
                    });
                }
            });
            convertThread.start();
        }

        mAuxCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (ZegoExpressEngine.getEngine() == null) {
                    return;
                }
                // 是否启用混音
                if (checked) {
                    ZegoExpressEngine.getEngine().enableAudioMixing(true);
                } else {
                    ZegoExpressEngine.getEngine().enableAudioMixing(false);
                }
            }
        });

        ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        // join room
        ZegoExpressEngine.getEngine().loginRoom(mRoomID, new ZegoUser(userID, userName));

        ZegoExpressEngine.getEngine().setAudioMixingHandler(new IZegoAudioMixingHandler() {
            @Override
            public ZegoAudioMixingData onAudioMixingCopyData(int expectedDataLength) {
                return ZGMixingDemo.sharedInstance().handleAuxCallback(mPCMFilePath, expectedDataLength);
            }
        });

        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                if (state == ZegoRoomState.CONNECTED && errorCode == 0) {
                    isLoginRoomSuccess = true;
                    ZegoExpressEngine.getEngine().startPreview(new ZegoCanvas(mPreview));
                } else if (state == ZegoRoomState.DISCONNECTED) {
                    mErrorTxt.setText("login room fail, err: " + errorCode);
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    mPublishBtn.setText(getString(R.string.tx_stoppublish));
                } else {
                    mErrorTxt.setText("publish fail err: " + errorCode);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        convertThread = null;

        if (isLoginRoomSuccess) {
            ZegoExpressEngine.getEngine().setAudioMixingHandler(null);
            ZegoExpressEngine.getEngine().setEventHandler(null);
            ZegoExpressEngine.getEngine().logoutRoom(mRoomID);
        }

        ZegoExpressEngine.destroyEngine(null);
    }

    public void dealPublish(View view) {
        if (isLoginRoomSuccess) {
            if (mPublishBtn.getText().toString().equals(getString(R.string.tx_startpublish))) {

                // 设置预览
                ZegoExpressEngine.getEngine().startPreview(new ZegoCanvas(mPreview));

                // 推流
                ZegoExpressEngine.getEngine().startPublishingStream(mRoomID);

            } else {
                // 停止推流
                ZegoExpressEngine.getEngine().stopPreview();
                ZegoExpressEngine.getEngine().stopPublishingStream();
                mPublishBtn.setText(getString(R.string.tx_startpublish));
            }
        }
    }


}
