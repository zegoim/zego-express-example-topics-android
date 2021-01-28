package im.zego.customaudioio;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.zego.common.GetAppIDConfig;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioSourceType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoAudioFrameParam;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomAudioConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K;

public class AudioCustomCaptureActivity extends Activity {
    private Button start, stop, playStream, loginRoom;
    private ZegoExpressEngine engine;
    private Switch openInternalRender;
    private LinearLayout internalRenderLayout;
    private String roomId = "QuickStartRoom-1";
    private String userId;
    private Integer mRecordBufferSize;
    private int captureSampleRate = 44100;
    private int captureChannel = AudioFormat.CHANNEL_IN_MONO;
    private TextView tv, capturePlayStateTv;
    private EditText publishStreamIdEditText;
    private EditText playStreamIdEditText;
    ByteBuffer mPcmBuffer;
    private AudioRecord mAudioRecord;
    private TextView publishState;
    private ZegoAudioFrameParam audioFrameParam = new ZegoAudioFrameParam();
    private ZegoEngineConfig engineConfig = new ZegoEngineConfig();
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    private enum Status {
        STATUS_NO_READY,
        STATUS_READY,
        STATUS_START,
        STATUS_STOP
    }

    private Status mStatus;
    private TextureView preView;

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
        mStatus = Status.STATUS_READY;
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
        preView = findViewById(R.id.capture_preview);
        start = findViewById(R.id.start_record);
        stop = findViewById(R.id.stop_record);
        loginRoom = findViewById(R.id.login_room);
        playStream = findViewById(R.id.play_stream);
        capturePlayStateTv = findViewById(R.id.capture_play_state);
        tv = findViewById(R.id.capture_state);
        publishStreamIdEditText = findViewById(R.id.capture_stream_id);
        playStreamIdEditText = findViewById(R.id.play_stream_id);
        publishState = findViewById(R.id.publish_state);
        openInternalRender = findViewById(R.id.open_internal_render);
        internalRenderLayout = findViewById(R.id.render_internal);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == Status.STATUS_START) {
                    //Toast.makeText(AudioCustomCaptureActivity.this,"custom Capture has been enabled, please do not click repeatedly",Toast.LENGTH_SHORT).show();
                    Log.i("[ZEGO]", "custom Capture has been enabled, please do not click repeatedly");
                    return;
                }
                if (publishStreamIdEditText.getText().toString() == null || publishStreamIdEditText.getText().toString().trim().equals("")) {
                    Toast.makeText(AudioCustomCaptureActivity.this, "streamId should not be empty when start custom capture", Toast.LENGTH_SHORT).show();
                    return;
                }
                playStreamIdEditText.setText(publishStreamIdEditText.getText().toString().trim());
                ZegoCustomAudioConfig config = new ZegoCustomAudioConfig();
                config.sourceType = ZegoAudioSourceType.CUSTOM;
                //enable custom capture
                engine.enableCustomAudioIO(true, config);
                engine.startPreview(new ZegoCanvas(preView));
                engine.startPublishingStream(publishStreamIdEditText.getText().toString().trim());
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
        playStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playStreamIdEditText.getText().toString() == null || playStreamIdEditText.getText().toString().trim().equals("")) {
                    Toast.makeText(AudioCustomCaptureActivity.this, "streamId should not be empty when play stream", Toast.LENGTH_SHORT).show();
                    return;
                }
                engine.startPlayingStream(playStreamIdEditText.getText().toString().trim(), new ZegoCanvas(null));
            }
        });
        openInternalRender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reset();
                if (isChecked) {
                    internalRenderLayout.setVisibility(View.VISIBLE);
                    engineConfig.advancedConfig.put("ext_capture_and_inner_render", "true");
                    ZegoExpressEngine.setEngineConfig(engineConfig);
                } else {
                    engineConfig.advancedConfig.put("ext_capture_and_inner_render", "false");
                    ZegoExpressEngine.setEngineConfig(engineConfig);
                }
            }
        });
        loginRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
                engine.loginRoom(roomId, new ZegoUser(userId));
            }
        });
    }

    public void reset() {
        internalRenderLayout.setVisibility(View.GONE);
        destroy();
        createZegoExpressEngine();
        initAudioRecord();
        tv.setText("Current status:  custom audio capture has been disabled");
    }

    private void createZegoExpressEngine() {
        engine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                for (ZegoStream stream : streamList) {
                    Log.i("[ZEGO]", "onRoomStreamUpdate roomID:" + roomID + " updateType:" + updateType + " streamId:" + stream.streamID);
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onRoomStateUpdate roomID:" + roomID + " state:" + state);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPublisherStateUpdate streamID:" + streamID + " state:" + state);
                publishState.setText("publish state:" + state + "   streamId:" + streamID);
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                capturePlayStateTv.setText("play state:" + state + "   streamId:" + streamID);

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
                mAudioRecord.setPositionNotificationPeriod(captureSampleRate / 25);
                mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                    @Override
                    public void onMarkerReached(AudioRecord recorder) {

                    }

                    @Override
                    public void onPeriodicNotification(AudioRecord recorder) {
                        singleThreadPool.execute(new Runnable() {//不通过子线程处理，会卡顿UI
                            @Override
                            public void run() {
                                if (mStatus == Status.STATUS_START) {//通过read驱动setRecordPositionUpdateListener回调
                                    mAudioRecord.read(bytes, 0, bytes.length);//读取流
                                    mPcmBuffer.clear();
                                    mPcmBuffer.put(bytes, 0, mRecordBufferSize);
                                    audioFrameParam.sampleRate = ZEGO_AUDIO_SAMPLE_RATE_44K;
                                    engine.sendCustomAudioCapturePCMData(mPcmBuffer, mRecordBufferSize, audioFrameParam);

                                }
                            }
                        });

                    }
                });
                mAudioRecord.startRecording();//开始录制
            }
        }).start();
        mStatus = Status.STATUS_START;
    }

    private void stopRecord() {
        if (mStatus == Status.STATUS_STOP) {
            //Toast.makeText(AudioCustomCaptureActivity.this,"The custom audio capture has been disabled, please do not click repeatedly",Toast.LENGTH_SHORT).show();
            Log.i("[ZEGO]", "The custom audio capture has been disabled, please do not click repeatedly");
            return;
        }
        engine.stopPublishingStream();
        engine.stopPreview();
        engine.logoutRoom(roomId);
        //disable custom capture
        engine.enableCustomAudioIO(false, null);
        mAudioRecord.stop();
        tv.setText("Current status: custom Audio capture has been disabled");
        mStatus = Status.STATUS_STOP;
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
        destroy();
    }

    public void destroy() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
        ZegoExpressEngine.destroyEngine(null);
        mStatus = Status.STATUS_NO_READY;
        publishState.setText("");
        capturePlayStateTv.setText("");
    }

    ;
}
