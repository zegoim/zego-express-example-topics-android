package im.zego.express_example_audio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import im.zego.customaudioio.AudioCustomActivity;
import im.zego.express_example_audio.audio_talk.AudioTalkActivity;
import im.zego.express_example_audio.quick_start.QuickStartActivity;

public class MainActivity extends AppCompatActivity {

    private String[] topics = {
            "QuickStart",
            "AudioTalk",
            "CustomAudioIO"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, topics);
        ListView listView = findViewById(R.id.main_topic_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, QuickStartActivity.class);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, AudioTalkActivity.class);
                        break;
                    case 2:
                        intent =new Intent(MainActivity.this, AudioCustomActivity.class);
                    default:
                        break;
                }

                if (intent != null && checkOrRequestPermission()) {
                    startActivity(intent);
                }
            }
        });
    }

    // Check and request permission
    public boolean checkOrRequestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
                return false;
            }
        }
        return true;
    }
}

