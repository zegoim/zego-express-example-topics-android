package com.zego.expresssample.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.zego.expresssample.R;
import com.zego.expresssample.databinding.ActivityMainBinding;
import com.zego.expresssample.adapter.MainAdapter;
import com.zego.expresssample.entity.ModuleInfo;
import com.zego.common.ui.WebActivity;
import com.zego.im.ui.IMActivity;
import com.zego.mixer.ui.MixerMainActivity;
import com.zego.quickstart.ui.BasicCommunicationActivity;


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
                    BasicCommunicationActivity.actionStart(com.zego.expresssample.ui.MainActivity.this);
                }
                else if (module.equals(getString(R.string.tx_module_mixer))) {
                    MixerMainActivity.actionStart(MainActivity.this);
                }
                else if (module.equals(getString(R.string.tx_module_im))) {
                    IMActivity.actionStart(MainActivity.this);
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
                .moduleName(getString(R.string.tx_module_im)).titleName(""));
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
