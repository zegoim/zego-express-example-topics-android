package im.zego.soundlevelandspectrum.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import im.zego.common.GetAppIDConfig;
import im.zego.soundlevelandspectrum.R;
import im.zego.soundlevelandspectrum.widget.SoundLevelAndSpectrumItem;
import im.zego.soundlevelandspectrum.widget.SpectrumView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class SoundLevelAndSpectrumMainActivity extends Activity {

    ZegoExpressEngine mSDKEngine;

    TextView mTvSoundlevelandspectrumRoomid;
    Switch mSwSoundlevelMonitor;
    Switch mSwSpectrumMonitor;
    // 本地推流的声浪的展现，需要获取该控件来设置进度值
    public ProgressBar mPbCaptureSoundLevel;
    TextView mTvSoundlevelandspectrumUserid ;
    TextView mTvSoundlevelandspectrumStreamid ;
    public SpectrumView mCaptureSpectrumView;
    // 使用线性布局作为容器，以动态添加所拉的流频谱和声浪展现
    public LinearLayout ll_container;

    String roomID = "SoundLevelRoom-1";
    String userName;
    String userID;
    String streamID;

    // 拉多条流的时候，使用list来保存展现的频谱和声浪的视图
    public ArrayList<SoundLevelAndSpectrumItem> frequencySpectrumAndSoundLevelItemList = new ArrayList<>();

    private static final String TAG = "Sound-Spectrum-Activity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundlevelandspectrum);


        /** 生成随机的用户ID，避免不同手机使用时用户ID冲突，相互影响 */
        /** Generate random user ID to avoid user ID conflict and mutual influence when different mobile phones are used */
        String randomSuffix = String.valueOf(new Date().getTime()%(new Date().getTime()/1000));

        mTvSoundlevelandspectrumRoomid = findViewById(R.id.tv_soundlevelandspectrum_roomid);
        mSwSoundlevelMonitor = findViewById(R.id.sw_soundlevelandspectrum_soundlevel_monitor);
        mSwSpectrumMonitor = findViewById(R.id.sw_soundlevelandspectrum_spectrum_monitor);
        mPbCaptureSoundLevel = findViewById(R.id.pb_sound_level);
        mTvSoundlevelandspectrumUserid = findViewById(R.id.tv_soundlevelandspectrum_userid);
        mTvSoundlevelandspectrumStreamid = findViewById(R.id.tv_soundlevelandspectrum_streamid);
        mCaptureSpectrumView = findViewById(R.id.soundlevelandspectrum_spectrum_view);
        ll_container = findViewById(R.id.ll_container);

        mTvSoundlevelandspectrumRoomid.setText(roomID);
        mSwSoundlevelMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mSDKEngine.startSoundLevelMonitor();
                }else {
                    mSDKEngine.stopSoundLevelMonitor();
                }
            }
        });
        mSwSpectrumMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mSDKEngine.startAudioSpectrumMonitor();
                }else {
                    mSDKEngine.stopAudioSpectrumMonitor();
                }
            }
        });
        // 创建引擎
        mSDKEngine = ZegoExpressEngine.createEngine(GetAppIDConfig.appID, GetAppIDConfig.appSign, true, ZegoScenario.GENERAL, this.getApplication(), null);
        // 增加本专题所用的回调
        mSDKEngine.setEventHandler(new IZegoEventHandler() {

            // 由于本专题中声浪需要做动画效果，这里使用两个实例变量来保存上一次SDK声浪回调中抛出的值，以实现过度动画的效果
            // 上一次本地采集的进度值
            private double last_progress_captured = 0.0;
            // 默认情况SDK默认支持最多拉12路流，这里使用一个12长度的int数值来保存所拉的流监控周期
            private HashMap<String, Double> last_stream_to_progress_value = new HashMap();

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList) {
                super.onRoomStreamUpdate(roomID, updateType, streamList);
                Log.v(TAG, "onRoomStreamUpdate: roomID" + roomID + ", updateType:" + updateType.value() + ", streamList: " + streamList);

                // 这里拉流之后动态添加渲染的View
                if(updateType == ZegoUpdateType.ADD){
                    for(ZegoStream zegoStream: streamList){
                        mSDKEngine.startPlayingStream(zegoStream.streamID, new ZegoCanvas(null));
                        SoundLevelAndSpectrumItem soundLevelAndSpectrumItem = new SoundLevelAndSpectrumItem(SoundLevelAndSpectrumMainActivity.this, null);
                        ll_container.addView(soundLevelAndSpectrumItem);
                        soundLevelAndSpectrumItem.getTvStreamId().setText(zegoStream.streamID);
                        soundLevelAndSpectrumItem.getTvUserId().setText(zegoStream.user.userID);
                        soundLevelAndSpectrumItem.setStreamid(zegoStream.streamID);
                        last_stream_to_progress_value.put(zegoStream.streamID, 0.0);
                        frequencySpectrumAndSoundLevelItemList.add(soundLevelAndSpectrumItem);

                    }
                }else if(updateType == ZegoUpdateType.DELETE){
                    for(ZegoStream zegoStream: streamList){
                        mSDKEngine.stopPlayingStream(zegoStream.streamID);
                        Iterator<SoundLevelAndSpectrumItem> it = frequencySpectrumAndSoundLevelItemList.iterator();
                        while(it.hasNext()){
                            SoundLevelAndSpectrumItem soundLevelAndSpectrumItemTmp = it.next();
                            if(soundLevelAndSpectrumItemTmp.getStreamid().equals(zegoStream.streamID)){
                                it.remove();
                                ll_container.removeView(soundLevelAndSpectrumItemTmp);
                                last_stream_to_progress_value.remove(zegoStream.streamID);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCapturedSoundLevelUpdate(double soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                Log.v(TAG, "onCapturedSoundLevelUpdate:" + soundLevel);
                ValueAnimator animator = ValueAnimator.ofFloat((float) last_progress_captured, (float)soundLevel).setDuration(100);
                animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        mPbCaptureSoundLevel.setProgress((int)((Float)valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                animator.start();
                last_progress_captured = soundLevel;
            }
            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Double> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                Log.v(TAG, "onRemoteSoundLevelUpdate:"+ soundLevels.size());

                Iterator<HashMap.Entry<String, Double>> it = soundLevels.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry<String, Double> entry = it.next();
                    String streamid = entry.getKey();
                    Double value = entry.getValue();
                    for(final SoundLevelAndSpectrumItem soundLevelAndSpectrumItem: frequencySpectrumAndSoundLevelItemList){
                        if(streamid.equals(soundLevelAndSpectrumItem.getStreamid())){
                            ValueAnimator animator = ValueAnimator.ofFloat(value.floatValue(), soundLevels.get(streamid).floatValue()).setDuration(100);
                            animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    soundLevelAndSpectrumItem.getPbSoundLevel().setProgress(((Float)(valueAnimator.getAnimatedValue())).intValue());
                                }
                            });
                            animator.start();
                            last_stream_to_progress_value.put(streamid, value);
                        }
                    }
                }
            }
            @Override
            public void onCapturedAudioSpectrumUpdate(float[] frequencySpectrum) {
                super.onCapturedAudioSpectrumUpdate(frequencySpectrum);
                Log.v(TAG, "call back onCapturedAudioSpectrumUpdate");
                mCaptureSpectrumView.updateFrequencySpectrum(frequencySpectrum);
            }
            @Override
            public void onRemoteAudioSpectrumUpdate(HashMap<String, float[]> frequencySpectrums) {
                super.onRemoteAudioSpectrumUpdate(frequencySpectrums);
                Log.v(TAG, "call back onRemoteAudioSpectrumUpdate:" + frequencySpectrums);

                Iterator<HashMap.Entry<String, float[]>> it = frequencySpectrums.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry<String, float[]> entry = it.next();
                    String streamid = entry.getKey();
                    float[] values = entry.getValue();

                    for(SoundLevelAndSpectrumItem soundLevelAndSpectrumItem: frequencySpectrumAndSoundLevelItemList){
                        if(streamid.equals(soundLevelAndSpectrumItem.getStreamid())){
                            soundLevelAndSpectrumItem.getSpectrumView().updateFrequencySpectrum(values);
                        }
                    }
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {

                Log.v(TAG, "onRoomStateUpdate: errorcode:"+ errorCode + ", roomID: "+ roomID + ", state:" + state.value());
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {

                Log.v(TAG, "onPublisherStateUpdate: errorcode:"+ errorCode + ", streamID:" + streamID + ", state:" + state.value());

            }
        });


        userID = "userid-" + randomSuffix;
        userName = "username-" + randomSuffix;
        streamID = "streamid-" + randomSuffix;

        mTvSoundlevelandspectrumUserid.setText(userID);
        mTvSoundlevelandspectrumStreamid.setText(streamID);

        ZegoRoomConfig config = new ZegoRoomConfig();
        /* 使能用户登录/登出房间通知 */
        /* Enable notification when user login or logout */
        config.isUserStatusNotify = true;
        mSDKEngine.loginRoom(roomID, new ZegoUser(userID, userName), config);
        // 本专题展示声浪与频谱，无需推视频流
        mSDKEngine.enableCamera(false);
        mSDKEngine.startPublishingStream(streamID);

    }

    @Override
    protected void onDestroy() {

        mSDKEngine.stopAudioSpectrumMonitor();
        mSDKEngine.stopSoundLevelMonitor();


        mSDKEngine.stopPublishingStream();
        mSDKEngine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);

        super.onDestroy();
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SoundLevelAndSpectrumMainActivity.class);
        activity.startActivity(intent);
    }
}
