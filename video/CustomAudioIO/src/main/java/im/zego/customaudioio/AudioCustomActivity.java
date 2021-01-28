package im.zego.customaudioio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

public class AudioCustomActivity extends Activity {
    private Button capture,render;
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, AudioCustomActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_custom);
        capture=findViewById(R.id.capture);
        render=findViewById(R.id.render);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(AudioCustomActivity.this, AudioCustomCaptureActivity.class);
                startActivity(intent);
            }
        });
        render.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(AudioCustomActivity.this, AudioCustomRenderActivity.class);
                startActivity(intent);
            }
        });
    }
}
