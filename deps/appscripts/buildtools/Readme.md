
##  专题demo工程编译脚本

```
├── topic_prj
│     │
|     ├── helper-scripts
|     |         └──────────── build.py、compile_windows.py、compile_windows_ended.py、compile_mac.py、compile_mac_ended.py
|     |
│     └── sdk_pkg_info.json、product_pkg_info.json、product_archive_info.json、product_upload_config.json

```

## `build.py`构建脚本，划分为以下几个步骤执行，步骤之间相对独立：

运行脚本前，下载git@gl.zego.im:realuei/pyscripts.git，并执行setup.py安装依赖库。

### 编译前：
    1.  根据配置文件的sdk位置，执行拷贝sdk到指定工程目录，根据sdk_pkg_info.json文件规则执行。
    2.  若存在环境变量sdk_on_share,则使用环境变量的值，该值是Jenkins上动态配置的值，优先使用。
    3.  该步骤可为全平台复用，调用copy_sdk_to_project.py来实现。

### 编译过程：
    1.  编译过程根据平台调用不同的编译脚本实现，根据sdk_pkg_info.json配置的平台信息。
    2.  各平台编译脚本根据环境变量读取Jenkins上配置的，实现参数化编译。
    3.  可能存在compile_windows.py、compile_osx.py、compile_ios.py、compile_android.py等文件在helper-scripts目录中提供给build.py调用。
    统一实现compile_prj方法。

### 编译后：
    1.  统一打包编译产物根据product_pkg_info.json文件规则执行。
    2.  统一上传到share目录，根据product_archive_info.json文件规则执行。
    3.  编译后在share目录上生成可用于发布的配置文件。


## `sdk_pkg_info.json` zego sdk依赖规则配置文件

主要配置指明所依赖的sdk在share机器上的绝对路径。
平台信息，windows、mac、ios、android。
sdk在工程中的目录位置，包括头文件、库文件（动态库，静态库，framework）等。

字段含义和用途：
- `project`：工程名。
- `platform`：取值为windows、osx、ios、android，在build.py中构建编译和执行编译后脚本时调用不同脚本来实现。
- `sdk_absolute_path_on_share`：zegosdk在share上的绝对路径。
- `copy_file_dirs`：从share目录下载zegosdk并解压然后拷贝到工程当中的配置。
- `src`：sdk解压后的所在目录为当前目录，配置拷贝源文件。
- `dst`：项目根目录为当前目录，配置拷贝至目标文件夹

- 示例文件：
```
{
  "project": "liveroom-topics-windows",
  "platform": "windows",
  "sdk_absolute_path_on_share": "//192.168.1.3/share/zego_sdk/zegoliveroom_master/zegoliveroom_181012_095652_master-0-gda30f6c_video_mediaplayer_bn388_12_win/windows/zegoliveroom_181012_095652_master-0-gda30f6c_video_mediaplayer_bn388_12_win.zip",
  "copy_file_dirs": [
    {
      "src": "include",
      "dst": "LiveRoomPlayground/libs/zegosdk/include/ZegoLiveRoom"
    },
    {
      "src": "libs/x86",
      "dst": "LiveRoomPlayground/libs/zegosdk/libs/Win32"
    },
    {
      "src": "libs/x64",
      "dst": "LiveRoomPlayground/libs/zegosdk/libs/x64"
    }
  ]
}
```

## `product_pkg_info.json` 打包编译产物规则文件

配置文件用于打包编译产物，
为发布上传做准备。
定义了要打包编译输出产物的规则.

- 各字段意义如下：

- `product_output_dir`：编译产物打包存放的文件夹目录，注意要单独新建，每次生成会清空文件夹里的内容

- `before_pkg_cmd`: 打包前执行的命令，目前支持删除文件列表和删除文件夹列表
    - `before_pkg_cmd.remove_file_list`: 打包前要删除的非必要公开的文件或者编译中间文件列表。
    - `before_pkg_cmd.remove_dir_list`:打包前要删除的非必要公开的文件夹或者编译中间文件夹列表。

- `product_list`：要打包的产物列表。

- `type`: 产物类型，可选为`source`、`symbol`、`bin`、
     其中source为要对外公开的源码目录，
     symbol为符号文件，windows下为pdb文件，
     bin为编译的可执行文件包，windows下为exe和dll，android为apk，ios为ipa文件。
    
- `src`: 产物源路径，相对项目根目录的路径。
- `dst`: 产物打包后放置的路径，相对项目根目录的路径。
- `file_base_name`: 产物打包后的名字，文件名规则为file_base_name_{data_version}.zip data_version为自动生成。
- `compress:` 是否需要压缩为zip，空字符串表示不压缩。

-  示例文件：
```
{
    "product_output_dir":"product_output",
    
    "before_pkg_cmd":{
        "remove_file_list":[
            "LiveRoomPlayground/AppSupport/AppIDHelper.h"
        ],
        "remove_dir_list":[
            "LiveRoomPlayground/build_dir",
            "LiveRoomPlayground/.vs",
            "LiveRoomPlayground/bin"
        ]
    },
    
    "product_list":[
    {
        "type": "source",
        "src": "LiveRoomPlayground",
        "file_base_name": "ZegoLiveRoomTopics",
        "compress": "zip"
    },
    {
        "type": "symbol",
        "src": "LiveRoomPlayground/compile_product_output/ZegoLiveRoomTopics.pdb",
        "file_base_name": "ZegoLiveRoomTopics",
        "compress": "zip"
    },
    {
        "type": "bin",
        "src": "LiveRoomPlayground/compile_product_output/bin",
        "file_base_name": "ZegoLiveRoomTopics",
        "compress": "zip"
    }]
}
```
## `product_archive_info.json` 编译产物归档到shared目录的配置

`upload_dst_share_dir`:要上传share的目录，相对于\\192.168.1.3\share目录。

```

{
    "upload_dst_share_dir":"zego_demo/liveroom_topics_demo/windows"
}

```

## `product_upload_config.json` 产物对外发布的配置

由于编译归档时，生成关于归档文件的描述文件`product_info.json`，该文件描述了哪些文件是源文件、哪些是符号文件、哪些是二进制文件，内容如下：
```
{
    "file_list": [
        {
            "type": "source",
            "filename": "ZegoLiveRoomTopics_source.zip"
        },
        {
            "type": "symbol",
            "filename": "ZegoLiveRoomTopics_symbol.zip"
        },
        {
            "type": "bin",
            "filename": "ZegoLiveRoomTopics_bin.zip"
        }
    ]
}
```

上传的目标平台账号信息、地址信息等由`product_upload_config.json`配置文件确定。
编译完成后，根据product_upload_config.json生成config.json
给发布任务使用。

```
{
    "定义编译产物发布上传的规则":"",
    
    "platform":"windows",
    
    "上传蒲公英的配置，上传的是二进制，android和ios使用":"",
    "pgyer":{
        "enable_upload":true,
        "从编译产物里的二进制文件里选，如果是压缩包，以解压后的目录作为当前目录。非压缩包以product_pkg_info.json src指定的目录为当前目录":"",
        "upload_list":["ZegoLiveRoomTopics.apk"]
    },
    
    "上传bugly的配置，上传的符号信息文件，android和ios使用":"",
    "bugly":{
        "enable_upload":true,
        "bundle_id":"",
        "version":"",
        "从编译产物里的符号文件里选，如果是压缩包，以解压后的目录作为当前目录。非压缩包以product_pkg_info.json src指定的目录为当前目录":"",
        "local_target":".",
        "app_id":"",
        "app_key":""
    },
    
    "上传github，上传工程源码":"",
    "github":{
        "enable_upload":true,
        "project":"new test prj",
        "comment":"更新代码",
        "repo":"https://github.com/xxx/xxx.git",
        "remote_tartget":".",
        "从编译产物里的工程源码里选，如果是压缩包，以解压后的目录作为当前目录。非压缩包以product_pkg_info.json src指定的目录为当前目录":"",
        "local_target":"."
    },
    
    "上传阿里云，上传的二进制可执行文件，windows平台和osx平台使用":"",
    "alicdn":{
        "enable_upload":true,
        "从编译产物里的二进制文件里选，如果是压缩包，以解压后的目录作为当前目录。非压缩包以product_pkg_info.json src指定的目录为当前目录":"",
        "local_target":"",
        "filename_on_server":""
    }
    
}
```




