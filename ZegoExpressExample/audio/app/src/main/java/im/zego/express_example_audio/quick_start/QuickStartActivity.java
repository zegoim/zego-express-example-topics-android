//
//  QuickStartActivity.java
//  ZegoExpressExampleAudio
//  im.zego.express_example_audio.quick_start
//
//  Created by Patrick Fu on 2020/06/01.
//  Copyright © 2020 Zego. All rights reserved.
//

package im.zego.express_example_audio.quick_start;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.util.Date;

import im.zego.common.GetAppIDConfig;
import im.zego.express_example_audio.R;
import im.zego.express_example_audio.databinding.ActivityQuickStartBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoUser;

public class QuickStartActivity extends AppCompatActivity {

    ActivityQuickStartBinding binding;

    boolean isTestEnv = true;

    String roomID = "QuickStartRoom-1";
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Generate random user ID to avoid user ID conflict and mutual influence when different mobile phones are used
        String randomSuffix = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
        userID = "user" + randomSuffix;

        setupUI();

    }

    @Override
    protected void onDestroy() {
        // Release SDK resources
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }

    private void setupUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quick_start);

        binding.sdkVersionTextView.setText("🌞 SDK Version: " + ZegoExpressEngine.getVersion());

        binding.createEngineButton.setText(getString(R.string.create_engine_button_normal));
        binding.loginRoomButton.setText(getString(R.string.login_room_button_normal));
        binding.startPublishingButton.setText(getString(R.string.start_publishing_button_normal));
        binding.startPlayingButton.setText(getString(R.string.start_playing_button_normal));

        binding.appidTextView.setText("AppID: " + GetAppIDConfig.appID);
        binding.isTestEnvTextView.setText("isTestEnv: " + isTestEnv);

        binding.roomIdTextView.setText(roomID);
        binding.userIdTextView.setText(userID);
    }

    public void createEngineButtonClick(View view) {
        Log.i("[ZEGO]", "🚀 Create ZegoExpressEngine");

        // Create ZegoExpressEngine and set a eventHandler
        ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, isTestEnv, ZegoScenario.GENERAL, getApplication(), eventHandler);

        binding.createEngineButton.setText(getString(R.string.create_engine_button_success));
    }

    public void loginRoomButtonClick(View view) {
        // Instantiate a ZegoUser object
        ZegoUser user = new ZegoUser(userID);

        Log.i("[ZEGO]", "🚪 Start login room");

        // Login room
        ZegoExpressEngine.getEngine().loginRoom(roomID, user);
    }

    public void startPublishingButtonClick(View view) {
        String publishStreamID = binding.publishStreamIdEditText.getText().toString();

        Log.i("[ZEGO]", "📤 Start publishing stream");

        ZegoExpressEngine.getEngine().startPublishingStream(publishStreamID);
    }

    public void startPlayingButtonClick(View view) {
        String playStreamID = binding.playStreamIdEditText.getText().toString();

        Log.i("[ZEGO]", "📥 Start playing stream");

        ZegoExpressEngine.getEngine().startPlayingStream(playStreamID);
    }

    IZegoEventHandler eventHandler = new IZegoEventHandler() {

        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
            if (state == ZegoRoomState.CONNECTED && errorCode == 0) {
                Log.i("[ZEGO]", "🚩 🚪 Login room success");
                binding.loginRoomButton.setText(getString(R.string.login_room_button_success));
            }

            if (errorCode != 0) {
                Log.i("[ZEGO]", "🚩 ❌ 🚪 Login room fail, errorCode: " + errorCode);
                binding.loginRoomButton.setText(getString(R.string.login_room_button_fail));
            }
        }

        @Override
        public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
            if (state == ZegoPublisherState.PUBLISHING && errorCode == 0) {
                Log.i("[ZEGO]", "🚩 📤 Publishing stream success");
                binding.startPublishingButton.setText(getString(R.string.start_publishing_button_success));
            }

            if (errorCode != 0) {
                Log.i("[ZEGO]", "🚩 ❌ 📤 Publishing stream fail, errorCode: " + errorCode);
                binding.startPublishingButton.setText(getString(R.string.start_publishing_button_fail));
            }
        }

        @Override
        public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
            if (state == ZegoPlayerState.PLAYING && errorCode == 0) {
                Log.i("[ZEGO]", "🚩 📥 Playing stream success");
                binding.startPlayingButton.setText(getString(R.string.start_playing_button_success));
            }

            if (errorCode != 0) {
                Log.i("[ZEGO]", "🚩 ❌ 📥 Playing stream fail, errorCode: " + errorCode);
                binding.startPlayingButton.setText(getString(R.string.start_playing_button_fail));
            }
        }
    };
}
