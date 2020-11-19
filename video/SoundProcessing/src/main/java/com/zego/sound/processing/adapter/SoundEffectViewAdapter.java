package com.zego.sound.processing.adapter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.viewpager.widget.PagerAdapter;

import im.zego.common.widgets.ArcSeekBar;
import im.zego.common.widgets.CustomMinSeekBar;
import im.zego.common.widgets.RelativeRadioGroup;

import com.zego.sound.processing.R;
import com.zego.sound.processing.view.FlowRadioGroup;

import java.util.ArrayList;
import java.util.List;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoReverbPreset;
import im.zego.zegoexpress.constants.ZegoVoiceChangerPreset;
import im.zego.zegoexpress.entity.ZegoReverbEchoParam;
import im.zego.zegoexpress.entity.ZegoReverbParam;
import im.zego.zegoexpress.entity.ZegoVoiceChangerParam;

/**
 * 音效PagerAdapter，创建的View不能被回收，因此需在ViewPager中进行 setOffscreenPageLimit()设置
 */
public class SoundEffectViewAdapter extends PagerAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // 音效设置View的数量
    private final static int VIEW_COUNT = 4;

    private final static int TEXT_COLOR_SELECTED = Color.parseColor("#0d70ff");
    private final static int TEXT_COLOR_UNSELECTED = Color.parseColor("#333333");


    // 无
    public static final float VOICE_CHANGE_NO = 0.0f;

    // 萝莉
    public static final float VOICE_CHANGE_LOLI = 7.0f;

    // 大叔
    public static final float VOICE_CHANGE_UNCLE = -3f;
    //
    // View的分组
    private final static int VIEW_GROUP_VOICE_CHANGE = 0x10;  // 变声

    // checkBox 列表
    private List<CheckBox> checkBoxList;
    // 当前checkBox的状态
    private boolean currentCheckBoxState;

    private List<TextView> voiceChangeTextViewList;
    private List<TextView> stereoTextViewList;
    private List<TextView> mixedTextViewList;
    private List<ZegoReverbEchoParam> zegoReverbEchoParamDatas;
    private OnSoundEffectChangedListener onSoundEffectChangedListener;
    private OnSoundEffectAuditionCheckedListener onSoundEffectAuditionCheckedListener;

    private Context context;
    private AudioManager audioManager;
    private BroadcastReceiver headSetBroadcastReceiver;
    private Window window;
    private SpinnerAdapter spinnerAdapter = null;
    private Spinner spinner;
    private RadioGroup radioGroup;
    private List<View> views;
    public SoundEffectViewAdapter(Context context, Window window) {
        checkBoxList = new ArrayList<>(3);
        currentCheckBoxState = false;
        this.window = window;
        voiceChangeTextViewList = new ArrayList<>(3);
        stereoTextViewList = new ArrayList<>(3);
        mixedTextViewList = new ArrayList<>(3);
        initZegoReverbEchoParamDatas();
        initViews(context);
        // 初始化耳机相关监听
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        headSetBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    if (intent.hasExtra("state")) {
                        int state = intent.getIntExtra("state", -1);
                        //  耳机 拔出
                        if (state == 0) {
                            // 当勾选的情况
                            if (currentCheckBoxState && checkBoxList != null && !checkBoxList.isEmpty()) {
                                // 切换状态
                                checkBoxList.get(0).toggle();
                            }
                        } else if (state == 1) {
                            // DO NOTHING
                        }
                    }
                }
            }
        };
        this.context = context;
        context.registerReceiver(headSetBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    private void initViews(Context context) {
        views=new ArrayList<>();
        View view1= LayoutInflater.from(context).inflate(R.layout.voice_change_item_layout,null);
        View view2= LayoutInflater.from(context).inflate(R.layout.stereo_item_layout,null);
        View view3= LayoutInflater.from(context).inflate(R.layout.reverb_item_layout,null);
        View view4= LayoutInflater.from(context).inflate(R.layout.reverb_echo_item,null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);
    }

    private void initZegoReverbEchoParamDatas() {
        zegoReverbEchoParamDatas = new ArrayList<>();
        initZegoReverbEchoParamNone();
        initZegoReverbEchoParamEthereal();
        initZegoReverbEchoParamRobot();
    }

    private void initZegoReverbEchoParamRobot() {
        ZegoReverbEchoParam echoParam3 = new ZegoReverbEchoParam();
        echoParam3.inGain= 0.8f;
        echoParam3.outGain =1.0f;
        echoParam3.numDelays = 7;
        int[] delay ={60,120,180,240,300,360,420};
        echoParam3.delay=delay;

        float[] decay={0.51f,0.26f,0.12f,0.05f,0.02f,0.009f,0.001f};
        echoParam3.decay=decay;
        zegoReverbEchoParamDatas.add(echoParam3);
    }

    private void initZegoReverbEchoParamEthereal() {
        ZegoReverbEchoParam echoParam2 = new ZegoReverbEchoParam();
        echoParam2.inGain= 0.8f;
        echoParam2.outGain =1.0f;
        echoParam2.numDelays = 7;
        int[] delay ={230,460,690,920,1150,1380,1610};
        echoParam2.delay=delay;

        float[] decay={0.41f,0.18f,0.08f,0.03f,0.009f,0.003f,0.001f};
        echoParam2.decay=decay;
        zegoReverbEchoParamDatas.add(echoParam2);
    }

    private void initZegoReverbEchoParamNone() {
        ZegoReverbEchoParam echoParam = new ZegoReverbEchoParam();
        echoParam.inGain = 1;
        echoParam.outGain = 1;
        echoParam.numDelays = 0;
        for (int i = 0; i < 7; i++) {
            echoParam.delay[i] = 0;
        }
        for (int i = 0; i < 7; i++) {
            echoParam.decay[i] = 0;
        }
        zegoReverbEchoParamDatas.add(echoParam);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return VIEW_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        TextView textView;
        CheckBox checkBox = null;
        View view = null;
        // 变声
        if (position == 0) {
            view = views.get(position);
            radioGroup = view.findViewById(R.id.rg_voice);
            checkBox = view.findViewById(R.id.checkbox);
            final CustomMinSeekBar customMinSeekBar = view.findViewById(R.id.tones);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    ZegoExpressEngine.getEngine().setVoiceChangerParam(new ZegoVoiceChangerParam());
                    if (checkedId == R.id.no) {
//                        customMinSeekBar.setCurrentValue(VOICE_CHANGE_NO);
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.NONE);
                        }
                    } else if (checkedId == R.id.loli) {
//                        customMinSeekBar.setCurrentValue(VOICE_CHANGE_LOLI);
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.MEN_TO_CHILD);
                        }
                    } else if (checkedId == R.id.uncle) {
//                        customMinSeekBar.setCurrentValue(VOICE_CHANGE_UNCLE);
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.WOMEN_TO_MEN);
                        }
                    } else if (checkedId == R.id.foreigner) {
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.FOREIGNER);
                        }
                    } else if (checkedId == R.id.optimus_prime) {
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.OPTIMUS_PRIME);
                        }
                    } else if (checkedId == R.id.android) {
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.ANDROID);
                        }
                    } else if (checkedId == R.id.ethereal) {
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.ETHEREAL);
                        }
                    }else if(checkedId == R.id.male_magnetic){
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.MALE_MAGNETIC);
                        }
                    }else if(checkedId == R.id.fresh){
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangePreset(ZegoVoiceChangerPreset.FEMALE_FRESH);
                        }
                    }else if(checkedId == R.id.custom){
                        if (onVoiceChangeListener != null) {
                            onVoiceChangeListener.onVoiceChangeParam(customMinSeekBar.getCurrentValue());
                        }
                    }

                }
            });

            customMinSeekBar.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
//                    if (progress == 0.0f) {
//                        radioGroup.check(R.id.no);
//                    } else if (progress == 7.0f) {
//                        radioGroup.check(R.id.loli);
//                    } else if (progress == -3f) {
//                        radioGroup.check(R.id.uncle);
//                    } else {
//                        radioGroup.check(R.id.custom);
//                    }
                    radioGroup.check(R.id.custom);
                    if (onVoiceChangeListener != null) {
                        onVoiceChangeListener.onVoiceChangeParam(progress);
                    }
                }
            });

        } else if (position == 1) {
            // 立体声
            view = views.get(position);
            ArcSeekBar arcSeekBar = view.findViewById(R.id.angle_seek_bar);
            final TextView angle = view.findViewById(R.id.angle);
            checkBox = view.findViewById(R.id.checkbox);
            final String txAngle = context.getString(R.string.tx_angle);
            arcSeekBar.setOnProgressChangeListener(new ArcSeekBar.OnProgressChangeListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onProgressChanged(ArcSeekBar seekBar, int progress, boolean isUser) {

                    angle.setText(txAngle + seekBar.getProgress() + " °");
                }

                @Override
                public void onStartTrackingTouch(ArcSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(ArcSeekBar seekBar) {
                    if (onStereoChangeListener != null) {
                        onStereoChangeListener.onStereoChangeParam(seekBar.getProgress());
                    }
                }
            });
            // 默认90°
            arcSeekBar.setProgress(90);
        } else if (position == 2) {
            // 混响
            view = views.get(position);
            final CustomMinSeekBar roomSize = view.findViewById(R.id.room_size);
            final FlowRadioGroup relativeRadioGroup = view.findViewById(R.id.rg_reverb);
            checkBox = view.findViewById(R.id.checkbox);
            roomSize.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.onRoomSizeChange(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });


            final CustomMinSeekBar damping = view.findViewById(R.id.damping);
            damping.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.onDamping(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });

            final CustomMinSeekBar reverberance = view.findViewById(R.id.reverberance);
            reverberance.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.onReverberance(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar wetGain = view.findViewById(R.id.wetGain);
            wetGain.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.wetGain(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar dryGain = view.findViewById(R.id.dryGain);
            dryGain.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.dryGain(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar toneLow = view.findViewById(R.id.toneLow);
            toneLow.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.toneLow(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar toneHigh = view.findViewById(R.id.toneHigh);
            toneHigh.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.toneHigh(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar preDelay = view.findViewById(R.id.preDelay);
            preDelay.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.preDelay(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final CustomMinSeekBar stereoWidth = view.findViewById(R.id.stereo_width);
            stereoWidth.setOnSeekBarChangeListener(new CustomMinSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, float progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar, float progress) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar, float progress) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.stereoWidth(progress);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            final Switch wetOnly =view.findViewById(R.id.wetOnly);
            wetOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (onReverberationChangeListener != null) {
                        onReverberationChangeListener.wetOnly(isChecked);
                    }
                    if (relativeRadioGroup != null) {
                        relativeRadioGroup.check(R.id.custom);
                    }
                }
            });
            relativeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.no) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(false, ZegoReverbPreset.NONE);
                        }
                    } else if (checkedId == R.id.concert_hall) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.CONCER_HALL);
                        }
                    } else if (checkedId == R.id.large_auditorium) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.VALLEY);
                        }
                    } else if (checkedId == R.id.warm_club) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.LARGE_ROOM);
                        }
                    } else if (checkedId == R.id.soft_room) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.SOFT_ROOM);
                        }
                    } else if (checkedId == R.id.custom) {
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onRoomSizeChange(roomSize.getCurrentValue());
                            onReverberationChangeListener.onDamping(damping.getCurrentValue());
                            onReverberationChangeListener.onReverberance(reverberance.getCurrentValue());
                            onReverberationChangeListener.wetOnly(wetOnly.isChecked());
                            onReverberationChangeListener.wetGain(wetGain.getCurrentValue());
                            onReverberationChangeListener.dryGain(dryGain.getCurrentValue());
                            onReverberationChangeListener.preDelay(preDelay.getCurrentValue());
                            onReverberationChangeListener.toneHigh(toneHigh.getCurrentValue());
                            onReverberationChangeListener.toneLow(toneLow.getCurrentValue());
                            onReverberationChangeListener.stereoWidth(stereoWidth.getCurrentValue());
                        }
                    }else if(checkedId ==R.id.record_studio){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.RECORDING_STUDIO);
                        }
                    }else if(checkedId ==R.id.basement){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.BASEMENT);
                        }
                    }else if(checkedId ==R.id.ktv){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.KTV);
                        }
                    }else if(checkedId ==R.id.popular){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.POPULAR);
                        }
                    }else if(checkedId ==R.id.rock){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.ROCK);
                        }
                    }else if(checkedId ==R.id.concer){
                        if (onReverberationChangeListener != null) {
                            onReverberationChangeListener.onAudioReverbModeChange(true, ZegoReverbPreset.VOCAL_CONCERT);
                        }
                    }
                }
            });

        } else if (position == 3) {
            view = views.get(position);
            checkBox = view.findViewById(R.id.checkbox);
            spinner = view.findViewById(R.id.sp_reverb_echo);
            spinnerAdapter = ArrayAdapter.createFromResource(view.getContext(), R.array.reverb_echo_mode, android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onReverberationEchoListener.onReverbEchoModeChange(zegoReverbEchoParamDatas.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }

        checkBox.setChecked(currentCheckBoxState);
        checkBox.setOnCheckedChangeListener(this);
        // 试听文案添加点击事件监听
        view.findViewById(R.id.audition_layout).setOnClickListener(this);
        checkBoxList.add(checkBox);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // do nothing
        container.removeView(views.get(position));
    }

    /**
     * 释放相关资源
     */
    public void release() {
        audioManager = null;
        context.registerReceiver(headSetBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.audition_layout) {
            CheckBox checkBox = ((ViewGroup) v).findViewById(R.id.checkbox);
            if (checkBox != null) {
                checkAndToggle(checkBox);
            }
            return;
        }
        // 如果是非选中，才进行处理
        if (!(boolean) v.getTag(R.id.sound_effect_selected_state)) {
            // 设置选中的字体颜色
            ((TextView) v).setTextColor(TEXT_COLOR_SELECTED);
            // 设置选中的图片背景
            setDrawableTopLevel((TextView) v, 2);
            // 设置选中状态
            v.setTag(R.id.sound_effect_selected_state, true);
            // 变声组
            if ((int) v.getTag(R.id.sound_effect_view_group_type) == VIEW_GROUP_VOICE_CHANGE) {
                for (TextView textView : voiceChangeTextViewList) {
                    // 重置其他没有选中的textView的状态
                    if (textView != v) {
                        // 设置字体颜色
                        textView.setTextColor(TEXT_COLOR_UNSELECTED);
                        setDrawableTopLevel(textView, 1);
                        textView.setTag(R.id.sound_effect_selected_state, false);
                    }
                }
            }

            // 进行音效状态改变回调
            if (onSoundEffectChangedListener != null) {
                onSoundEffectChangedListener.onSoundEffectChanged((int) v.getTag(R.id.sound_effect_type));
            }
        }
    }

    /**
     * 检查后 checkbox 切换状态
     *
     * @param checkBox 检查和切换状态的 checkbox
     */
    private void checkAndToggle(CheckBox checkBox) {
        if (!checkBox.isChecked()) {
            // 如果没有勾选，并且没有插耳机，提示
            if (!audioManager.isWiredHeadsetOn()) {
                Toast.makeText(context, "音效试听需要带上耳机才可使用", Toast.LENGTH_LONG).show();
            } else {
                // 否则，切换状态
                checkBox.toggle();
            }
        } else {
            checkBox.toggle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        currentCheckBoxState = isChecked;

        for (CheckBox checkBox : checkBoxList) {
            // 对其他对checkBox进行处理
            if (checkBox != buttonView) {
                // 先置null，避免重复调用
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(isChecked);
                checkBox.setOnCheckedChangeListener(this);
            }
        }

        if (onSoundEffectAuditionCheckedListener != null) {
            onSoundEffectAuditionCheckedListener.onSoundEffectAuditionChecked(isChecked);
        }
    }

    /**
     * 设置check点击回调事件
     */
    public void setOnSoundEffectAuditionCheckedListener(OnSoundEffectAuditionCheckedListener onSoundEffectAuditionCheckedListener) {
        this.onSoundEffectAuditionCheckedListener = onSoundEffectAuditionCheckedListener;
    }

    /**
     * 设置音效状态改变事件
     */
    public void setOnSoundEffectChangedListener(OnSoundEffectChangedListener onSoundEffectChangedListener) {
        this.onSoundEffectChangedListener = onSoundEffectChangedListener;
    }

    // 设置 textView drawableTop level 值
    private void setDrawableTopLevel(TextView textView, int level) {
        if (textView != null && textView.getCompoundDrawables()[1] != null) {
            textView.getCompoundDrawables()[1].setLevel(level);
        }
    }

    /**
     * 音效改变监听器
     */
    public interface OnSoundEffectChangedListener {

        /**
         * 音效类型改变回调
         *
         * @param soundEffectType 音效类型
         */
        void onSoundEffectChanged(int soundEffectType);
    }

    /**
     * 音效试听点击回调
     */
    public interface OnSoundEffectAuditionCheckedListener {
        void onSoundEffectAuditionChecked(boolean isChecked);
    }

    private OnVoiceChangeListener onVoiceChangeListener;
    private OnStereoChangeListener onStereoChangeListener;
    private OnReverberationChangeListener onReverberationChangeListener;
    private OnReverberationEchoListener onReverberationEchoListener;
    public void setOnVoiceChangeListener(OnVoiceChangeListener listener) {
        this.onVoiceChangeListener = listener;
    }

    public void setOnStereoChangeListener(OnStereoChangeListener listener) {
        this.onStereoChangeListener = listener;
    }

    public void setOnReverberationChangeListener(OnReverberationChangeListener listener) {
        this.onReverberationChangeListener = listener;
    }
    public void setOnReverberationEchoListener(OnReverberationEchoListener listener){
        this.onReverberationEchoListener =listener;
    }

    /**
     * 声音变化监听
     */
    public interface OnVoiceChangeListener {
        void onVoiceChangeParam(float param);

        void onVoiceChangePreset(ZegoVoiceChangerPreset mode);
    }


    /**
     * 立体声变化监听
     */
    public interface OnStereoChangeListener {
        void onStereoChangeParam(int param);
    }

    /**
     * 混响参数变化监听器
     */
    public interface OnReverberationChangeListener {
        void onAudioReverbModeChange(boolean enable, ZegoReverbPreset mode);

        void onRoomSizeChange(float param);

        void onDryWetRationChange(float param);

        void onDamping(float param);

        void onReverberance(float param);
        void wetOnly(boolean enable);
        void wetGain(float param);
        void dryGain(float param);
        void toneLow(float param);
        void toneHigh(float param);
        void preDelay(float param);
        void stereoWidth(float param);
    }
    public interface OnReverberationEchoListener {
        void onReverbEchoModeChange(ZegoReverbEchoParam mode);
    }


}
