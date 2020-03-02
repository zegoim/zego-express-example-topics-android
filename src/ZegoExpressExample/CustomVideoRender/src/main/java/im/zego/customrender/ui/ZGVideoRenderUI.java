package im.zego.customrender.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.cc.customrender.R;

import java.util.Date;

import im.zego.common.GetAppIDConfig;
import im.zego.common.util.DeviceInfoManager;
import im.zego.customrender.videorender.VideoRenderHandler;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerMediaEvent;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class ZGVideoRenderUI extends Activity {
    private TextureView mPreView;
    private TextureView mPlayView;
    private TextView mErrorTxt;
    private Button mDealBtn;
    private Button mDealPlayBtn;
    private String userName;
    private String userID;

    private String mRoomID = "zgver_";
    private String mRoomName = "VideoExternalRenderDemo";
    private String mPlayStreamID = "";


    ZegoExpressEngine mSDKEngine;
    // 渲染类
    private VideoRenderHandler videoRenderer;
    private int chooseRenderType;
    private String mStreamID;
    public static String mainPublishChannel = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zgvideo_render);

        mPreView = (TextureView) findViewById(R.id.pre_view);
        mPlayView = (TextureView) findViewById(R.id.play_view);
        mErrorTxt = (TextView) findViewById(R.id.error_txt);
        mDealBtn = (Button) findViewById(R.id.publish_btn);
        mDealPlayBtn = (Button) findViewById(R.id.play_btn);

        // 获取已选的渲染类型
        chooseRenderType = getIntent().getIntExtra("RenderType", 0);

        // 获取设备唯一ID
        String deviceID = DeviceInfoManager.generateDeviceId(this);
        mRoomID += deviceID;
        mStreamID = mRoomID;
        mPlayStreamID = mStreamID;
        videoRenderer = new VideoRenderHandler();

        videoRenderer.init();

        mSDKEngine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, this.getApplication(), null);
        mSDKEngine.setCustomVideoRenderHandler(videoRenderer);

        mSDKEngine.setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR);
        mSDKEngine.addEventHandler(new IZegoEventHandler() {


            @Override
            public void onDebugError(int errorCode, String funcName, String info) {

            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode) {
                if (state == ZegoRoomState.CONNECTED) {

                    mSDKEngine.enableCamera(true);
                    ZegoCanvas zegoCanvas = new ZegoCanvas(null);
                    zegoCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
                    videoRenderer.addView(mainPublishChannel, mPreView);
                    ZegoVideoConfig zegoVideoConfig = new ZegoVideoConfig();
                    zegoVideoConfig.setCaptureResolution(360, 640);
                    zegoVideoConfig.setEncodeResolution(360, 640);

                    mSDKEngine.setVideoConfig(zegoVideoConfig);
                    mSDKEngine.startPreview(zegoCanvas);
                    mSDKEngine.startPublishing(mRoomID);
                } else if (state == ZegoRoomState.DISCONNECTED) {
                    mErrorTxt.setText("login room fail, err:" + errorCode);
                }
            }

            @Override
            public void onPlayerMediaEvent(String streamID, ZegoPlayerMediaEvent event) {
                if (event == ZegoPlayerMediaEvent.VIDEO_BREAK_OCCUR) {

                    mErrorTxt.setText("视频中断, 正在重连");

                } else if (event == ZegoPlayerMediaEvent.VIDEO_BREAK_RESUME) {

                    mErrorTxt.setText("");

                }
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode) {
                Log.e("", "onPlayerStateUpdate errorCode:" + errorCode + "===" + state + "===" + streamID);
            }
        });

        String randomSuffix = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
        userID = "user" + randomSuffix;
        userName = "user" + randomSuffix;

        ZegoUser zegoUser = new ZegoUser(userID, userName);

        mSDKEngine.loginRoom(mRoomID, zegoUser, new ZegoRoomConfig());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 释放渲染类
        videoRenderer.uninit();
        // 登出房间，去除推拉流回调监听，释放 ZEGO SDK
        mSDKEngine.logoutRoom(mRoomID);

        ZegoExpressEngine.destroyEngine();
    }

    // 处理推流相关操作
    public void dealPublishing(View view) {
        // 界面button==停止推流
        if (mDealBtn.getText().toString().equals("StopPublish")) {
            //停止预览，停止推流
            mSDKEngine.stopPreview();
            mSDKEngine.stopPublishing();

            //移除渲染视图
            videoRenderer.removeView(mainPublishChannel);

            mDealBtn.setText("StartPublish");

        } else {
            // 界面button==开始推流
            // 开启预览再开始推流

            mDealBtn.setText("StopPublish");

            // 外部渲染采用码流渲染类型时，推流时由 SDK 进行渲染。

            // 添加外部渲染视图
            videoRenderer.addView(mainPublishChannel, mPreView);
            ZegoCanvas zegoCanvas = new ZegoCanvas(null);
            zegoCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
            mSDKEngine.startPreview(zegoCanvas);
            mSDKEngine.startPublishing(mStreamID);
        }
    }

    // 处理拉流相关操作
    public void dealPlay(View view) {

        // 界面button==开始拉流
        if (mDealPlayBtn.getText().toString().equals("StartPlay") && !mPlayStreamID.equals("")) {
            // 设置拉流视图

            // 选择的外部渲染类型不是未解码型，根据拉流流名设置渲染视图
            videoRenderer.addView(mPlayStreamID, mPlayView);

            // 开始拉流，不为 SDK 设置渲染视图，使用自渲染的视图
            mSDKEngine.startPlayingStream(mPlayStreamID, null);

            mErrorTxt.setText("");

            mDealPlayBtn.setText("StopPlay");

        } else {
            // 界面button==停止拉流
            if (!mPlayStreamID.equals("")) {
                //停止拉流
                mSDKEngine.stopPlayingStream(mPlayStreamID);
                //移除外部渲染视图
                videoRenderer.removeView(mPlayStreamID);
                mErrorTxt.setText("");

                mDealPlayBtn.setText("StartPlay");

            }
        }
    }

}
