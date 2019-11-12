package com.zego.common.entity;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.zego.common.BR;

/**
 * Created by zego on 2019/3/20.
 */

public class OperationInfo extends BaseObservable {

    private String initResult = "";
    private String loginResult = "";
    private String handleStreamResult = "";

    @Bindable
    public String getInitResult() {
        return initResult;
    }

    public void setInitResult(String initResult) {
        this.initResult = initResult;
        notifyPropertyChanged(BR.initResult);
    }

    @Bindable
    public String getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(String loginResult) {
        this.loginResult = loginResult;
        notifyPropertyChanged(BR.loginResult);
    }

    @Bindable
    public String getHandleStreamResult() {
        return handleStreamResult;
    }

    public void setHandleStreamResult(String handleStreamResult) {
        this.handleStreamResult = handleStreamResult;
        notifyPropertyChanged(BR.handleStreamResult);
    }
}
