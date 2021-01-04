package im.zego.networktest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import java.util.regex.Pattern;

import im.zego.common.util.SettingDataUtil;
import im.zego.networktest.databinding.ActivityNetworkTestBinding;
import im.zego.networktest.databinding.NetworkQualityBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoTestNetworkConnectivityCallback;
import im.zego.zegoexpress.constants.ZegoNetworkSpeedTestType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestConfig;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestQuality;
import im.zego.zegoexpress.entity.ZegoTestNetworkConnectivityResult;

public class NetWorkTest extends Activity {
    private ActivityNetworkTestBinding binding;
    private ZegoExpressEngine engine;
    private NetworkQualityBinding upLinkQualityInclude,downLinkQualityInclude;
    private boolean uplinkOpen=true;
    private boolean downlinkOpen =true;
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, NetWorkTest.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_network_test);
        upLinkQualityInclude = binding.upLinkQualityInclude;
        downLinkQualityInclude = binding.downLinkQualityInclude;
        engine = ZegoExpressEngine.createEngine(SettingDataUtil.getAppId(), SettingDataUtil.getAppKey(), SettingDataUtil.getEnv(), SettingDataUtil.getScenario(), getApplication(), null);
        ZegoCanvas canvas = new ZegoCanvas(binding.preview);
        canvas.viewMode= ZegoViewMode.ASPECT_FILL;
        engine.startPreview(canvas);
        initView();
    }

    private void initView() {
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onNetworkSpeedTestError(int errorCode, ZegoNetworkSpeedTestType type) {
                if(type==ZegoNetworkSpeedTestType.DOWNLINK){
                    downLinkQualityInclude.networkType.setText("networkType:"+type +" errorCode: "+errorCode);
                    downLinkQualityInclude.qualityConnectCost.setText("");
                    downLinkQualityInclude.qualityLostRate.setText("");
                    downLinkQualityInclude.qualityRtt.setText("");

                }else{
                    upLinkQualityInclude.networkType.setText("networkType:"+type +" errorCode: "+errorCode);
                    upLinkQualityInclude.qualityConnectCost.setText("");
                    upLinkQualityInclude.qualityLostRate.setText("");
                    upLinkQualityInclude.qualityRtt.setText("");
                }
            }

            @Override
            public void onNetworkSpeedTestQualityUpdate(ZegoNetworkSpeedTestQuality quality, ZegoNetworkSpeedTestType type) {
                if(type==ZegoNetworkSpeedTestType.DOWNLINK){
                    downLinkQualityInclude.networkType.setText("networkType: "+type);
                    downLinkQualityInclude.qualityConnectCost.setText("connectCost: "+quality.connectCost);
                    downLinkQualityInclude.qualityLostRate.setText("packetLostRate:" +quality.packetLostRate);
                    downLinkQualityInclude.qualityRtt.setText("rtt:" +quality.rtt);
                }else {
                    upLinkQualityInclude.networkType.setText("networkType: "+type);
                    upLinkQualityInclude.qualityConnectCost.setText("connectCost: "+quality.connectCost);
                    upLinkQualityInclude.qualityLostRate.setText("packetLostRate:" +quality.packetLostRate);
                    upLinkQualityInclude.qualityRtt.setText("rtt:" +quality.rtt);
                }
            }
        });
//        binding.testConnectivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                engine.testNetworkConnectivity(new IZegoTestNetworkConnectivityCallback() {
//                    @Override
//                    public void onTestNetworkConnectivityCallback(int i, ZegoTestNetworkConnectivityResult zegoTestNetworkConnectivityResult) {
//                        binding.connectCost.setText("connectCost: "+zegoTestNetworkConnectivityResult.connectCost);
//                    }
//                });
//            }
//        });
        binding.uplinkOpen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.up_open){
                    uplinkOpen =true;
                }else {
                    uplinkOpen =false;
                }
            }
        });
        binding.downlinkOpen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.down_open){
                    downlinkOpen =true;
                }else {
                    downlinkOpen =false;
                }
            }
        });
        binding.startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZegoNetworkSpeedTestConfig config =new ZegoNetworkSpeedTestConfig();
                config.testUplink =uplinkOpen;
                config.testDownlink =downlinkOpen;
                String edExpectedDownLinkBitrate =binding.edExpectedDownLinkBitrate.getText().toString();
                String edExpectedUpLinkBitrate=binding.edExpectedUpLinkBitrate.getText().toString();
                if(edExpectedDownLinkBitrate!=null&&!edExpectedDownLinkBitrate.trim().equals("")){
                    if(checkIsNum(edExpectedDownLinkBitrate.trim(),"expectedDownlinkBitrate should be a number")){
                        config.expectedDownlinkBitrate =Integer.valueOf(edExpectedDownLinkBitrate.trim());
                    }
                }
                if(edExpectedUpLinkBitrate!=null&&!edExpectedUpLinkBitrate.trim().equals("")){
                    if(checkIsNum(edExpectedUpLinkBitrate.trim(),"expectedUplinkBitrate should be a number")){
                        config.expectedUplinkBitrate =Integer.valueOf(edExpectedUpLinkBitrate.trim());
                    }
                }
                engine.startNetworkSpeedTest(config);
            }
        });
        binding.stopTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.stopNetworkSpeedTest();
            }
        });
    }
    public boolean checkIsNum(String text,String msg){
        Pattern pattern = Pattern.compile("[0-9]*");
        if(!pattern.matcher(text).matches()){
            Toast.makeText(NetWorkTest.this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }
}
