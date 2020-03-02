package im.zego.expresssample.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import im.zego.customrender.ui.ZGVideoRenderTypeUI;
import im.zego.expresssample.R;
import im.zego.expresssample.databinding.ActivityMainBinding;
import im.zego.expresssample.adapter.MainAdapter;
import im.zego.expresssample.entity.ModuleInfo;
import im.zego.common.ui.WebActivity;
import im.zego.im.ui.IMActivity;
import im.zego.mediaplayer.ui.MediaplayerMainActivity;
import im.zego.mixer.ui.MixerMainActivity;
import im.zego.quickstart.ui.BasicCommunicationActivity;
import im.zego.soundlevelandspectrum.ui.SoundLevelAndSpectrumMainActivity;
import im.zego.videocapture.ui.ZGVideoCaptureOriginUI;


public class MainActivity extends AppCompatActivity {


    private MainAdapter mainAdapter = new MainAdapter();
    private static final int REQUEST_PERMISSION_CODE = 101;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()) {
            /* If this is not the root activity */
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }

        setTitle(getString(R.string.tx_title));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainAdapter.setOnItemClickListener((view, position) -> {
            boolean orRequestPermission = this.checkOrRequestPermission(REQUEST_PERMISSION_CODE);
            ModuleInfo moduleInfo = (ModuleInfo) view.getTag();
            if (orRequestPermission) {
                String module = moduleInfo.getModule();
                if (module.equals(getString(R.string.tx_module_basic))) {
                    BasicCommunicationActivity.actionStart(MainActivity.this);
                }
                else if (module.equals(getString(R.string.tx_module_mixer))) {
                    MixerMainActivity.actionStart(MainActivity.this);
                }
                else if (module.equals(getString(R.string.tx_module_im))) {
                    IMActivity.actionStart(MainActivity.this);
                }else if(module.equals(getString(R.string.tx_module_soundlevelandspectrum))){
                    SoundLevelAndSpectrumMainActivity.actionStart(MainActivity.this);
                }else if(module.equals(getString(R.string.tx_module_mediaplayer))){
                    MediaplayerMainActivity.actionStart(MainActivity.this);
                }else if(module.equals(getString(R.string.tx_module_custom_render))){
                    ZGVideoRenderTypeUI.actionStart(MainActivity.this);
                }else if(module.equals(getString(R.string.tx_module_custom_capture))){
                    ZGVideoCaptureOriginUI.actionStart(MainActivity.this);
                }

            }
        });

        // UI Setting
        binding.moduleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.moduleList.setAdapter(mainAdapter);
        binding.moduleList.setItemAnimator(new DefaultItemAnimator());

        // Add Module
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_basic)).titleName(getString(R.string.tx_title_quickstart)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_mixer)).titleName(getString(R.string.tx_title_advance)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_im)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_soundlevelandspectrum)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_mediaplayer)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_custom_render)));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName(getString(R.string.tx_module_custom_capture)));
    }


    public void jumpSourceCodeDownload(View view) {
        WebActivity.actionStart(this, "https://github.com/zegoim/zego-express-example-topics-android", ((TextView) view).getText().toString());
    }

    public void jumpQuickStart(View view) {
        WebActivity.actionStart(this, "https://doc-zh.zego.im/zh/727.html", ((TextView) view).getText().toString());
    }

    public void jumpDoc(View view) {
        WebActivity.actionStart(this, "https://doc-zh.zego.im/zh/303.html", ((TextView) view).getText().toString());
    }

    // 需要申请 麦克风权限-读写sd卡权限-摄像头权限
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};

    /**
     * 校验并请求权限
     */
    public boolean checkOrRequestPermission(int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, code);
                return false;
            }
        }
        return true;
    }
}