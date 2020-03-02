package im.zego.common.application;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

import im.zego.common.widgets.log.FloatingView;

/**
 * Created by zego on 2018/10/16.
 */

public class ZegoApplication extends Application {

    public static ZegoApplication zegoApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        zegoApplication = this;

        initBugly();

        // 添加悬浮日志视图
        FloatingView.get().add();
    }

    /**
     * 初始化腾讯bug管理平台
     */
    private void initBugly() {
        /* Bugly SDK初始化
         * 参数1：上下文对象
         * 参数2：APPID，平台注册时得到,注意替换成你的appId
         * 参数3：是否开启调试模式，调试模式下会输出'CrashReport'tag的日志
         * 注意：如果您之前使用过Bugly SDK，请将以下这句注释掉。
         */
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
//        strategy.setAppVersion(AppUtils.getAppVersionName());
//        strategy.setAppPackageName(AppUtils.getAppPackageName());
        strategy.setAppReportDelay(20000);                          //Bugly会在启动20s后联网同步数据

        /*  第三个参数为SDK调试模式开关，调试模式的行为特性如下：
            输出详细的Bugly SDK的Log；
            每一条Crash都会被立即上报；
            自定义日志将会在Logcat中输出。
            建议在测试阶段建议设置成true，发布时设置为false。*/

        CrashReport.initCrashReport(getApplicationContext(), "d53f2eacfe", false);
    }
}
