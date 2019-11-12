package com.zego.play.ui;

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
import com.zego.common.entity.StreamQuality;
import com.zego.common.util.DeviceInfoManager;
import com.zego.play.R;
import com.zego.play.databinding.ActivityPlayBinding;
import com.zego.common.ui.BaseActivity;
import com.zego.common.util.AppLogger;
import com.zego.zegoexpress.callback.IZegoEventHandler;
import com.zego.zegoexpress.constants.ZegoPlayerEventReason;
import com.zego.zegoexpress.constants.ZegoPlayerFirstFrameEvent;
import com.zego.zegoexpress.constants.ZegoPlayerMediaEvent;
import com.zego.zegoexpress.constants.ZegoPlayerState;
import com.zego.zegoexpress.constants.ZegoRoomState;
import com.zego.zegoexpress.constants.ZegoUpdateType;
import com.zego.zegoexpress.constants.ZegoViewMode;
import com.zego.zegoexpress.entity.ZegoCanvas;
import com.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import com.zego.zegoexpress.entity.ZegoStream;
import com.zego.zegoexpress.entity.ZegoUser;

import java.util.ArrayList;
import java.util.Date;

public class PlayActivityUI extends BaseActivity {
    private ActivityPlayBinding binding;
    private OperationInfo operationInfo = new OperationInfo();
    private StreamQuality streamQuality = new StreamQuality();

    // 房间 ID
    private String mRoomID;
    // 流 ID
    private String mStreamID;
    // 用户 ID
    private String mUserID;

    // 拉流视图
    private static TextureView playView = null;
    private boolean isStartPlay = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play);

        // 界面上显示登录、拉流操作结果
        binding.setOperation(operationInfo);
        // 界面上显示流质量
        binding.setQuality(streamQuality);

        mRoomID = getIntent().getStringExtra("roomID");
        mStreamID = getIntent().getStringExtra("streamID");
        playView = binding.playView;

        // 保证音频输出设备能继续使用，如果之前关闭了音频输出设备，未退出模块（未反初始化SDK），重新推流音频输出设备也是关闭状态
        InitSDKPlayActivityUI.zegoExpressEngine.muteAudioOutput(false);

        operationInfo.setInitResult(getString(R.string.tx_init_success));

        // 扬声器开关的监听
        binding.swSpeaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    InitSDKPlayActivityUI.zegoExpressEngine.muteAudioOutput(!isChecked);
                }
            }
        });

        // 添加拉流回调事件监听
        InitSDKPlayActivityUI.zegoExpressEngine.addEventHandler(playerCallback);

//        String randomSuffix = "-" + new Date().getTime()%(new Date().getTime()/1000);
        // 生成唯一的 userID，此处采用设备 ID，实际应用中可与业务账号系统关联
        mUserID = DeviceInfoManager.generateDeviceId(this)/* + randomSuffix*/;
        ZegoUser zegoUser = new ZegoUser(mUserID, mUserID);

        streamQuality.setRoomID(String.format("RoomID : %s", mRoomID));
        // 登录房间
        InitSDKPlayActivityUI.zegoExpressEngine.loginRoom(mRoomID, zegoUser, null);

        // 开始拉流
        streamQuality.setStreamID(String.format("StreamID : %s", mStreamID));
        // 构造拉流渲染视图
        ZegoCanvas canvas = new ZegoCanvas(binding.playView, ZegoViewMode.VIEW_MODE_ASPECT_FILL);
        // 开始拉流
        InitSDKPlayActivityUI.zegoExpressEngine.startPlayingStream(mStreamID, canvas);
        isStartPlay = true;

        operationInfo.setHandleStreamResult(getString(R.string.tx_playing));
    }

    /**
     * 回到填写房间ID和流ID页面
     * @param view
     */
    public void goBackToInitSDKMainUI(View view) {
        // 停止拉流并退出房间
        release();
        super.finish();
    }

//    /**
//     * Button点击事件, 跳转官网示例代码链接
//     *
//     * @param view
//     */
//    public void goCodeDemo(View view) {
//        WebActivity.actionStart(this, "https://doc.zego.im/CN/217.html", getString(com.zego.common.R.string.tx_play_guide));
//
//    }

    /**
     * 停止拉流并退出房间
     */
    private void release() {
        // 停止拉流
        InitSDKPlayActivityUI.zegoExpressEngine.stopPlayingStream(mStreamID);
        // 退出房间
        InitSDKPlayActivityUI.zegoExpressEngine.logoutRoom(mRoomID);
        // 移除事件监听
        InitSDKPlayActivityUI.zegoExpressEngine.removeEventHandler(playerCallback);

        AppLogger.getInstance().i(PlayActivityUI.class, "停止拉流并退出房间：%s", mRoomID);
    }

    /**
     * button 点击事件触发
     * 离开直播
     *
     * @param view
     */
    public void onLeaveLive(View view) {
        // 停止拉流并退出房间
        release();
        this.finish();
    }

    /**
     * 跳转到常用界面
     *
     * @param view
     */
    public void goSetting(View view) {
        PlaySettingActivityUI.actionStart(this, mStreamID);
    }

    public static TextureView getPlayView() {
        return playView;
    }

    /**
     * 辅助其它activity跳转到此activity
     * @param activity
     * @param roomID 房间ID
     * @param streamID 流ID
     */
    public static void actionStart(Activity activity, String roomID, String streamID) {
        Intent intent = new Intent(activity, PlayActivityUI.class);
        intent.putExtra("roomID", roomID);
        intent.putExtra("streamID", streamID);
        activity.startActivity(intent);
    }

    // 登录房间、拉流等相关回调事件
    private IZegoEventHandler playerCallback = new IZegoEventHandler() {
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
                AppLogger.getInstance().i(PlayActivityUI.class, "登录房间：%s 成功", roomID);
            } else if (state == ZegoRoomState.ROOM_STATE_DISCONNECTED && errCode != 0) {
                operationInfo.setLoginResult(getString(R.string.tx_login_room_failure));
                AppLogger.getInstance().e(PlayActivityUI.class, "登录房间失败，请检查网络，roomID: %s, err:%d", roomID, errCode);
                Toast.makeText(PlayActivityUI.this, "登录房间失败，请检查网络", Toast.LENGTH_SHORT).show();
            } else if (errCode == 63000001) {
                AppLogger.getInstance().i(PlayActivityUI.class, "被踢出房间，roomID: %s, userID: %s", roomID, mUserID);
                Toast.makeText(PlayActivityUI.this, "被踢出房间，请检查是否使用相同的UserID登录相同的房间", Toast.LENGTH_SHORT).show();
            } else if (state == ZegoRoomState.ROOM_STATE_CONNECTING) {
                operationInfo.setLoginResult(getString(R.string.tx_login_room_ing));
            }
        }

        /**
         * 流新增/删除回调
         * @param updateType 更新类型，新增/删除
         * @param streamList 对应更新类型的流列表
         * @param roomID 房间ID
         */
        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
            AppLogger.getInstance().i(PlayActivityUI.class, "流更新通知，type:%d, roomID:%s", updateType.value(), roomID);
            if (updateType == ZegoUpdateType.UPDATE_TYPE_ADD) {
                for (ZegoStream zegoStream:streamList) {
                    if (zegoStream.streamId.equals(mStreamID)) {
                        if (!isStartPlay) {
                            // 构造拉流渲染视图
                            ZegoCanvas canvas = new ZegoCanvas(binding.playView, ZegoViewMode.VIEW_MODE_ASPECT_FILL);
                            // 开始拉流
                            InitSDKPlayActivityUI.zegoExpressEngine.startPlayingStream(zegoStream.streamId, canvas);

                            isStartPlay = true;
                            operationInfo.setHandleStreamResult(getString(R.string.tx_playing));
                        }
                    }
                }
                if (!isStartPlay) {
                    AppLogger.getInstance().d(PlayActivityUI.class, "%s 房间中不存在流：%s", roomID, mStreamID);
                    Toast.makeText(PlayActivityUI.this, "当前房间中不存在此条流", Toast.LENGTH_SHORT).show();
                }
            } else if (updateType == ZegoUpdateType.UPDATE_TYPE_DEL) {
                for (ZegoStream zegoStream : streamList) {
                    if (zegoStream.streamId.equals(mStreamID)) {

                        // 设置拉流渲染视图
                        InitSDKPlayActivityUI.zegoExpressEngine.stopPlayingStream(zegoStream.streamId);
                        isStartPlay = false;

                        operationInfo.setHandleStreamResult(getString(R.string.tx_playing));
                        AppLogger.getInstance().i(PlayActivityUI.class, "房间:%s 中的流: %s 停止直播", roomID, zegoStream.streamId);
                        Toast.makeText(PlayActivityUI.this, "此流已停推", Toast.LENGTH_SHORT).show();
                        operationInfo.setHandleStreamResult(getString(R.string.tx_stop_play));
                    }
                }
            }
        }


        /**
         * 拉流状态回调
         * @param streamID 流名
         * @param state 拉流状态
         * @param errCode 错误码
         */
        @Override
        public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errCode) {
            super.onPlayerStateUpdate(streamID, state, errCode);

            if (state == ZegoPlayerState.PLAYER_STATE_PLAYING) {
                operationInfo.setHandleStreamResult(getString(R.string.tx_play_success));
                AppLogger.getInstance().i(PlayActivityUI.class, "拉流成功，streamID:%s", streamID);
            } else if (state == ZegoPlayerState.PLAYER_STATE_NO_PLAY && errCode != 0) {
                isStartPlay = false;
                AppLogger.getInstance().e(PlayActivityUI.class, "拉流失败, streamID : %s, err: %d", streamID, errCode);
                Toast.makeText(PlayActivityUI.this, "拉取流:"+streamID+" 失败,err:"+errCode, Toast.LENGTH_SHORT).show();

                operationInfo.setHandleStreamResult(getString(R.string.tx_play_fail)+", err:"+errCode);
            } else if (state == ZegoPlayerState.PLAYER_STATE_PLAY_REQUESTING && errCode == 1004020) {
                AppLogger.getInstance().d(PlayActivityUI.class, "拉流重试中, streamID : %s, err: %d", streamID, errCode);
            }
        }

        /**
         * 拉流质量回调，回调频率默认3秒一次
         * @param streamID 流名
         * @param quality 拉流质量
         */
        @Override
        public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
            super.onPlayerQualityUpdate(streamID, quality);
            streamQuality.setFps(String.format("帧率: %f", quality.videoDecodeFPS));
            streamQuality.setBitrate(String.format("码率: %f kbs", quality.videoKBPS));
        }

        /**
         * 拉流媒体事件回调
         * @param streamID 流名
         * @param mediaEvent 媒体事件
         * @param reason 事件触发原因
         */
        @Override
        public void onPlayerMediaEvent(String streamID, ZegoPlayerMediaEvent event) {
        }

        /**
         * 拉流首帧通知
         * @param streamID 流名
         * @param event 首帧事件、音频或者视频
         */
        @Override
        public void onPlayerRecvFirstFrameEvent(String streamID, ZegoPlayerFirstFrameEvent event) {
        }

        /**
         * 所拉流的分辨率发生变化通知
         * @param streamID 流名
         * @param width 视频宽
         * @param height 视频高
         */
        @Override
        public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
            super.onPlayerVideoSizeChanged(streamID, width, height);
            streamQuality.setResolution(String.format("分辨率: %dX%d", width, height));
            AppLogger.getInstance().i(PlayActivityUI.class, "[player]视频宽高发生变化，width*height=%d*%d", width, height);
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
                AppLogger.getInstance().e(PlayActivityUI.class, "device:%s error:%d", deviceName, errorCode);
            }
        }
    };
}
