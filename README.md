# Zego Express Example Topics Android (Java)

Zego Express Android (Java) Example Topics Demo

## Download SDK

The SDK `libZegoExpressEngine.so` and `ZegoExpressEngine.jar` required to run the Demo project is missing from this Repository, and needs to be downloaded and placed in the Libs folder of the Demo project

[https://storage.zego.im/downloads/ZegoExpress-Android.zip](https://storage.zego.im/downloads/ZegoExpress-Android.zip)

finally, the structure under the `src/ZegoExpressExample/main/libs` directory should be as follows:

```tree
src/ZegoExpressExample/main
                        └─libs
                            │
                            arm64-v8a
                            |   └── libZegoExpressEngine.so
                            |
                            armeabi-v7a
                            |   └── libZegoExpressEngine.so
                            |
                            x86
                            | └── libZegoExpressEngine.so
                            |
                            ZegoExpressEngine.jar
                            |
                            readme.md
```

## Fill in the appID and appSign required by the SDK

You should go to [ZEGO Management Site](https://console-express.zego.im/acount/register) apply for appID and appSign , then fill it in `src/ZegoExpressExample/common/src/main/java/im/zego/common/GetAppIDConfig.java`