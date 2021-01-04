package im.zego.datarecord;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import im.zego.common.GetAppIDConfig;
import im.zego.common.util.AppLogger;
import im.zego.common.util.SettingDataUtil;
import im.zego.common.widgets.log.FloatingView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoDataRecordEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoDataRecordState;
import im.zego.zegoexpress.constants.ZegoDataRecordType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoDataRecordConfig;
import im.zego.zegoexpress.entity.ZegoDataRecordProgress;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class DataRecordActivity extends Activity {
    private Button startRecord,stopRecord;
    private EditText path;
    private  ZegoDataRecordConfig recordConfig=new ZegoDataRecordConfig();
    private static String roomId="recordRoom";
    private String userId;
    private ZegoExpressEngine engine;
    private Status mStatus=Status.STOP;
    private String mStreamId;
    private TextureView textureView;
    private TextView recordState,recordDuration,recordFileSize;
    private RadioGroup group;
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, DataRecordActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_record);
        checkPermissions();
        /** 添加悬浮日志视图 */
        /** Add floating log view */
        FloatingView.get().add();
        /** 记录SDK版本号 */
        /** Record SDK version */
        AppLogger.getInstance().i("SDK version : %s", ZegoExpressEngine.getVersion());
        initView();
        createEnigne();
    }
    private enum Status{
        START,
        STOP
    }
    private void createEnigne() {
        engine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        AppLogger.getInstance().i("createEngine success!!");
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
                AppLogger.getInstance().i("onRoomStateUpdate, roomId :%s state:%s", roomID,state);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPublisherStateUpdate streamID:"+streamID+" state:"+state);
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPlayerStateUpdate streamID:"+streamID+" state:"+state);
            }
        });
        userId = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));
        engine.loginRoom(roomId,new ZegoUser(userId));
    }

    private void initView() {
        startRecord=findViewById(R.id.start_record);
        stopRecord=findViewById(R.id.stop_record);
        path=findViewById(R.id.path);//only support .mp4 and .flv
        textureView=findViewById(R.id.preview);
        recordState=findViewById(R.id.record_state);
        recordDuration=findViewById(R.id.duration);
        recordFileSize=findViewById(R.id.currentFileSize);
        group=findViewById(R.id.record_type);
        recordConfig.recordType = ZegoDataRecordType.AUDIO_AND_VIDEO;//default
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatus==Status.START){
                    Toast.makeText(DataRecordActivity.this,"Recording function has already been started",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(path.getText().toString()==null||path.getText().toString().equals("")){
                    Toast.makeText(DataRecordActivity.this,"The storage path needs to be filled in to enable the recording function",Toast.LENGTH_SHORT).show();
                    return;
                }
                recordConfig.filePath = path.getText().toString().trim();
                engine.setDataRecordEventHandler(new IZegoDataRecordEventHandler() {
                    @Override
                    public void onCapturedDataRecordStateUpdate(ZegoDataRecordState state, int errorCode, ZegoDataRecordConfig config, ZegoPublishChannel channel) {
                        Log.i("[ZEGO]", "onCapturedDataRecordStateUpdate state:"+state+" path:"+config.filePath);
                        recordState.setText("RecordState:"+state.toString());
                    }

                    @Override
                    public void onCapturedDataRecordProgressUpdate(ZegoDataRecordProgress progress, ZegoDataRecordConfig config, ZegoPublishChannel channel) {
                        Log.i("[ZEGO]", "onCapturedDataRecordProgressUpdate duration:"+progress.duration+"currentFileSize :"+progress.currentFileSize);
                        recordDuration.setText("RecordDuration:"+progress.duration+"ms");
                        recordFileSize.setText("currentFileSize:"+progress.currentFileSize+"byte");

                    }
                });
                engine.startPreview(new ZegoCanvas(textureView));
                engine.startRecordingCapturedData(recordConfig, ZegoPublishChannel.MAIN);
                mStatus=Status.START;
                AppLogger.getInstance().i("startRecordingCapturedData");

            }
        });
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatus==Status.STOP){
                    Toast.makeText(DataRecordActivity.this,"Recording function has already been stopped",Toast.LENGTH_SHORT).show();
                    return;
                }
                engine.stopRecordingCapturedData(ZegoPublishChannel.MAIN);
                engine.logoutRoom(roomId);
                mStatus=Status.STOP;
                AppLogger.getInstance().i("stopRecordingCapturedData");
            }
        });
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.only_audio) {
                    recordConfig.recordType = ZegoDataRecordType.ONLY_AUDIO;
                } else if (checkedId == R.id.only_video) {
                    recordConfig.recordType = ZegoDataRecordType.ONLY_VIDEO;
                } else if (checkedId == R.id.both) {
                    recordConfig.recordType = ZegoDataRecordType.AUDIO_AND_VIDEO;
                }
            }
        });
        initPath();
    }

    private void initPath() {
        String pathUrl=getExternalCacheDir().getAbsolutePath()+"/demo.flv";
        Log.i("[ZEGO]","default path:"+pathUrl);
        path.setText(pathUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatus=Status.STOP;
        ZegoExpressEngine.destroyEngine(null);
        recordState.setText("NO_RECORD");
        recordDuration.setText("RecordDuration: 0ms");
        recordFileSize.setText("currentFileSize:0 byte");
    }
    private void checkPermissions() {
        String[] permissionNeeded = {
                "android.permission.WRITE_EXTERNAL_STORAGE"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionNeeded, 101);
            }
        }
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
