# Zego Express Example Topics Android (Java)

[English](README.md) | [中文](README_zh.md)

Zego Express Example Topics Video Android Demo (Java)

## Download SDK

The SDK `libZegoExpressEngine.so` and `ZegoExpressEngine.jar` required to run the Demo project is missing from this Repository, and needs to be downloaded and placed in the Libs folder of the Demo project.

**[https://storage.zego.im/express/video/android/zego-express-video-android.zip](https://storage.zego.im/express/video/android/zego-express-video-android.zip)**

finally, the structure under the `ZegoExpressExample/main/libs` directory should be as follows:

```tree
.
├── README.md
├── README_zh.md
└── ZegoExpressExample/main
                        └── libs
                            ├── ZegoExpressEngine.jar
                            ├── arm64-v8a
                            │   └── libZegoExpressEngine.so
                            ├── armeabi-v7a
                            │   └── libZegoExpressEngine.so
                            └── x86
                                └── libZegoExpressEngine.so
```

## Fill in the appID and appSign required by the SDK

You should go to [ZEGO Management Site](https://console-express.zego.im/acount/register) apply for appID and appSign , then fill it in `ZegoExpressExample/common/src/main/java/im/zego/common/GetAppIDConfig.java`.
