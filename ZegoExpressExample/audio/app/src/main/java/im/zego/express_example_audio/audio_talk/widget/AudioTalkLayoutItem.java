//
//  AudioTalkLayoutItem.java
//  ZegoExpressExampleAudio
//  im.zego.express_example_audio.audio_talk.widget
//
//  Created by Patrick Fu on 2020/06/01.
//  Copyright © 2020 Zego. All rights reserved.
//

package im.zego.express_example_audio.audio_talk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import im.zego.express_example_audio.R;
import im.zego.express_example_audio.databinding.ActivityAudioTalkLayoutItemBinding;

public class AudioTalkLayoutItem extends LinearLayout {

    ActivityAudioTalkLayoutItemBinding binding;

    private String streamID;

    public AudioTalkItemView getSpectrumView() {
        return binding.spectrumView;
    }

    public TextView getUserIdTextView() {
        return binding.userIdTextView;
    }

    public TextView getStreamIdTextView() {
        return binding.streamIdTextView;
    }

    public String getStreamId() {
        return streamID;
    }

    public void setStreamId(String streamid) {
        this.streamID = streamid;
    }

    public ProgressBar getSoundLevelProgressBar() {
        return binding.soundLevelProgressBar;
    }

    public AudioTalkLayoutItem(Context context){

        super(context);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.activity_audio_talk_layout_item, this, true);

    }

}

