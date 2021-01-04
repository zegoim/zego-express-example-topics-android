package im.zego.audioeffectplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import im.zego.audioeffectplayer.databinding.AudioEffectPlayerActivityBinding;
import im.zego.common.util.AppLogger;
import im.zego.common.util.SettingDataUtil;
import im.zego.common.widgets.log.FloatingView;
import im.zego.zegoexpress.ZegoAudioEffectPlayer;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoAudioEffectPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoAudioEffectPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoAudioEffectPlayerSeekToCallback;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioConfigPreset;
import im.zego.zegoexpress.constants.ZegoAudioEffectPlayState;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoAudioEffectPlayConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class AudioEffectPlayerUI extends Activity {
    private static final String PATH_ARRAY = "audio_effect_player_resource";
    private String root;
    private String path1;
    private String path2;
    private String path3;
    private List<String> fileNames = new ArrayList<>();
    private AudioEffectPlayerActivityBinding binding;
    private List<String> historyList;
    private AutoCompleteTextViewAdapter adapter;
    private ZegoExpressEngine engine;
    private String userId;
    private String roomId;
    private String streamId;
    private ZegoAudioEffectPlayer audioEffectPlayer;
    private ZegoAudioEffectPlayer tempAudioEffectPlayer;
    private static boolean isOutPublish = false;

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, AudioEffectPlayerUI.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.audio_effect_player_activity);
        initData();
        initView();
        createEngine();
    }

    private void createEngine() {
        engine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), this.getApplication(), null);
        AppLogger.getInstance().i("createEngine success!!");
        ZegoAudioConfig zegoAudioConfig=new ZegoAudioConfig(ZegoAudioConfigPreset.STANDARD_QUALITY_STEREO);
        engine.setAudioConfig(zegoAudioConfig);
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,JSONObject extendedData) {
                for (ZegoStream stream : streamList) {
                    Log.i("[ZEGO]", "onRoomStreamUpdate roomID:" + roomID + " updateType:" + updateType + " streamId:" + stream.streamID);
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onRoomStateUpdate roomID:" + roomID + " state:" + state);
                AppLogger.getInstance().i("onRoomStateUpdate, roomId :%s state:%s", roomID, state);
                if(state==ZegoRoomState.CONNECTED){
                    binding.audioRoomIdTv.setText("RoomID: "+roomID);
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPublisherStateUpdate streamID:" + streamID + " state:" + state);
                AppLogger.getInstance().i("onPublisherStateUpdate, streamId :%s state:%s", streamID, state);
                if(state==ZegoPublisherState.PUBLISHING){
                    binding.audioStreamIdTv.setText("streamID: "+streamID);
                }
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                Log.i("[ZEGO]", "onPlayerStateUpdate streamID:" + streamID + " state:" + state);
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

    private void initView() {
        binding.savePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPath = binding.pathList.getText().toString();
                if (newPath == null || newPath.trim().equals("")) {
                    Toast.makeText(AudioEffectPlayerUI.this, "new resource Path should not be null", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (String oldPath : historyList) {
                    if (newPath.trim().equals(oldPath)) {
                        Toast.makeText(AudioEffectPlayerUI.this, "new resource Path already exits", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                historyList.add(binding.pathList.getText().toString().trim());
                savePathArray(AudioEffectPlayerUI.this, historyList);
                adapter.addAll(historyList);
                adapter.notifyDataSetChanged();

            }
        });
        binding.clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pathList.setText("");
            }
        });
        binding.isOutPublish.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.output_true) {
                    isOutPublish = true;

                } else if (checkedId == R.id.output_false) {
                    isOutPublish = false;
                }
            }
        });
        binding.pathList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    binding.pathList.setText(binding.pathList.getText().toString().trim());
                }

            }
        });

    }

    private void initData() {
        root = getExternalFilesDir("").getPath();
        path1 = root + "/3-s.mp3";
        path2 = root + "/2-m.wav";
        path3 = root +"/2-s.wav";
        fileNames.add("3-s.mp3");
        fileNames.add("2-m.wav");
        fileNames.add("2-s.wav");

        copyAssetsFiles(fileNames);
        if (getSavePathArray(this) == null || getSavePathArray(this).size() == 0) {
            List array = new ArrayList();
            array.add(path1);
            array.add(path2);
            array.add(path3);
            savePathArray(this, array);
        }
        historyList = getSavePathArray(this);
        adapter = new AutoCompleteTextViewAdapter(
                historyList, this);
        binding.pathList.setAdapter(adapter);
        binding.pathList.setText(historyList.get(0));
        binding.pathList.setSelection(historyList.get(0).length());
    }

    public static void savePathArray(Context context, List<String> pathArray) {
        SharedPreferences prefs = context.getSharedPreferences(PATH_ARRAY, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (String b : pathArray) {
            jsonArray.put(b);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PATH_ARRAY, jsonArray.toString());
        editor.commit();
    }

    public static List<String> getSavePathArray(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PATH_ARRAY, Context.MODE_PRIVATE);
        List<String> resArray = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(prefs.getString(PATH_ARRAY, "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                resArray.add(jsonArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resArray;
    }


    private void copyAssetsFiles(final List<String> fileNames) {
        new Thread() {
            public void run() {
                for (String fileName : fileNames) {
                    copyAssetsFile(fileName);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        binding.copy.setVisibility(View.GONE);
                    }
                });

            }

        }.start();
    }

    private void copyAssetsFile(String fileName) {
        final File file = new File(getExternalFilesDir(""), fileName);//getFilesDir()方法用于获取/data/data//files目录
        System.out.println("文件路径---->" + file.getAbsolutePath());
        if (file.exists()) {//文件存在了就不需要拷贝了
            System.out.println("文件已经存在,不需要再拷贝");
            return;
        }
        try {
            //获取资产目录管理器
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);//输入流
            FileOutputStream fos = new FileOutputStream(file);//输出流
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(View view) {
        if (engine != null) {
            tempAudioEffectPlayer = engine.createAudioEffectPlayer();
            if (tempAudioEffectPlayer != null) {
                audioEffectPlayer=tempAudioEffectPlayer;
                audioEffectPlayer.setEventHandler(new IZegoAudioEffectPlayerEventHandler() {
                    @Override
                    public void onAudioEffectPlayStateUpdate(ZegoAudioEffectPlayer audioEffectPlayer, int audioEffectID, ZegoAudioEffectPlayState state, int errorCode) {
                        Log.d("[ZEGO]", "onAudioEffectPlayStateUpdate errorCode:" + errorCode + "  audioEffectID:" + audioEffectID + "  state:" + state);
                        AppLogger.getInstance().i("onAudioEffectPlayStateUpdate, audioEffectID :%s state:%s errorCode:%d", audioEffectID, state, errorCode);
                    }
                });
            }

        }
    }

    public boolean checkIsEmpty(String a) {
        if (a == null || a.trim().equals("")) {
            return true;
        }
        return false;
    }

    public boolean audioEffectIdCheck(String text) {
        if (checkIsEmpty(text)) {
            Toast.makeText(AudioEffectPlayerUI.this, "audioEffectId is empty", Toast.LENGTH_SHORT).show();
            return true;
        }
            Pattern pattern = Pattern.compile("[0-9]*");
        if(!pattern.matcher(text).matches()){
            Toast.makeText(AudioEffectPlayerUI.this, "audioEffectId should be a number", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public boolean pathCheck(String text) {
        if (checkIsEmpty(text)) {
            Toast.makeText(AudioEffectPlayerUI.this, "path is empty", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void start(View view) {
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            if (pathCheck(binding.pathList.getText().toString())) {
                return;
            }
            if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
                return;
            }
            ZegoAudioEffectPlayConfig config = new ZegoAudioEffectPlayConfig();
            String playCount = binding.playCount.getText().toString();
            if (playCount != null && !playCount.trim().equals("")) {
                Pattern pattern = Pattern.compile("[0-9]*");
                if(!pattern.matcher(playCount).matches()){
                    Toast.makeText(AudioEffectPlayerUI.this, "playCount should be a number", Toast.LENGTH_SHORT).show();
                    return ;
                }
                config.playCount = Integer.valueOf(playCount.trim());
            }
            config.isPublishOut =isOutPublish;
            audioEffectPlayer.start(Integer.valueOf(binding.audioEffectId.getText().toString().trim()), binding.pathList.getText().toString().trim(), config);
        }
    }
    public void stop(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.stop(Integer.valueOf(binding.audioEffectId.getText().toString().trim()));
        }

    }
    public void pause(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.pause(Integer.valueOf(binding.audioEffectId.getText().toString().trim()));
        }
    }
    public void resume(View view){
        if(audioEffectPlayer==null){
            Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
        }
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.resume(Integer.valueOf(binding.audioEffectId.getText().toString().trim()));
        }
    }
    public void pauseAll(View view){

        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.pauseAll();
        }
    }
    public void stopAll(View view){
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.stopAll();
        }
    }
    public void resumeAll(View view){
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.resumeAll();
        }
    }
    public void loadResource(View view){
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            if (pathCheck(binding.pathList.getText().toString())) {
                return;
            }
            if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
                return;
            }

            audioEffectPlayer.loadResource(Integer.valueOf(binding.audioEffectId.getText().toString().trim()), binding.pathList.getText().toString().trim(), new IZegoAudioEffectPlayerLoadResourceCallback() {
                @Override
                public void onLoadResourceCallback(int i) {
                    Log.d("[ZEGO]", "onLoadResourceCallback errorCode:" + i + "  audioEffectID:" + binding.audioEffectId.getText().toString().trim());
                    AppLogger.getInstance().i("onLoadResourceCallback audioEffectID :%s  errorCode:%d", binding.audioEffectId.getText().toString().trim(), i);

                }
            });
        }
    }
    public void unLoadResource(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }

            audioEffectPlayer.unloadResource(Integer.valueOf(binding.audioEffectId.getText().toString()));

        }
    }
    public boolean volumeCheck(String text) {
        if (checkIsEmpty(text)) {
            Toast.makeText(AudioEffectPlayerUI.this, "volume is empty", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    public void setVolume(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (volumeCheck(binding.volumeValue.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.setVolume(Integer.valueOf(binding.audioEffectId.getText().toString().trim()), Integer.valueOf(binding.volumeValue.getText().toString().trim()));
        }

    }
    public void setVolumeAll(View view){
        if (volumeCheck(binding.volumeValue.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.setVolumeAll(Integer.valueOf(Integer.valueOf(binding.volumeValue.getText().toString().trim())));
        }
    }
    public boolean seekToValueCheck(String text) {
        if (checkIsEmpty(text)) {
            Toast.makeText(AudioEffectPlayerUI.this, "seekToValue is empty", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    public void seekTo(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if (seekToValueCheck(binding.seekValue.getText().toString())) {
            return;
        }
        if (engine != null) {
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            audioEffectPlayer.seekTo(Integer.valueOf(binding.audioEffectId.getText().toString()), 1, new IZegoAudioEffectPlayerSeekToCallback() {
                @Override
                public void onSeekToCallback(int errorCode) {
                    Log.d("[ZEGO]", "onSeekToCallback errorCode:" + errorCode + "  audioEffectID:" + binding.audioEffectId.getText().toString());
                    AppLogger.getInstance().i("onSeekToCallback audioEffectID :%s  errorCode:%d", binding.audioEffectId.getText().toString().trim(), errorCode);

                }
            });
        }
    }
    public void publish(View view){
        streamId = binding.streamId.getText().toString();
        if(streamId==null||streamId.trim().equals("")){
            Toast.makeText(this,"stream Id should not be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(engine!=null){
            ZegoCanvas canvas =new ZegoCanvas(binding.preview);
            engine.startPreview(canvas);
            engine.startPublishingStream(streamId);
        }
    }
    public void destroyPlayer(View view){
        if(engine!=null){
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }else {
                engine.destroyAudioEffectPlayer(audioEffectPlayer);
            }
        }
    }
    public void getTotalDuration(View view) {
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if(engine!=null){
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            int audioEffectID =Integer.valueOf(binding.audioEffectId.getText().toString());
            binding.totalTv.setText(""+audioEffectPlayer.getTotalDuration(audioEffectID));
        }
    }
    public void getCurrentProgress(View view){
        if (audioEffectIdCheck(binding.audioEffectId.getText().toString())) {
            return;
        }
        if(engine!=null){
            if(audioEffectPlayer==null){
                Toast.makeText(this,"please create AudioEffectPlayre firstly",Toast.LENGTH_SHORT).show();
                return;
            }
            int audioEffectID =Integer.valueOf(binding.audioEffectId.getText().toString());
            binding.progressTv.setText(""+audioEffectPlayer.getCurrentProgress(audioEffectID));
        }
    }
    public void stopPublish(View view){
        if(engine!=null){
            engine.stopPreview();
            engine.stopPublishingStream();
        }
    }
    public void LoginRoom(View view){
        roomId=binding.audioEffectRoomId.getText().toString();
        if(roomId==null||roomId.trim().equals("")){
            Toast.makeText(this,"room_id should not be null",Toast.LENGTH_SHORT).show();
            return;
        }
        if(engine!=null){
            userId = String.valueOf(new Date().getTime() % (new Date().getTime() / 1000));
            engine.loginRoom(roomId, new ZegoUser(userId));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(engine!=null){
            engine.setAudioConfig(new ZegoAudioConfig());
        }
        ZegoExpressEngine.destroyEngine(null);
    }
}



