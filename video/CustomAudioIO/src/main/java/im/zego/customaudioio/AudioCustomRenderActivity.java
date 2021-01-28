package im.zego.customaudioio;


import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import im.zego.common.GetAppIDConfig;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioSampleRate;
import im.zego.zegoexpress.constants.ZegoAudioSourceType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoAudioFrameParam;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomAudioConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class AudioCustomRenderActivity extends AppCompatActivity {
    private Button startRender, stopRender;
    private TextView renderTV;
    private ZegoAudioFrameParam audioFrameParam=new ZegoAudioFrameParam();
    private String roomId="QuickStartRoom-1";
    private String userId;
    private EditText editText;
    private TextView playState;
    private TextureView playView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_custom_render);
        checkPermissions();
        initView();
        initAudioTrack();
        createZegoExpressEngine();
    }

    private void initView() {
        playView =findViewById(R.id.render_view);
        startRender = findViewById(R.id.start_render_record);
        stopRender = findViewById(R.id.stop_render_record);
        renderTV=findViewById(R.id.render_state);
        editText=findViewById(R.id.render_stream_id);
        playState =findViewById(R.id.play_state);
        startRender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == Status.STATUS_START) {
                    Log.i("[ZEGO]","custom render has been enabled, please do not click repeatedly");
                    return;
                }
                if (editText.getText().toString() == null || editText.getText().toString().trim().equals("")) {
                    Toast.makeText(AudioCustomRenderActivity.this,"streamId should not be empty when start custom render",Toast.LENGTH_SHORT).show();
                    return;
                }
                userId = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
                engine.loginRoom(roomId,new ZegoUser(userId));
                ZegoCustomAudioConfig config=new ZegoCustomAudioConfig();
                config.sourceType= ZegoAudioSourceType.CUSTOM;
                //enable custom capture
                engine.enableCustomAudioIO(true,config);
                engine.startPlayingStream(editText.getText().toString().trim(),new ZegoCanvas(playView));
                startRender();
            }
        });
        stopRender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == Status.STATUS_STOP) {
                    Log.i("[ZEGO]","custom render has been disabled, please do not click repeatedly");
                    return;
                }
                stopRender();
            }
        });
    }

    private int mRenderBufferSize;
    private AudioTrack mAudioTrack;
    private int RENDER_SAMPLE_RATE = 44100;
    private int CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private enum Status {
        STATUS_NO_READY,
        STATUS_READY,
        STATUS_START,
        STATUS_STOP
    }

    private  static Status mStatus;
    private ByteBuffer renderBuffer;

    private void initAudioTrack() {
        //渲染
        mRenderBufferSize = AudioTrack.getMinBufferSize(RENDER_SAMPLE_RATE, CHANNEL, AudioFormat.ENCODING_PCM_16BIT);
        renderBuffer = ByteBuffer.allocateDirect(mRenderBufferSize);
        if (mRenderBufferSize <= 0) {
            throw new IllegalStateException("AudioTrack is not available " + mRenderBufferSize);
        }
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RENDER_SAMPLE_RATE, CHANNEL, AudioFormat.ENCODING_PCM_16BIT,
                  mRenderBufferSize, AudioTrack.MODE_STREAM);
        mStatus = Status.STATUS_READY;
    }


    public void startRender() throws IllegalStateException {

        if (mStatus == Status.STATUS_NO_READY || mAudioTrack == null) {
            throw new IllegalStateException("AudioTrack is not init");
        }
        if (mStatus == Status.STATUS_START) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playAudioData();
                } catch (Exception e) {
                    Log.i("[ZEGO]","playAudioData Exception:"+e.getMessage());
                }
            }
        }).start();
        mStatus = Status.STATUS_START;
        renderTV.setText("Current status: Audio custom rendering has been enabled");
    }

    private void playAudioData() throws Exception {
//        mStatus=Status.STATUS_READY;
//        byte[] bytes = new byte[mRenderBufferSize];
        if(mAudioTrack==null){
            return;
        }
        mAudioTrack.play();
        byte[] bytes= new byte[mRenderBufferSize];
        while (mStatus== Status.STATUS_START){
                renderBuffer.clear();//清除buffer
                //采集
             engine.fetchCustomAudioRenderPCMData(renderBuffer,mRenderBufferSize,audioFrameParam);
                if (renderBuffer != null) {
                    renderBuffer.get(bytes);
                }
//渲染
                mAudioTrack.write(bytes, 0, bytes.length);
        }
    }

    public void stopRender() throws IllegalStateException {
        if (mStatus == Status.STATUS_NO_READY || mStatus == Status.STATUS_READY) {
            throw new IllegalStateException("AudioTrack has not played yet");
        } else {
            mAudioTrack.stop();
            mStatus = Status.STATUS_STOP;
            engine.logoutRoom(roomId);
            //disable custom capture
            engine.enableCustomAudioIO(false,null);
        }
        renderTV.setText("Current status: Audio custom rendering has been disabled");
    }

    public void releaseRender() {
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        mStatus = Status.STATUS_NO_READY;
    }

    private ZegoExpressEngine engine;
    private void createZegoExpressEngine() {
        audioFrameParam.sampleRate= ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_44K;
        engine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, getApplication(), null);
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,JSONObject extendedData) {
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
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPlayerStateUpdate streamID:"+streamID+" state:"+state);
                playState.setText("play state:"+state+"   "+"streamId: "+streamID);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRender();
        ZegoExpressEngine.destroyEngine(null);
        playState.setText("");
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
}
