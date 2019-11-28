package com.zego.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.common.GetAppIDConfig;
import com.zego.im.R;
import com.zego.zegoexpress.ZegoExpressEngine;
import com.zego.zegoexpress.callback.IZegoEventHandler;
import com.zego.zegoexpress.callback.IZegoIMSendBroadcastMessageCallback;
import com.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
import com.zego.zegoexpress.constants.ZegoScenario;
import com.zego.zegoexpress.constants.ZegoUpdateType;
import com.zego.zegoexpress.entity.ZegoMessageInfo;
import com.zego.zegoexpress.entity.ZegoRoomConfig;
import com.zego.zegoexpress.entity.ZegoUser;

import java.util.ArrayList;
import java.util.Date;

public class IMActivity extends AppCompatActivity {
    ZegoExpressEngine engine;
    private static ArrayList<CheckBox> checkBoxList=new ArrayList<CheckBox>();
    private static LinearLayout ll_checkBoxList;
    ArrayList<String> mUserList = new ArrayList<>();
    ArrayList<String> records = new ArrayList<>();
    String userID;
    String userName;
    String roomID = "ChatRoom-1";

    @Override
    protected void onDestroy() {
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine();
        checkBoxList.clear();
        mUserList.clear();
        records.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im);


        /** 生成随机的用户ID，避免不同手机使用时用户ID冲突，相互影响 */
        /** Generate random user ID to avoid user ID conflict and mutual influence when different mobile phones are used */
        String randomSuffix = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
        userID = "user" + randomSuffix;
        userName = "user" + randomSuffix;
        TextView tv_room = findViewById(R.id.tv_im_room);
        tv_room.setText(roomID);
        TextView tv_user = findViewById(R.id.tv_im_user);
        tv_user.setText(userID);

        engine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.SCENARIO_GENERAL, this.getApplication(), null);
        if (engine != null) {
            engine.addEventHandler(new IZegoEventHandler() {
                @Override
                public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                    for (int i = 0; i < userList.size(); i++) {
                        if (updateType == ZegoUpdateType.UPDATE_TYPE_ADD) {
                            mUserList.add(userList.get(i).userID);
                        }
                        else {
                            mUserList.remove(userList.get(i).userID);
                        }
                    }

                    ll_checkBoxList = findViewById(R.id.ll_CheckBoxList);
                    ll_checkBoxList.removeAllViews();
                    checkBoxList.clear();
                    for(String userID: mUserList){
                        CheckBox checkBox=(CheckBox) View.inflate(IMActivity.this, R.layout.checkbox, null);
                        checkBox.setText(userID);
                        ll_checkBoxList.addView(checkBox);
                        checkBoxList.add(checkBox);
                    }
                }

                public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoMessageInfo> messageList) {
                    for (int i = 0; i < messageList.size(); i++) {
                        ZegoMessageInfo info = messageList.get(i);
                        records.add(info.fromUser.userID + ": " + info.message);
                    }

                    /** 在ListView中显示消息 */
                    /** Show message in the Listview */
                    ListView listView = findViewById(R.id.lv_im_message);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(IMActivity.this, R.layout.array_adapter, records);
                    listView.setAdapter(adapter);
                }

                public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
                    records.add(fromUser.userID + ": " + command);
                    /** 在ListView中显示消息 */
                    /** Show message in the Listview */
                    ListView listView = findViewById(R.id.lv_im_message);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(IMActivity.this, R.layout.array_adapter, records);
                    listView.setAdapter(adapter);
                }
            });
            ZegoRoomConfig config = new ZegoRoomConfig();
            /** 使能用户登录/登出房间通知 */
            /** Enable notification when user login or logout */
            config.isUserStateNotify = true;
            engine.loginRoom(roomID, new ZegoUser(userID, userName), config);
        }
    }

    public void ClickSendBCMsg(View v) {
        EditText etMsg = findViewById(R.id.ed_bc_message);
        final String msg = etMsg.getText().toString();
        if (!msg.equals("")) {
            engine.sendBroadcastMessage(msg, roomID, new IZegoIMSendBroadcastMessageCallback() {
                /** 发送广播消息结果回调处理 */
                /** Send broadcast message result callback processing */
                @Override
                public void onIMSendBroadcastMessageResult(int errorCode) {
                    if (errorCode == 0) {
                        Toast.makeText(IMActivity.this, getString(R.string.tx_im_send_bc_ok), Toast.LENGTH_SHORT).show();
                        records.add(userID + getString(R.string.tx_im_me) + msg);
                        ListView listView = findViewById(R.id.lv_im_message);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(IMActivity.this, R.layout.array_adapter, records);
                        listView.setAdapter(adapter);
                    }
                    else {
                        Toast.makeText(IMActivity.this, getString(R.string.tx_im_send_bc_fail) + errorCode, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void ClickSendCustomMsg(View v) {
        EditText etMsg = findViewById(R.id.ed_cc_message);
        final String msg = etMsg.getText().toString();
        ArrayList<ZegoUser> userList = new ArrayList<>();
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isChecked()) {
                String userID = checkBoxList.get(i).getText().toString();
                ZegoUser user = new ZegoUser(userID, userID);
                userList.add(user);
            }
        }
        if (!msg.equals("")) {
            engine.sendCustomCommand(msg, userList, roomID, new IZegoIMSendCustomCommandCallback() {
                /** 发送用户自定义消息结果回调处理 */
                /** Send custom command result callback processing */
                @Override
                public void onIMSendCustomCommandResult(int errorCode) {
                    if (errorCode == 0) {
                        Toast.makeText(IMActivity.this, getString(R.string.tx_im_send_cc_ok), Toast.LENGTH_SHORT).show();
                        records.add(userID + getString(R.string.tx_im_me) + msg);
                        ListView listView = findViewById(R.id.lv_im_message);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(IMActivity.this, R.layout.array_adapter, records);
                        listView.setAdapter(adapter);
                    }
                    else {
                        Toast.makeText(IMActivity.this, getString(R.string.tx_im_send_cc_fail) + errorCode, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, IMActivity.class);
        activity.startActivity(intent);
    }
}
