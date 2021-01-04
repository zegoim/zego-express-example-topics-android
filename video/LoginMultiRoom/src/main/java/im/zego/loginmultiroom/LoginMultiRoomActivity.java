package im.zego.loginmultiroom;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import im.zego.common.util.AppLogger;
import im.zego.common.util.SettingDataUtil;
import im.zego.common.widgets.log.FloatingView;
import im.zego.loginmultiroom.databinding.LoginMultiRoomBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBroadcastMessageCallback;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.constants.ZegoLanguage;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class LoginMultiRoomActivity extends Activity {
    private LoginMultiRoomBinding binding;
    private ZegoExpressEngine engine;
    private String userId;
    private String mainRoomId="multi_main";
    private String auxRoomId="multi_aux";
    private String extraRoomId=mainRoomId;
    private String broadRoomId=mainRoomId;
    private String fromRoomID;
    private String toRoomID;
    public static void actionStart(Activity activity) {

        Intent intent = new Intent(activity, LoginMultiRoomActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.login_multi_room);
        /** 申请权限 */
        /** Request permission */
        checkOrRequestPermission();

        /** 添加悬浮日志视图 */
        /** Add floating log view */
        FloatingView.get().add();
        /** 记录SDK版本号 */
        /** Record SDK version */
        AppLogger.getInstance().i("SDK version : %s", ZegoExpressEngine.getVersion());
        initView();
        createEngine();
    }

    private void initView() {
        binding.multiRoomId.setText("MainRoomId: "+mainRoomId+"\n  AuxRoomId:"+auxRoomId);
        userId="userId-"+String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
        binding.userId.setText("userName: "+userId+"\n userId:  "+userId);
        binding.extraRoomId.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.extra_multi_main) {
                    extraRoomId = mainRoomId;
                }else if(checkedId==R.id.extra_multi_aux){
                    extraRoomId=auxRoomId;
                }
            }
        });
        binding.broadRoomId.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.broad_main) {
                    broadRoomId = mainRoomId;
                }else if(checkedId==R.id.broad_aux){
                    broadRoomId=auxRoomId;
                }
            }
        });
    }

    private void createEngine() {
        engine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), getApplication(), null);
        AppLogger.getInstance().i("CreateEngine Success!!");
        engine.setDebugVerbose(true, ZegoLanguage.CHINESE);
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                super.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
                for(int i=0;i<roomExtraInfoList.size();i++) {
                    AppLogger.getInstance().i("onRoomExtraInfoUpdate"  + ", roomId = " + roomID + ", key = " + roomExtraInfoList.get(i).key+ ", value = " + roomExtraInfoList.get(i).value+", updateTime = " + roomExtraInfoList.get(i).updateTime+", userId = " + roomExtraInfoList.get(i).updateUser.userID);
                    binding.recvExtra.setText("ReceiveExtraInfo: \n"+"roomId:"+roomID+"\n"+"userId:"+roomExtraInfoList.get(i).updateUser.userID+"\n"+"message:"+roomExtraInfoList.get(i).value);

                }

            }
            /** 常用回调 */
            /** The following are callbacks frequently used */
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                /** 房间状态回调，在登录房间后，当房间状态发生变化（例如房间断开，认证失败等），SDK会通过该接口通知 */
                /** Room status update callback: after logging into the room, when the room connection status changes
                 * (such as room disconnection, login authentication failure, etc.), the SDK will notify through the callback
                 */
                AppLogger.getInstance().i("onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
                if (errorCode != 0) {
                    Toast.makeText(LoginMultiRoomActivity.this, String.format("login room fail, errorCode: %d", errorCode), Toast.LENGTH_LONG).show();
                    return;
                }
                if(state==ZegoRoomState.CONNECTED){
                    if(mainRoomId.equals(fromRoomID)){
                        mainRoomId= toRoomID;
                        binding.extraMultiMain.setText(mainRoomId);
                        binding.broadMain.setText(mainRoomId);
                    }
                    else if(auxRoomId.equals(fromRoomID)){
                        auxRoomId=toRoomID;
                        binding.extraMultiAux.setText(auxRoomId);
                        binding.broadAux.setText(auxRoomId);
                    }
                    binding.multiRoomId.setText("MainRoomId: "+mainRoomId+"\n  AuxRoomId:"+auxRoomId);
                }
                if(state==ZegoRoomState.DISCONNECTED&&roomID.equals(auxRoomId)){

                }
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                /** 房间状态更新，在登录房间后，当用户进入或退出房间，SDK会通过该接口通知 */
                /** User status is updated. After logging into the room, when a user is added or deleted in the room,
                 * the SDK will notify through this callback
                 */
                AppLogger.getInstance().i("onRoomUserUpdate: roomID = " + roomID + ", updateType = " + updateType);
                for (int i = 0; i < userList.size(); i++) {
                    AppLogger.getInstance().i("userID = " + userList.get(i).userID + ", userName = " + userList.get(i).userName);
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,JSONObject extendedData) {
                /** 流状态更新，在登录房间后，当房间内有新增或删除音视频流，SDK会通过该接口通知 */
                /** The stream status is updated. After logging into the room, when there is a new publish or delete of audio and video stream,
                 * the SDK will notify through this callback */
                AppLogger.getInstance().i("onRoomStreamUpdate: roomID = " + roomID + ", updateType = " + updateType);
                for (int i = 0; i < streamList.size(); i++) {
                    AppLogger.getInstance().i("streamID = " + streamList.get(i).streamID);
                }
            }

            @Override
            public void onDebugError(int errorCode, String funcName, String info) {
                /** 调试异常信息通知 */
                /** Printing debugging error information */
                AppLogger.getInstance().i("onDebugError: errorCode = " + errorCode + ", funcName = " + funcName + ", info = " + info);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                /** 在调用推流接口成功后，推流状态变更（例如由于网络中断引起的流状态异常），SDK会通过该接口通知 */
                /** After calling the stream publishing interface successfully, when the status of the stream changes,
                 * such as the exception of streaming caused by network interruption, the SDK will notify through this callback
                 */
                AppLogger.getInstance().i("onPublisherStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
            }


            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                /** 在调用拉流接口成功后，拉流状态变更（例如由于网络中断引起的流状态异常），SDK会通过该接口通知 */
                /** After calling the streaming interface successfully, when the status of the stream changes,
                 * such as network interruption leading to abnormal situation, the SDK will notify through
                 * this callback */
                AppLogger.getInstance().i("onPlayerStateUpdate: streamID = " + streamID + ", state = " + state + ", errCode = " + errorCode);
            }

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
            }


            @Override
            public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
                super.onIMRecvBroadcastMessage(roomID, messageList);
                for(int i=0;i<messageList.size();i++) {
                    AppLogger.getInstance().i("onIMRecvBroadcastMessage: roomID = " + roomID + ", message = " + messageList.get(i).message + ", sendTime = " + messageList.get(i).sendTime+", userId = " + messageList.get(i).fromUser.userID);
                    binding.recvBroad.setText("ReceiveBroadcastMessage: \n"+"roomId:"+roomID+"\n"+"userId:"+messageList.get(i).fromUser.userID+"\n"+"message:"+messageList.get(i).message);
                }
            }
        });
    }

    public boolean checkOrRequestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
                return false;
            }
        }
        return true;
    }

    public void LoginMultiRoom(View view) {
        if(engine!=null){
             ZegoUser user=new ZegoUser(userId);
            //You must log in to the main room before logging in to the auxiliary room
            engine.loginRoom(mainRoomId,user);
            //Log in to the auxiliary room after logging in to the main room
            engine.loginMultiRoom(auxRoomId,null); }
    }

    public void LogOutMultiRoom(View view) {
        if(engine!=null){
            //must call logout aux room firstly
            engine.logoutRoom(auxRoomId);
            // Log out of the main room after logging out of the auxiliary room
            engine.logoutRoom(mainRoomId);
        }
    }

    public void startPublish(View view) {
        String streamId=binding.edMultiStreamId.getText().toString().trim();
        if(checkIsEmpty(streamId)){
            Toast.makeText(LoginMultiRoomActivity.this,"stream Id should not be empty",Toast.LENGTH_SHORT).show();
        }
        engine.startPreview(new ZegoCanvas(binding.display));
        engine.startPublishingStream(streamId);
    }
    public boolean checkIsEmpty(String a){
        if(a==null||a.equals("")){
            return true;
        }
        return false;
    }

    public void stopPublish(View view) {
        engine.stopPreview();
        engine.stopPublishingStream();
    }

    public void startPlay(View view) {
        String streamId=binding.edMultiStreamId.getText().toString().trim();
        if(checkIsEmpty(streamId)){
            Toast.makeText(LoginMultiRoomActivity.this,"stream Id should not be empty",Toast.LENGTH_SHORT).show();
        }
        engine.startPlayingStream(streamId,new ZegoCanvas(binding.display));
    }

    public void stopPlay(View view) {
        String streamId=binding.edMultiStreamId.getText().toString().trim();
        if(checkIsEmpty(streamId)){
            Toast.makeText(LoginMultiRoomActivity.this,"stream Id should not be empty",Toast.LENGTH_SHORT).show();
        }
        engine.stopPlayingStream(streamId);
    }

    public void sendExtraInfo(View view) {
        String messages=binding.edMultiExtraInfo.getText().toString().trim();
        if(checkIsEmpty(messages)){
            Toast.makeText(LoginMultiRoomActivity.this,"message should not be empty",Toast.LENGTH_SHORT).show();
        }
        engine.setRoomExtraInfo(extraRoomId, "zego", messages, new IZegoRoomSetRoomExtraInfoCallback() {
            @Override
            public void onRoomSetRoomExtraInfoResult(int i) {
                AppLogger.getInstance().i("onRoomSetRoomExtraInfoResult:  errorCode = " + i);
            }
        });
    }

    public void switchRoom(View view){
        fromRoomID = binding.fromRoomID.getText().toString().trim();
        toRoomID = binding.toRoomID.getText().toString().trim();
        if(checkIsEmpty(fromRoomID)){
            Toast.makeText(LoginMultiRoomActivity.this,"switchRoom fromRoomID should not be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(checkIsEmpty(toRoomID)){
            Toast.makeText(LoginMultiRoomActivity.this,"switchRoom toRoomID should not be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        engine.switchRoom(fromRoomID, toRoomID);
    }

    public void sendBroadCastMessage(View view) {
        String messages=binding.edMultiBroadInfo.getText().toString().trim();
        if(checkIsEmpty(messages)){
            Toast.makeText(LoginMultiRoomActivity.this,"message should not be empty",Toast.LENGTH_SHORT).show();
        }
        engine.sendBroadcastMessage(broadRoomId, messages, new IZegoIMSendBroadcastMessageCallback() {
            @Override
            public void onIMSendBroadcastMessageResult(int i, long l) {
                AppLogger.getInstance().i("onIMSendBroadcastMessageResult:  errorCode = " + i);

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FloatingView.get().attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FloatingView.get().detach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }
}
