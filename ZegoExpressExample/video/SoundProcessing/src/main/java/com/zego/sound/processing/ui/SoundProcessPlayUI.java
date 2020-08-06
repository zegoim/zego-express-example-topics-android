package com.zego.sound.processing.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.zego.sound.processing.R;
import com.zego.sound.processing.databinding.ActivitySoundProcessPlayBinding;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.common.entity.StreamQuality;
import im.zego.common.ui.BaseActivity;
import im.zego.common.util.AppLogger;
import im.zego.common.util.SettingDataUtil;
import im.zego.common.widgets.CustomDialog;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

/**
 * Created by zego on 2019/4/22.
 *
 * 变声/混响/立体声 拉流页面，方便开发者听到的变声效果
 */

public class SoundProcessPlayUI extends BaseActivity {


    private ActivitySoundProcessPlayBinding binding;
    private ZegoExpressEngine zegoExpressEngine;

    private String mStreamID, mRoomID;
    private StreamQuality streamQuality = new StreamQuality();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound_process_play);
        // 利用DataBinding 可以通过bean类驱动UI变化。
        // 方便快捷避免需要写一大堆 setText 等一大堆臃肿的代码。
        binding.setQuality(streamQuality);
        mStreamID = getIntent().getStringExtra("streamID");
        mRoomID =  getIntent().getStringExtra("roomID");

        streamQuality.setRoomID(String.format("RoomID : %s", mRoomID));

        initSDK();

        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                /**
                 * 拉流质量更新, 回调频率默认3秒一次
                 * 可通过 {@link com.zego.zegoliveroom.ZegoLiveRoom#setPlayQualityMonitorCycle(long)} 修改回调频率
                 */
                streamQuality.setFps(String.format("帧率: %f", quality.videoRecvFPS));
                streamQuality.setBitrate(String.format("码率: %f kbs", quality.videoKBPS));
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    AppLogger.getInstance().i( "拉流成功, streamID : %s", streamID);
                    Toast.makeText(SoundProcessPlayUI.this, getString(R.string.tx_play_success), Toast.LENGTH_SHORT).show();

                    // 修改标题状态拉流成功状态
                    binding.title.setTitleName(getString(R.string.tx_playing));
                } else {
                    // 修改标题状态拉流失败状态
                    binding.title.setTitleName(getString(R.string.tx_play_fail));

                    AppLogger.getInstance().i( "拉流失败, streamID : %s, errorCode : %d", streamID, errorCode);
                    Toast.makeText(SoundProcessPlayUI.this, getString(R.string.tx_play_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
                // 视频宽高变化通知,startPlay后，如果视频宽度或者高度发生变化(首次的值也会)，则收到该通知.
                streamQuality.setResolution(String.format("分辨率: %dX%d", width, height));
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                // 关闭dialog
                CustomDialog.createDialog(SoundProcessPlayUI.this).cancel();
                if (errorCode == 0) {
                    AppLogger.getInstance().i( "登陆房间成功 roomId : %s", mRoomID);

                } else {
                    AppLogger.getInstance().i("登陆房间失败, errorCode : %d", errorCode);
                    Toast.makeText(SoundProcessPlayUI.this, getString(R.string.tx_login_room_failure), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {

                // 当登陆房间成功后，如果房间内中途有人推流或停止推流。房间内其他人就能通过该回调收到流更新通知。
                for (ZegoStream streamInfo : streamList) {
                    if (streamInfo.streamID.equals(mStreamID)) {
                        if (updateType == ZegoUpdateType.ADD) {
                            // 当收到房间流新增的时候, 重新拉流
                            ZegoExpressEngine.getEngine().startPlayingStream(mStreamID, new ZegoCanvas(binding.playView));

                        } else if (updateType == ZegoUpdateType.DELETE) {
                            // 当收到房间流删除的时候停止拉流
                            ZegoExpressEngine.getEngine().stopPlayingStream(mStreamID);
                            Toast.makeText(SoundProcessPlayUI.this, R.string.tx_current_stream_delete, Toast.LENGTH_LONG).show();
                            // 修改标题状态拉流成功状态
                            binding.title.setTitleName(getString(R.string.tx_current_stream_delete));
                        }
                    }
                }
            }
        });


        loginRoom();

    }

    /**
     * 初始化SDK逻辑
     */
    private void initSDK() {
        AppLogger.getInstance().i( "初始化zegoSDK");
        zegoExpressEngine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        AppLogger.getInstance().i("初始化zegoSDK成功");
    }

    @Override
    protected void onDestroy() {

        // 清空代理设置
        ZegoExpressEngine.getEngine().setEventHandler(null);

        // 停止所有的推流和拉流后，才能执行 logoutRoom
        if (mStreamID != null) {
            ZegoExpressEngine.getEngine().stopPlayingStream(mStreamID);

        }
        ZegoExpressEngine.getEngine().logoutRoom(mRoomID);
        // 当用户退出界面时退出登录房间
        super.onDestroy();
    }

    /**
     * 开始拉流
     */
    public void loginRoom() {

        CustomDialog.createDialog("登录房间中...", this).show();
        // 开始拉流前需要先登录房间
        ZegoExpressEngine.getEngine().loginRoom(mRoomID, new ZegoUser("", ""));
    }



    public static void actionStart(Activity activity, String roomID, String streamID) {
        Intent intent = new Intent(activity, SoundProcessPlayUI.class);
        intent.putExtra("roomID", roomID);
        intent.putExtra("streamID", streamID);
        activity.startActivity(intent);
    }
}
