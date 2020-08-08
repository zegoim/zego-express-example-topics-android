package com.zego.sound.processing.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import im.zego.common.entity.StreamQuality;
import im.zego.common.ui.BaseActivity;
import im.zego.common.util.AppLogger;
import im.zego.common.util.ZegoUtil;
import im.zego.common.widgets.CustomDialog;
import com.zego.sound.processing.R;
import com.zego.sound.processing.databinding.ActivitySoundProcessPublishBinding;
import com.zego.sound.processing.databinding.InputRoomIdLayoutBinding;
import com.zego.sound.processing.ui.SoundProcessPublishUI;
import com.zego.sound.processing.view.SoundEffectDialog;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import im.zego.common.ui.BaseActivity;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoUser;

/**
 * Created by zego on 2019/5/8.
 *
 * 推流相关逻辑都在这里。主要目的是为了让开发者更方便阅读变声/混响/立体声的代码。
 *
 */

public class SoundProcessPublishBaseUI extends BaseActivity {


    protected ActivitySoundProcessPublishBinding binding;
    protected InputRoomIdLayoutBinding layoutBinding;
    protected StreamQuality streamQuality = new StreamQuality();

    // 音效控制 Dialog
    protected SoundEffectDialog soundEffectDialog;

    protected String streamID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String randomSuffix = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
        userID = "user2" + randomSuffix;
        userName = "userName2" + randomSuffix;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound_process_publish);

        layoutBinding = binding.layout;
        // 利用DataBinding 可以通过bean类驱动UI变化。
        // 方便快捷避免需要写一大堆 setText 等一大堆臃肿的代码。
        binding.setQuality(streamQuality);

        // 初始化 SDK 回调代理
        initSDKCallback();

        // 调用sdk 开始预览接口 设置view 启用预览
        ZegoExpressEngine.getEngine().startPreview(new ZegoCanvas(binding.publishView));


    }

    /**
     * 懒加载TipDialog
     *
     * @return 返回页面公用的TipDialog
     */
    protected SoundEffectDialog getSoundEffectDialog() {
        if (soundEffectDialog == null) {
            soundEffectDialog = new SoundEffectDialog(this);
        }
        return soundEffectDialog;
    }


    protected void initSDKCallback() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                // 推流状态更新，errorCode 非0 则说明推流失败
                // 推流常见错误码请看文档: <a>https://doc.zego.im/CN/308.html</a>

                if (errorCode == 0) {
                    binding.title.setTitleName(getString(R.string.tx_publish_success));
                    AppLogger.getInstance().i("推流成功, streamID : %s", streamID);
                    Toast.makeText(SoundProcessPublishBaseUI.this, getString(R.string.tx_publish_success), Toast.LENGTH_SHORT).show();
                } else {
                    binding.title.setTitleName(getString(R.string.tx_publish_fail));
                    AppLogger.getInstance().i("推流失败, streamID : %s, errorCode : %d", streamID, errorCode);
                    Toast.makeText(SoundProcessPublishBaseUI.this, getString(R.string.tx_publish_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                /**
                 * 推流质量更新, 回调频率默认3秒一次
                 * 可通过 {@link com.zego.zegoliveroom.ZegoLiveRoom#setPublishQualityMonitorCycle(long)} 修改回调频率
                 */
                streamQuality.setFps(String.format("帧率: %f", quality.videoCaptureFPS));
                streamQuality.setBitrate(String.format("码率: %f kbs", quality.videoKBPS));

            }

            @Override
            public void onPublisherVideoSizeChanged(int width, int height, ZegoPublishChannel channel) {
                streamQuality.setResolution(String.format("分辨率: %dX%d", width, height));
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                CustomDialog.createDialog(SoundProcessPublishBaseUI.this).cancel();
                if (errorCode == 0) {
                    AppLogger.getInstance().i( "登陆房间成功 roomId : %s", roomID);

                    // 登陆房间成功，开始推流
                    startPublish(roomID);
                } else {
                    AppLogger.getInstance().i("登陆房间失败, errorCode : %d", errorCode);
                    Toast.makeText(SoundProcessPublishBaseUI.this, getString(R.string.tx_login_room_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String userID;
    private String userName;

    /**
     * Button点击事件
     * 确认推流
     *
     * @param view
     */
    public void onConfirmPublish(View view) {
        final String roomId = layoutBinding.edRoomId.getText().toString();
        if (!"".equals(roomId)) {
            CustomDialog.createDialog("登录房间中...", this).show();
            // 开始推流前需要先登录房间
            ZegoExpressEngine.getEngine().loginRoom(roomId, new ZegoUser(userID, userName));

        } else {
            Toast.makeText(SoundProcessPublishBaseUI.this, getString(R.string.tx_room_id_is_no_null), Toast.LENGTH_SHORT).show();
            AppLogger.getInstance().i( getString(R.string.tx_room_id_is_no_null));
        }
    }


    // 开始推流
    protected void startPublish(String roomId) {
        streamID = ZegoUtil.getPublishStreamID();
        // 隐藏输入RoomID布局
        hideInputRoomIDLayout();

        // 更新界面RoomID 与 StreamID 信息
        streamQuality.setRoomID(String.format("roomID: %s", roomId));
        streamQuality.setStreamID(String.format("streamID: %s", streamID));

        // 开始推流 推流使用 JoinPublish 连麦模式，可降低延迟
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);


    }

    protected void hideInputRoomIDLayout() {
        // 隐藏InputStreamIDLayout布局
        layoutBinding.getRoot().setVisibility(View.GONE);
        binding.publishStateView.setVisibility(View.VISIBLE);
    }

    //  某些华为手机上，应用在后台超过2分钟左右，华为系统会把摄像头资源给释放掉，并且可能会断开你应用的网络连接
    //  关于后台会断开网络的问题可以通过在设置-应用-权限管理-菜单-特殊访问权限-电池优化，将设置成不允许使用电池优化，才能解决。
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSoundEffectDialog().release();
        // 停止所有的推流和拉流后，才能执行 logoutRoom
        ZegoExpressEngine.getEngine().stopPreview();
        ZegoExpressEngine.getEngine().stopPublishingStream();
        final String roomId = layoutBinding.edRoomId.getText().toString();
        ZegoExpressEngine.getEngine().logoutRoom(roomId);
    }
}
