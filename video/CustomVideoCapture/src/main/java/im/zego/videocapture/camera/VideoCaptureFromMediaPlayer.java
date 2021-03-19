package im.zego.videocapture.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import im.zego.common.util.AppLogger;
import im.zego.common.util.CommonTools;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerVideoHandler;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormat;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zegoexpress.entity.ZegoVideoFrameParam;

/**
 * VideoCaptureFromCamera
 * 实现从摄像头采集数据并传给ZEGO SDK，需要继承实现ZEGO SDK 的ZegoVideoCaptureDevice类
 * 采用内存拷贝方式传递数据，即YUV格式，通过client的onByteBufferFrameCaptured传递采集数据
 */

/**
 *  * VideoCaptureFromCamera
 *  * To collect data from the camera and pass it to the ZEGO SDK, you need to inherit the ZegoVideoCaptureDevice class that implements the ZEGO SDK
 *  * Use memory copy to transfer data, that is, YUV format, through client's onByteBufferFrameCaptured to transfer collected data
 *  
 */
public class VideoCaptureFromMediaPlayer extends ZegoVideoCaptureCallback {
        ZegoExpressEngine mSDKEngine = null;
        public static ZegoMediaPlayer mediaPlayer;
        private Context mContext;
        private TextureView preView;
        private ByteBuffer tempByteBuffer;
        public VideoCaptureFromMediaPlayer(Context context, ZegoExpressEngine mSDKEngine) {
            this.mContext = context;
            this.mSDKEngine = mSDKEngine;
            initMediaPlayer();
        }

        private void initMediaPlayer() {
           mediaPlayer = mSDKEngine.createMediaPlayer();
        }

    @Override
    public void setView(View view) {
        preView=(TextureView) view;
    }

    /**
         * 初始化资源，必须实现
         * Initialization of resources must be achieved
         */
        @Override
        public void onStart(ZegoPublishChannel channel) {
            useRGBA32Format();
//            useI420Format();
            mediaPlayer.loadResource(CommonTools.getPath(mContext, "ad.mp4"),new IZegoMediaPlayerLoadResourceCallback(){
                @Override
                public void onLoadResourceCallback(int errorCode) {
                    if(errorCode==0) {
                        mediaPlayer.setPlayerCanvas(null);
                        mSDKEngine.startPreview(new ZegoCanvas(preView));
                        mediaPlayer.enableRepeat(true);
                        mediaPlayer.start();
                    }else{
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,"Load MediaPlayer Resource Fail",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        public void useI420Format(){
            mediaPlayer.setVideoHandler(new IZegoMediaPlayerVideoHandler() {
                @Override
                public void onVideoFrame(ZegoMediaPlayer zegoMediaPlayer, ByteBuffer[] byteBuffers, int[] dataLength, ZegoVideoFrameParam zegoVideoFrameParam) {
                    int totalDataLength=byteBuffers[0].capacity()+byteBuffers[1].capacity()+byteBuffers[2].capacity();
                    if(tempByteBuffer==null||tempByteBuffer.capacity()!=totalDataLength) {
                        tempByteBuffer = ByteBuffer.allocateDirect(byteBuffers[0].capacity() + byteBuffers[1].capacity() + byteBuffers[2].capacity()).put(byteBuffers[0]).put(byteBuffers[1]).put(byteBuffers[2]);
                    }else {
                        tempByteBuffer.clear();
                        tempByteBuffer.put(byteBuffers[0]).put(byteBuffers[1]).put(byteBuffers[2]);
                    }
                    sendMediaPlayerDataToSDK(tempByteBuffer,tempByteBuffer.capacity(),zegoVideoFrameParam);
                }
            },ZegoVideoFrameFormat.I420);
        }
        private void useRGBA32Format(){
            mediaPlayer.setVideoHandler(new IZegoMediaPlayerVideoHandler() {
                @Override
                public void onVideoFrame(ZegoMediaPlayer zegoMediaPlayer, ByteBuffer[] byteBuffers, int[] dataLength, ZegoVideoFrameParam zegoVideoFrameParam) {
                    int totalDataLength=byteBuffers[0].capacity();
                    if(tempByteBuffer==null||tempByteBuffer.capacity()!=totalDataLength) {
                        tempByteBuffer = ByteBuffer.allocateDirect(byteBuffers[0].capacity()).put(byteBuffers[0]);
                    }else {
                        tempByteBuffer.clear();
                        tempByteBuffer.put(byteBuffers[0]);
                    }
                    sendMediaPlayerDataToSDK(tempByteBuffer,tempByteBuffer.capacity(),zegoVideoFrameParam);
                }
            },ZegoVideoFrameFormat.RGBA32);
        }

        private void sendMediaPlayerDataToSDK(ByteBuffer byteBuffers, int dataLength, ZegoVideoFrameParam zegoVideoFrameParam) {

            long now;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                now = SystemClock.elapsedRealtime();
            } else {
                now = TimeUnit.MILLISECONDS.toMillis(SystemClock.elapsedRealtime());
            }
            // 将采集的数据传给ZEGO SDK
            // Pass the collected data to ZEGO SDK
            mSDKEngine.sendCustomVideoCaptureRawData(byteBuffers, dataLength, zegoVideoFrameParam, now);
        }

        // 停止推流时，ZEGO SDK 调用 stopCapture 通知外部采集设备停止采集，必须实现
        // When stopping pushing, the ZEGO SDK calls stopCapture to notify the external collection device to stop collection, which must be implemented
        @Override
        public void onStop(ZegoPublishChannel channel) {
            mediaPlayer.stop();
            mediaPlayer.setVideoHandler(null,ZegoVideoFrameFormat.NV21);
        }



}
