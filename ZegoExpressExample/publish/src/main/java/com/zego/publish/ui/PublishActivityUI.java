package com.zego.publish.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.zego.common.entity.OperationInfo;
import com.zego.common.entity.SDKConfigInfo;
import com.zego.common.entity.StreamQuality;
import com.zego.common.util.DeviceInfoManager;
import com.zego.publish.R;
import com.zego.publish.databinding.ActivityPublishBinding;
import com.zego.common.ui.BaseActivity;
import com.zego.common.util.AppLogger;
import com.zego.zegoexpress.callback.IZegoEventHandler;
import com.zego.zegoexpress.constants.ZegoPublisherFirstFrameEvent;
import com.zego.zegoexpress.constants.ZegoPublisherState;
import com.zego.zegoexpress.constants.ZegoRoomState;
import com.zego.zegoexpress.constants.ZegoViewMode;
import com.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import com.zego.zegoexpress.entity.ZegoUser;
import com.zego.zegoexpress.entity.ZegoCanvas;

import java.util.Date;

public class PublishActivityUI extends BaseActivity {

    private ActivityPublishBinding binding;
    private OperationInfo operationInfo = new OperationInfo();
    private SDKConfigInfo sdkConfigInfo = new SDKConfigInfo();
    private StreamQuality streamQuality = new StreamQuality();

    private String mStreamID;
    private String mRoomID;
    private String mUserID;

    private static TextureView previewView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_publish);
        binding.setOperation(operationInfo);
        binding.setQuality(streamQuality);

        mRoomID = getIntent().getStringExtra("roomID");
        mStreamID = getIntent().getStringExtra("streamID");
        previewView = binding.preview;
        // 保证摄像头/麦克风能继续使用，如果之前关闭了摄像头/麦克风，未退出模块（未反初始化SDK），重新推流摄像头/麦克风也是关闭状态
        InitSDKPublishActivityUI.zegoExpressEngine.enableCamera(true);
        InitSDKPublishActivityUI.zegoExpressEngine.enableMicrophone(true);

        operationInfo.setInitResult(getString(R.string.tx_init_success));

        // 添加推流回调事件监听
        InitSDKPublishActivityUI.zegoExpressEngine.addEventHandler(publisherCallback);


        // 开始预览
        ZegoCanvas canvas = new ZegoCanvas(binding.preview, ZegoViewMode.VIEW_MODE_ASPECT_FILL);
        InitSDKPublishActivityUI.zegoExpressEngine.startPreview(canvas);

//        String randomSuffix = "-" + new Date().getTime()%(new Date().getTime()/1000);
        // 生成唯一的 userID，此处采用设备 ID，实际应用中可与业务账号系统关联
        mUserID = DeviceInfoManager.generateDeviceId(this)/* + randomSuffix*/;
        ZegoUser zegoUser = new ZegoUser(mUserID, mUserID);

        streamQuality.setRoomID(String.format("RoomID : %s", mRoomID));
        // 登录房间
        InitSDKPublishActivityUI.zegoExpressEngine.loginRoom(mRoomID, zegoUser, null);

        // 开始推流
        streamQuality.setStreamID(String.format("StreamID : %s", mStreamID));

        InitSDKPublishActivityUI.zegoExpressEngine.startPublishing(mStreamID);

        operationInfo.setHandleStreamResult(getString(R.string.tx_publishing));

        // 监听摄像头开关
        binding.swCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    InitSDKPublishActivityUI.zegoExpressEngine.enableCamera(isChecked);
                }
            }
        });

        // 监听麦克风开关
        binding.swMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    InitSDKPublishActivityUI.zegoExpressEngine.enableMicrophone(isChecked);
                }
            }
        });
    }

    /**
     * 返回到demo主页面
     * @param view
     */
    public void goBackToInitSDKMainUI(View view) {
        //停止推流并退出房间
        release();
        super.finish();
    }

    public void goSetting(View view) {
        PublishSettingActivityUI.actionStart(this);
    }

    public static TextureView getPreviewView() {
        return previewView;
    }

    /**
     * 停止推流并退出房间
     */
    private void release() {
        // 停止预览和推流
        InitSDKPublishActivityUI.zegoExpressEngine.stopPreview();
        InitSDKPublishActivityUI.zegoExpressEngine.stopPublishing();
        // 退出房间
        InitSDKPublishActivityUI.zegoExpressEngine.logoutRoom(mRoomID);
        // 移除回调事件监听
        InitSDKPublishActivityUI.zegoExpressEngine.removeEventHandler(publisherCallback);
        AppLogger.getInstance().i(PublishActivityUI.class, "停止推流并退出房间：%s", mRoomID);
    }

    /**
     * 结束直播
     * @param view
     */
    public void onEndLive(View view) {
        // 停止推流并退出房间
        release();
        this.finish();
    }

    /**
     * 辅助其它activity跳转到此activity
     * @param activity
     * @param roomID
     * @param streamID
     */
    public static void actionStart(Activity activity, String roomID, String streamID) {
        Intent intent = new Intent(activity, PublishActivityUI.class);
        intent.putExtra("roomID", roomID);
        intent.putExtra("streamID", streamID);
        activity.startActivity(intent);
    }

//    public void goCodeDemo(View view) {
//        WebActivity.actionStart(this, "https://doc.zego.im/CN/209.html", getString(R.string.tx_publish_guide));
//    }

    /**
     * 房间、推流等相关回调事件
     */
    private IZegoEventHandler publisherCallback = new IZegoEventHandler() {
        @Override
        public void onDebugError(int errorCode, String funcName, String info) {
            super.onDebugError(errorCode, funcName, info);
        }

        /**
         * 房间状态回调
         * @param roomID 房间ID
         * @param state 房间状态
         * @param errCode 错误码
         */
        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errCode) {
            super.onRoomStateUpdate(roomID, state, errCode);
            // 房间状态更新回调
            if (state == ZegoRoomState.ROOM_STATE_CONNECTED) {
                operationInfo.setLoginResult(getString(R.string.tx_login_room_success));
                AppLogger.getInstance().i(PublishActivityUI.class, "登录房间成功，roomID: %s", roomID);
            } else if (state == ZegoRoomState.ROOM_STATE_DISCONNECTED && errCode != 0) {
                operationInfo.setLoginResult(getString(R.string.tx_login_room_failure));
                AppLogger.getInstance().e(PublishActivityUI.class, "登录房间失败，请检查网络，roomID: %s，err:%d", roomID, errCode);
                Toast.makeText(PublishActivityUI.this, "登录房间失败，请检查网络", Toast.LENGTH_SHORT).show();
            } else if (errCode == 63000001) {
                AppLogger.getInstance().i(PublishActivityUI.class, "被踢出房间，roomID: %s, userID: %s", roomID, mUserID);
                Toast.makeText(PublishActivityUI.this, "被踢出房间，请检查是否使用相同的UserID登录相同的房间", Toast.LENGTH_SHORT).show();
            } else if (state == ZegoRoomState.ROOM_STATE_CONNECTING) {
                operationInfo.setLoginResult(getString(R.string.tx_login_room_ing));
            }
        }

        /**
         * 推流状态回调
         * @param streamID 流ID
         * @param state 流状态
         * @param errCode 错误码
         */
        @Override
        public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errCode) {
            super.onPublisherStateUpdate(streamID, state, errCode);
            if (state == ZegoPublisherState.PUBLISHER_STATE_PUBLISHING) {
                operationInfo.setHandleStreamResult(getString(R.string.tx_publish_success));
                AppLogger.getInstance().i(PublishActivityUI.class, "推流成功, streamID : %s", streamID);
            } else if (state == ZegoPublisherState.PUBLISHER_STATE_NO_PUBLISH && errCode != 0) {
                operationInfo.setHandleStreamResult(getString(R.string.tx_publish_fail)+", err:"+errCode);
                AppLogger.getInstance().e(PublishActivityUI.class, "推流失败, streamID : %s， err:%d", streamID, errCode);
                Toast.makeText(PublishActivityUI.this, "推流失败，请检查网络", Toast.LENGTH_LONG).show();
            } else if (state == ZegoPublisherState.PUBLISHER_STATE_PUBLISH_REQUESTING && errCode == 1003020) {
                AppLogger.getInstance().e(PublishActivityUI.class, "网络中断引起的推流重试, streamID:%s, err:%d", streamID, errCode);
            }
        }

        /**
         * 推流质量回调，回调频率默认3秒一次
         * @param streamID
         * @param quality
         */
        @Override
        public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
            super.onPublisherQualityUpdate(streamID, quality);
//            Log.i("zego","**** 推流质量回调，width:"+quality.width+", height:"+quality.height);
            streamQuality.setFps(String.format("帧率: %f", quality.videoSendFPS));
            streamQuality.setBitrate(String.format("码率: %f kbs", quality.videoKBPS));
        }


        /**
         * 推流采集分辨率改变通知
         * @param width 视频宽
         * @param height 视频高
         */
        @Override
        public void onPublisherVideoSizeChanged(int width, int height) {
            super.onPublisherVideoSizeChanged(width, height);
            streamQuality.setResolution(String.format("分辨率: %dX%d", width, height));
            AppLogger.getInstance().i(PublishActivityUI.class,"[publisher]视频宽高发生变化,width*height=%d*%d", width, height);
        }

        /**
         * 设备出错信息回调
         * @param errorCode 错误码
         * @param deviceName 设备名称
         */
        @Override
        public void onDeviceError(int errorCode, String deviceName) {
            super.onDeviceError(errorCode, deviceName);
            if (errorCode != 0) {
                AppLogger.getInstance().e(PublishActivityUI.class, "device:%s error:%d", deviceName, errorCode);
            }
        }
    };
}
