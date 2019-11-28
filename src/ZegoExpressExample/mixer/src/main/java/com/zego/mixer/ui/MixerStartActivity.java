package com.zego.mixer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.common.util.AppLogger;
import com.zego.common.widgets.log.FloatingView;
import com.zego.mixer.R;
import com.zego.zegoexpress.callback.IZegoEventHandler;
import com.zego.zegoexpress.callback.IZegoMixerStartCallback;
import com.zego.zegoexpress.callback.IZegoMixerStopCallback;
import com.zego.zegoexpress.callback.ZegoMixerStartResult;
import com.zego.zegoexpress.constants.ZegoMixerContentType;
import com.zego.zegoexpress.constants.ZegoResolution;
import com.zego.zegoexpress.constants.ZegoUpdateType;
import com.zego.zegoexpress.constants.ZegoViewMode;
import com.zego.zegoexpress.entity.ZegoCanvas;
import com.zego.zegoexpress.entity.ZegoMixerAudioConfig;
import com.zego.zegoexpress.entity.ZegoMixerInput;
import com.zego.zegoexpress.entity.ZegoMixerOutput;
import com.zego.zegoexpress.entity.ZegoMixerTask;
import com.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import com.zego.zegoexpress.entity.ZegoRect;
import com.zego.zegoexpress.entity.ZegoStream;
import com.zego.zegoexpress.entity.ZegoWatermark;

import java.util.ArrayList;

public class MixerStartActivity extends AppCompatActivity implements IMixerStreamUpdateHandler{

    private static ArrayList<CheckBox> checkBoxList=new ArrayList<CheckBox>();
    private static LinearLayout ll_checkBoxList;
    private static String mixStreamID = "mixstream_output_100";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer_start);

        ll_checkBoxList = findViewById(R.id.ll_CheckBoxList);
        TextView tv_room = findViewById(R.id.tv_room_id3);
        tv_room.setText(MixerMainActivity.roomID);

        ll_checkBoxList.removeAllViews();
        checkBoxList.clear();
        for(String streamID: MixerMainActivity.streamIDList){
            CheckBox checkBox=(CheckBox) View.inflate(this, R.layout.checkbox, null);
            checkBox.setText(streamID);
            ll_checkBoxList.addView(checkBox);
            checkBoxList.add(checkBox);
        }

        MixerMainActivity.registerStreamUpdateNotify(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixerMainActivity.registerStreamUpdateNotify(null);
    }

    public void ClickStartMix(View view) {
        int count = 0;
        String streamID_1 = "";
        String streamID_2 = "";
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isChecked()) {
                count++;
                if (streamID_1.equals("")) {
                    streamID_1 = checkBoxList.get(i).getText().toString();
                }
                else if (streamID_2.equals("")){
                    streamID_2 = checkBoxList.get(i).getText().toString();
                }
            }
        }
        if (count != 2) {
            Toast.makeText(this, getString(R.string.tx_mixer_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        ZegoMixerTask task = new ZegoMixerTask("task1");
        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        ZegoMixerInput input_1 = new ZegoMixerInput();
        input_1.streamID = streamID_1;
        input_1.contentType = ZegoMixerContentType.CONTENT_TYPE_VIDEO;
        input_1.layout.left = 50;
        input_1.layout.right = 670;
        input_1.layout.top = 0;
        input_1.layout.bottom = 540;
        inputList.add(input_1);

        ZegoMixerInput input_2 = new ZegoMixerInput();
        input_2.streamID = streamID_2;
        input_2.contentType = ZegoMixerContentType.CONTENT_TYPE_VIDEO;
        input_2.layout.left = 50;
        input_2.layout.right = 670;
        input_2.layout.top = 640;
        input_2.layout.bottom = 1180;
        inputList.add(input_2);

        ArrayList<ZegoMixerOutput> outputList = new ArrayList<>();
        ZegoMixerOutput output = new ZegoMixerOutput();
        output.target = mixStreamID;
        outputList.add(output);

        ZegoMixerAudioConfig audioConfig = new ZegoMixerAudioConfig();
        ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig(ZegoResolution.RESOLUTION_720x1280);
        task.setMixerInputList(inputList);
        task.setMixerOutputList(outputList);
        task.setMixerOutputAudioConfig(audioConfig);
        task.setMixerOutputVideoConfig(videoConfig);

        ZegoWatermark watermark = new ZegoWatermark();
        watermark.imageURL = "preset-id://zegowp.png";
        watermark.layout.top = 0;
        watermark.layout.left = 0;
        watermark.layout.right = 300;
        watermark.layout.bottom = 200;
        task.setMixerOutputWatermark(watermark);

        task.setMixerOutputBackgroundImage("preset-id://zegobg.png");

        MixerMainActivity.engine.startMixerTask(task, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(String s, ZegoMixerStartResult zegoMixerStartResult) {
                AppLogger.getInstance().i("onMixerStartResult: result = " + zegoMixerStartResult.errorCode);
                if (zegoMixerStartResult.errorCode != 0) {
                    String msg = getString(R.string.tx_mixer_start_fail) + zegoMixerStartResult.errorCode;
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                else {
                    String msg = getString(R.string.tx_mixer_start_ok);
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextureView tv_play_mix = findViewById(R.id.tv_play_mix);
        ZegoCanvas canvas = new ZegoCanvas(tv_play_mix, ZegoViewMode.VIEW_MODE_ASPECT_FIT);
        MixerMainActivity.engine.startPlayingStream(mixStreamID, canvas);
    }

    public void onRoomStreamUpdate() {
        ll_checkBoxList.removeAllViews();
        checkBoxList.clear();
        for(String streamID: MixerMainActivity.streamIDList){
            CheckBox checkBox=(CheckBox) View.inflate(this, R.layout.checkbox, null);
            checkBox.setText(streamID);
            ll_checkBoxList.addView(checkBox);
            checkBoxList.add(checkBox);
        }
    }

    public void ClickStopWatch(View view) {
        MixerMainActivity.engine.stopPlayingStream(mixStreamID);
    }

    public void ClickStopMix(View view) {
        MixerMainActivity.engine.stopMixerTask("task1");
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MixerStartActivity.class);
        activity.startActivity(intent);
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
}
