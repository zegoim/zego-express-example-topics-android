package im.zego.express.mixing;

import android.content.Context;
import android.util.Log;

import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import im.zego.zegoexpress.constants.ZegoAudioChannel;
import im.zego.zegoexpress.constants.ZegoAudioSampleRate;
import im.zego.zegoexpress.entity.ZegoAudioFrameParam;
import im.zego.zegoexpress.entity.ZegoAudioMixingData;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_16K;
import static im.zego.zegoexpress.constants.ZegoAudioSampleRate.ZEGO_AUDIO_SAMPLE_RATE_22K;

public class ZGMixingDemo {

    private final int DURATION = 30000;
    private ZegoAudioSampleRate mSampleRate;
    private ZegoAudioChannel mChannels = ZegoAudioChannel.STEREO;;
    private int mBitRate;

    private static ZGMixingDemo zgMixingDemo = null;
    private ZegoAudioMixingData mZegoAudioAux = null;

    public static ZGMixingDemo sharedInstance() {
        if (zgMixingDemo == null) {
            synchronized (ZGMixingDemo.class) {
                if (zgMixingDemo == null) {
                    zgMixingDemo = new ZGMixingDemo();
                }
            }
        }
        return zgMixingDemo;
    }

    public ZegoAudioMixingData getZegoAudioAux() {
        if (mZegoAudioAux == null) {
            mZegoAudioAux = new ZegoAudioMixingData();
        }
        return mZegoAudioAux;
    }

    public String getPath(Context context, String fileName) {
        String path = context.getExternalCacheDir().getPath();
        File pathFile = new File(path + "/" + fileName);
        if (!pathFile.exists()) {
            copyFileFromAssets(context, fileName, pathFile.getPath());
        }
        return pathFile.getPath();
    }

    /**
     * 从assets目录下拷贝文件到存储卡.
     *
     * @param context            安卓上下文：用于获取 assets 目录下的资源
     * @param assetsFilePath     assets文件的路径名如：xxx.mp3
     * @param targetFileFullPath sd卡目标文件路径如：/sdcard/xxx.mp3
     *
     */
    public static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {
        Log.d("Tag", "copyFileFromAssets ");
        InputStream assetsFileInputStream;
        try {
            assetsFileInputStream = context.getAssets().open(assetsFilePath);
            copyFile(assetsFileInputStream, targetFileFullPath);
        } catch (IOException e) {
            Log.d("Tag", "copyFileFromAssets " + "IOException-" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 处理混音传递pcm数据给SDK
    protected InputStream mBackgroundMusic = null;

    private byte[] dataBuf = new byte[1];
    private ByteBuffer mPcmBuffer = ByteBuffer.allocateDirect(1);

    public ZegoAudioMixingData handleAuxCallback(String pcmFilePath, int exceptDataLength) {

        if (dataBuf.length != exceptDataLength) {
            dataBuf = new byte[exceptDataLength];
        }
        if (mPcmBuffer.capacity() != exceptDataLength) {
            mPcmBuffer = ByteBuffer.allocateDirect(exceptDataLength);
        }
        mPcmBuffer.clear();

        ZegoAudioMixingData auxDataEx = getZegoAudioAux();

        try {

            if (mBackgroundMusic == null) {
                if (!pcmFilePath.equals("")) {
                    mBackgroundMusic = new FileInputStream(pcmFilePath);
                }
            }

            int len = mBackgroundMusic.read(dataBuf);

            if (len > 0) {
                mPcmBuffer.put(dataBuf, 0, exceptDataLength);
                auxDataEx.audioData = mPcmBuffer;
                auxDataEx.audioDataLength = len;
            } else {
                // 歌曲播放完毕
                mBackgroundMusic.close();
                mBackgroundMusic = null;
                auxDataEx.audioData = mPcmBuffer;
                auxDataEx.audioDataLength = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        param.channel = mChannels;
        param.sampleRate = mSampleRate;
        auxDataEx.param = param;

        return auxDataEx;
    }

    ZegoAudioFrameParam param = new ZegoAudioFrameParam();

    private static void copyFile(InputStream in, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(targetPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            in.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // mp3格式转pcm
    public void MP3ToPCM(String mp3FilePath, String pcmFilePath) {
        FileOutputStream fos = null;

        try {
            byte[] pcmData = decodeToPCM(mp3FilePath, 0, DURATION);

            if (pcmData != null) {

                File outfile = new File(pcmFilePath);
                if (!outfile.exists()) {
                    fos = new FileOutputStream(outfile);
                    fos.write(pcmData);
                    fos.close();
                }
            }

        } catch (IOException e) {
            Log.e("Zego", "io exception happened to mp3 to pcm");
        }
    }

    // mp3解码为pcm数据
    private static byte[] decodeToPCM(String mp3FilePath, int startMs, int maxMs)
            throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

        float totalMs = 0;
        boolean seeking = true;

        File file = new File(mp3FilePath);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024);

        try {

            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();

            boolean done = false;
            while (!done) {
                Header frameHeader = bitstream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    totalMs += frameHeader.ms_per_frame();

                    if (totalMs >= startMs) {
                        seeking = false;
                    }

                    if (!seeking) {
                        SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

                        if (output.getSampleFrequency() != 44100
                                || output.getChannelCount() != 2) {
                            Log.e("Zego", "mono or non-44100 MP3 not supported");
                        }

                        short[] pcm = output.getBuffer();
                        for (short s : pcm) {
                            outStream.write(s & 0xff);
                            outStream.write((s >> 8) & 0xff);
                        }
                    }

                    if (totalMs >= (startMs + maxMs)) {
                        done = true;
                    }
                }
                bitstream.closeFrame();
            }

            return outStream.toByteArray();
        } catch (BitstreamException e) {
            Log.e("Zego", "Bitstream error: " + e.getMessage());
            throw new IOException("Bitstream error: " + e);

        } catch (DecoderException e) {
            Log.e("Zego", "Decoder error " + e.getMessage());
        } finally {
            inputStream.close();
        }
        return null;
    }

    // 获取mp3文件采样率等信息
    public void getMP3FileInfo(String mp3FilePath) {

        try {
            MP3File mp3File = new MP3File(mp3FilePath);
            MP3AudioHeader header = mp3File.getMP3AudioHeader();
            int timeLen = header.getTrackLength();
            String bitrate = header.getBitRate();
            mBitRate = Integer.valueOf(bitrate);

            String format = header.getFormat();
            String channels = header.getChannels();
            if (channels.contains("Stereo")) {
                mChannels = ZegoAudioChannel.STEREO;
            } else {
                mChannels = ZegoAudioChannel.MONO;
            }

            String samplerate = header.getSampleRate();
            Log.e("Zego", "get mp3file sampleRate: " + samplerate);
            mSampleRate = ZegoAudioSampleRate.getZegoAudioSampleRate(Integer.parseInt(samplerate));

        } catch (Exception e) {
            Log.e("Zego", "get mp3file info exception");
            e.printStackTrace();
        }
    }
}
