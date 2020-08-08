package im.zego.customaudioio;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import im.zego.common.GetAppIDConfig;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioSourceType;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoAudioFrameParam;
import im.zego.zegoexpress.entity.ZegoCustomAudioConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K;

public class AudioCustomCaptureActivity extends Activity {
    private Button start, stop;
    private ZegoExpressEngine engine;
    private String roomId="QuickStartRoom-1";
    private String userId;
    private Integer mRecordBufferSize;
     private int captureSampleRate =44100;
    private int captureChannel = AudioFormat.CHANNEL_IN_MONO;
    private TextView tv;
    private EditText editText;
    ByteBuffer mPcmBuffer;
    private AudioRecord mAudioRecord;
    private TextView publishState;
    private ZegoAudioFrameParam audioFrameParam=new ZegoAudioFrameParam();
    private enum Status {
        STATUS_NO_READY,
        STATUS_READY,
        STATUS_START,
        STATUS_STOP
    }
    private Status mStatus;
    private void initAudioRecord() {
        //获取每一帧的字节流大小
        mRecordBufferSize = AudioRecord.getMinBufferSize(captureSampleRate
                , captureChannel
                , AudioFormat.ENCODING_PCM_16BIT);
        mPcmBuffer = ByteBuffer.allocateDirect(mRecordBufferSize);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                , captureSampleRate
                , captureChannel
                , AudioFormat.ENCODING_PCM_16BIT
                , mRecordBufferSize);
        mStatus=Status.STATUS_READY;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_custom_capture);
        checkPermissions();
        initView();
        createZegoExpressEngine();
        initAudioRecord();
    }

    private void initView() {
        start = findViewById(R.id.start_record);
        stop = findViewById(R.id.stop_record);
        tv=findViewById(R.id.capture_state);
        editText=findViewById(R.id.capture_stream_id);
        publishState=findViewById(R.id.publish_state);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatus==Status.STATUS_START) {
                    //Toast.makeText(AudioCustomCaptureActivity.this,"custom Capture has been enabled, please do not click repeatedly",Toast.LENGTH_SHORT).show();
                    Log.i("[ZEGO]","custom Capture has been enabled, please do not click repeatedly");
                    return;
                }
                if (editText.getText().toString() == null || editText.getText().toString().trim().equals("")) {
                    Toast.makeText(AudioCustomCaptureActivity.this,"streamId should not be empty when start custom capture",Toast.LENGTH_SHORT).show();
                    return;
                }
                userId = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
                engine.loginRoom(roomId,new ZegoUser(userId));
                ZegoCustomAudioConfig config=new ZegoCustomAudioConfig();
                config.sourceType= ZegoAudioSourceType.CUSTOM;
                //enable custom capture
                engine.enableCustomAudioIO(true,config);
                engine.startPublishingStream(editText.getText().toString().trim());
                //start AudioRecord
                startRecord();
                tv.setText("Current status:  custom audio capture has been enabled");
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
    }

    private void createZegoExpressEngine() {
        engine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                for(ZegoStream stream:streamList) {
                    Log.i("[ZEGO]", "onRoomStreamUpdate roomID:" + roomID + " updateType:" + updateType+" streamId:"+stream.streamID);
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onRoomStateUpdate roomID:"+roomID+" state:"+state);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPublisherStateUpdate streamID:"+streamID+" state:"+state);
                publishState.setText("publish state:" +state+"   streamId:"+streamID);
            }
        });

    }


    public void startRecord() {
        if (mStatus == Status.STATUS_NO_READY || mAudioRecord == null) {
            throw new IllegalStateException("AudioRecord is not init");
        }
        if (mStatus == Status.STATUS_START) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final byte[] bytes = new byte[mRecordBufferSize];
                mAudioRecord.read(bytes, 0, bytes.length);//通过read驱动setRecordPositionUpdateListener回调
                mAudioRecord.setPositionNotificationPeriod(captureSampleRate /25);//设置太小会导致UI卡顿
                mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                    @Override
                    public void onMarkerReached(AudioRecord recorder) {

                    }

                    @Override
                    public void onPeriodicNotification(AudioRecord recorder) {
                        if(mStatus==Status.STATUS_START) {//通过read驱动setRecordPositionUpdateListener回调
                            mAudioRecord.read(bytes, 0, bytes.length);//读取流
                            mPcmBuffer.clear();
                            mPcmBuffer.put(bytes, 0, mRecordBufferSize);
                            audioFrameParam.sampleRate = ZEGO_AUDIO_SAMPLE_RATE_44K;
                            engine.sendCustomAudioCapturePCMData(mPcmBuffer, mRecordBufferSize,audioFrameParam);

                        }
                    }
                });
                mAudioRecord.startRecording();//开始录制
            }
        }).start();
        mStatus=Status.STATUS_START;
    }

    private void stopRecord() {
        if(mStatus==Status.STATUS_STOP) {
            //Toast.makeText(AudioCustomCaptureActivity.this,"The custom audio capture has been disabled, please do not click repeatedly",Toast.LENGTH_SHORT).show();
            Log.i("[ZEGO]","The custom audio capture has been disabled, please do not click repeatedly");
            return;
        }
        engine.stopPublishingStream();
        engine.logoutRoom(roomId);
        //disable custom capture
        engine.enableCustomAudioIO(false,null);
        mAudioRecord.stop();
        tv.setText("Current status: custom Audio capture has been disabled");
        mStatus=Status.STATUS_STOP;
    }
    private void checkPermissions() {
        String[] permissionNeeded = {
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionNeeded, 101);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAudioRecord!=null) {
            mAudioRecord.release();
        }
        ZegoExpressEngine.destroyEngine(null);
        mStatus=Status.STATUS_NO_READY;
        publishState.setText("");
    }
}
