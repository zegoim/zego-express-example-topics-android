package im.zego.aux.publisher.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.zego.aux.publisher.R;
import com.zego.aux.publisher.databinding.AuxLoginBinding;


public class ZGAuxPublisherLoginUI extends Activity {
    private AuxLoginBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.aux_login);
        binding.explanation.setText(getString(R.string.explanation));
    }

    public void jumpPublish(View view){
        ZGAuxPublisherPublishUI.actionStart(this);
    }
    public void jumpStart(View view){
        ZGAuxPublisherPlayUI.actionStart(this);
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, ZGAuxPublisherLoginUI.class);
        activity.startActivity(intent);
    }
}
