//
//  AudioTalkActivity.java
//  ZegoExpressExampleAudio
//  im.zego.express_example_audio.audio_talk
//
//  Created by Patrick Fu on 2020/06/01.
//  Copyright © 2020 Zego. All rights reserved.
//

package im.zego.express_example_audio.audio_talk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import im.zego.common.GetAppIDConfig;
import im.zego.express_example_audio.R;
import im.zego.express_example_audio.audio_talk.widget.AudioTalkLayoutItem;
import im.zego.express_example_audio.databinding.ActivityAudioTalkBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class AudioTalkActivity extends AppCompatActivity {

    ActivityAudioTalkBinding binding;

    String roomID = "SoundLevelRoom-1";
    String userName;
    String userID;
    String streamID;

    private AudioTalkLayoutItem captureAudioTalkLayoutItem;

    private static final String TAG = "AudioTalk";

    // When playing multiple streams, use list to save the displayed spectrum and sound wave view
    public ArrayList<AudioTalkLayoutItem> audioTalkLayoutItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Generate random user ID to avoid user ID conflict and mutual influence when different mobile phones are used
        String randomSuffix = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));

        ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, getApplication(), eventHandler);

        userID = "userid-" + randomSuffix;
        userName = "username-" + randomSuffix;
        streamID = "streamid-" + randomSuffix;

        setupUI();

        // Enable notification when user login or logout
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.isUserStatusNotify = true;

        ZegoExpressEngine.getEngine().loginRoom(roomID, new ZegoUser(userID, userName), config);

        ZegoExpressEngine.getEngine().startPublishingStream(streamID);

    }

    @Override
    protected void onDestroy() {
        ZegoExpressEngine.getEngine().logoutRoom(roomID);
        // Release SDK resources
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }

    private void setupUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_talk);

        binding.roomIdTextView.setText(roomID);

        binding.soundlevelMonitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ZegoExpressEngine.getEngine().startSoundLevelMonitor();
                } else {
                    ZegoExpressEngine.getEngine().stopSoundLevelMonitor();
                }
            }
        });

        binding.spectrumMonitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ZegoExpressEngine.getEngine().startAudioSpectrumMonitor();
                } else {
                    ZegoExpressEngine.getEngine().stopAudioSpectrumMonitor();
                }
            }
        });

        captureAudioTalkLayoutItem = new AudioTalkLayoutItem(AudioTalkActivity.this);

        captureAudioTalkLayoutItem.setStreamId(streamID);
        captureAudioTalkLayoutItem.getUserIdTextView().setText(userID);
        captureAudioTalkLayoutItem.getStreamIdTextView().setText(streamID);

        audioTalkLayoutItemList.add(captureAudioTalkLayoutItem);

        binding.container.addView(captureAudioTalkLayoutItem);
    }

    IZegoEventHandler eventHandler = new IZegoEventHandler() {

        // Since the sound waves in this topic need to be animated, two instance variables are used here to save the value thrown in the previous SDK sound wave callback to achieve the effect of excessive animation
        // The progress value of the last local collection
        private double lastProgressCaptured = 0.0;

        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
            super.onRoomStreamUpdate(roomID, updateType, streamList);
            Log.v(TAG, "🚩 🌊 onRoomStreamUpdate: roomID" + roomID + ", updateType:" + updateType.value() + ", streamList: " + streamList);

            // Add the rendered view dynamically after pulling the stream here
            if (updateType == ZegoUpdateType.ADD) {
                for (ZegoStream zegoStream: streamList) {
                    ZegoExpressEngine.getEngine().startPlayingStream(zegoStream.streamID);
                    AudioTalkLayoutItem audioTalkLayoutItem = new AudioTalkLayoutItem(AudioTalkActivity.this);
                    binding.container.addView(audioTalkLayoutItem);
                    audioTalkLayoutItem.getStreamIdTextView().setText(zegoStream.streamID);
                    audioTalkLayoutItem.getUserIdTextView().setText(zegoStream.user.userID);
                    audioTalkLayoutItem.setStreamId(zegoStream.streamID);

                    audioTalkLayoutItemList.add(audioTalkLayoutItem);

                }
            } else if (updateType == ZegoUpdateType.DELETE) {
                for (ZegoStream zegoStream: streamList) {
                    ZegoExpressEngine.getEngine().stopPlayingStream(zegoStream.streamID);
                    Iterator<AudioTalkLayoutItem> it = audioTalkLayoutItemList.iterator();
                    while (it.hasNext()) {
                        AudioTalkLayoutItem audioTalkLayoutItemTmp = it.next();
                        if (audioTalkLayoutItemTmp.getStreamId().equals(zegoStream.streamID)) {
                            it.remove();
                            binding.container.removeView(audioTalkLayoutItemTmp);
                        }
                    }
                }
            }
        }

        @Override
        public void onCapturedSoundLevelUpdate(float soundLevel) {
            super.onCapturedSoundLevelUpdate(soundLevel);
            // Log.v(TAG, "onCapturedSoundLevelUpdate:" + soundLevel);
            ValueAnimator animator = ValueAnimator.ofFloat((float) lastProgressCaptured, (float)soundLevel).setDuration(100);
            animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    captureAudioTalkLayoutItem.getSoundLevelProgressBar().setProgress((int)((Float)valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            animator.start();
            lastProgressCaptured = soundLevel;
        }

        @Override
        public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
            super.onRemoteSoundLevelUpdate(soundLevels);
            // Log.v(TAG, "onRemoteSoundLevelUpdate:"+ soundLevels.size());
            for (HashMap.Entry<String, Float> entry : soundLevels.entrySet()) {
                String streamid = entry.getKey();
                Float value = entry.getValue();
                for (final AudioTalkLayoutItem audioTalkLayoutItem : audioTalkLayoutItemList) {
                    if (streamid.equals(audioTalkLayoutItem.getStreamId())) {
                        ValueAnimator animator = ValueAnimator.ofFloat(value.floatValue(), soundLevels.get(streamid).floatValue()).setDuration(100);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                audioTalkLayoutItem.getSoundLevelProgressBar().setProgress(((Float) (valueAnimator.getAnimatedValue())).intValue());
                            }
                        });
                        animator.start();
                    }
                }
            }
        }

        @Override
        public void onCapturedAudioSpectrumUpdate(float[] frequencySpectrum) {
            super.onCapturedAudioSpectrumUpdate(frequencySpectrum);
            // Log.v(TAG, "call back onCapturedAudioSpectrumUpdate");
            captureAudioTalkLayoutItem.getSpectrumView().updateFrequencySpectrum(frequencySpectrum);
        }

        @Override
        public void onRemoteAudioSpectrumUpdate(HashMap<String, float[]> frequencySpectrums) {
            super.onRemoteAudioSpectrumUpdate(frequencySpectrums);
            // Log.v(TAG, "call back onRemoteAudioSpectrumUpdate:" + frequencySpectrums);
            for (HashMap.Entry<String, float[]> entry : frequencySpectrums.entrySet()) {
                String streamid = entry.getKey();
                float[] values = entry.getValue();

                for (AudioTalkLayoutItem audioTalkLayoutItem : audioTalkLayoutItemList) {
                    if (streamid.equals(audioTalkLayoutItem.getStreamId())) {
                        audioTalkLayoutItem.getSpectrumView().updateFrequencySpectrum(values);
                    }
                }
            }
        }

        @Override
        public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
            if (state == ZegoRoomState.CONNECTED && errorCode == 0) {
                Log.i(TAG, "🚩 🚪 Login room success");
            }

            if (errorCode != 0) {
                Log.i(TAG, "🚩 ❌ 🚪 Login room fail, errorCode: " + errorCode);
            }
        }
    };
}
