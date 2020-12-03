package im.zego.common.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import im.zego.common.BR;

public class PerformanceStatus extends BaseObservable {
    private String cpuUsageApp = "";
    private String cpuUsageSystem = "";
    private String memoryUsageApp = "";
    private String memoryUsageSystem = "";
    private String memoryUsedApp = "";
    @Bindable
    public String getCpuUsageApp() {
        return cpuUsageApp;
    }

    public void setCpuUsageApp(String cpuUsageApp) {
        this.cpuUsageApp = cpuUsageApp;
        notifyPropertyChanged(BR.cpuUsageApp);
    }
    @Bindable
    public String getCpuUsageSystem() {
        return cpuUsageSystem;
    }

    public void setCpuUsageSystem(String cpuUsageSystem) {
        this.cpuUsageSystem = cpuUsageSystem;
        notifyPropertyChanged(BR.cpuUsageSystem);
    }
    @Bindable
    public String getMemoryUsageApp() {
        return memoryUsageApp;
    }

    public void setMemoryUsageApp(String memoryUsageApp) {
        this.memoryUsageApp = memoryUsageApp;
        notifyPropertyChanged(BR.memoryUsageApp);
    }
    @Bindable
    public String getMemoryUsageSystem() {
        return memoryUsageSystem;
    }

    public void setMemoryUsageSystem(String memoryUsageSystem) {
        this.memoryUsageSystem = memoryUsageSystem;
        notifyPropertyChanged(BR.memoryUsageSystem);
    }
    @Bindable
    public String getMemoryUsedApp() {
        return memoryUsedApp;
    }

    public void setMemoryUsedApp(String memoryUsedApp) {
        this.memoryUsedApp = memoryUsedApp;
        notifyPropertyChanged(BR.memoryUsedApp);
    }


}
