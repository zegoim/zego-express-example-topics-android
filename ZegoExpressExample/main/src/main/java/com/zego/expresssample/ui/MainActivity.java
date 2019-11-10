package com.zego.expresssample.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.zego.expresssample.R;
import com.zego.expresssample.databinding.ActivityMainBinding;
import com.zego.expresssample.adapter.MainAdapter;
import com.zego.expresssample.entity.ModuleInfo;
import com.zego.common.ui.BaseActivity;
import com.zego.common.ui.WebActivity;
import com.zego.play.ui.InitSDKPlayActivityUI;
import com.zego.publish.ui.InitSDKPublishActivityUI;


public class MainActivity extends BaseActivity {


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

        setTitle("示例代码");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.setting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SettingActivity.actionStart(MainActivity.this);
            }
        });


        mainAdapter.setOnItemClickListener((view, position) -> {
            boolean orRequestPermission = checkOrRequestPermission(REQUEST_PERMISSION_CODE);
            ModuleInfo moduleInfo = (ModuleInfo) view.getTag();
            if (orRequestPermission) {
                switch (moduleInfo.getModule()) {
                    case "推流":
                        InitSDKPublishActivityUI.actionStart(MainActivity.this);
                        break;
                    case "拉流":
                        InitSDKPlayActivityUI.actionStart(MainActivity.this);
                        break;
                }
            }
        });

        // UI Setting
        binding.moduleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.moduleList.setAdapter(mainAdapter);
        binding.moduleList.setItemAnimator(new DefaultItemAnimator());

        // Add Module
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName("推流").titleName("快速开始"));
        mainAdapter.addModuleInfo(new ModuleInfo()
                .moduleName("拉流"));
    }


    public void jumpSourceCodeDownload(View view) {
        WebActivity.actionStart(this, "https://github.com/zegodev/liveroom-topics-android", ((TextView) view).getText().toString());
    }

    public void jumpCommonProblems(View view) {
        WebActivity.actionStart(this, "https://doc.zego.im/CN/496.html", ((TextView) view).getText().toString());
    }

    public void jumpDoc(View view) {
        WebActivity.actionStart(this, " https://doc.zego.im/CN/303.html", ((TextView) view).getText().toString());
    }

    public void jumpWebRtc() {
        WebActivity.actionStart(this, "https://bansheehannibal.github.io/webrtcDemo/", "WebRtc");
    }
}
