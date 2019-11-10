#! /usr/bin/env python3
# coding: utf-8

# import sys
# import os
# sys.path.append(os.path.dirname(os.path.realpath(__file__)))

import os
from . import zego_helper
from .zego_helper import copy_dir
from zegopy.common import log
import shutil
from . import zego_copy_file
from . import unzip_file
import json

root_path = zego_helper.get_root_path()
script_path = os.path.dirname(os.path.realpath(__file__))

def ensure_dir_exist(path):
    if not os.path.exists(path):
        os.makedirs(path)

def copy_liveroom(lib_path):
    """
    从 smb 服务器上获取 ZegoExpress SDK
    :param lib_path: SDK 文件相对于 share 的路径，例如：/zego_sdk/zegoexpressengine_master/191031_165747_master-0-gae06919/Android/ZegoExpressSDK-Android-Release_bin.zip
    """
    _copy_libs_from_smb(lib_path, "zego_sdk_from_share_tmp")

def _copy_libs_from_smb(lib_path, local_tmp_folder):
    """
    从 smb 服务器上获取 SDK
    :param lib_path: SDK 文件相对于 share 的路径
    """
    log.d("<< going to copy_libs_from_smb: {0}".format(lib_path))

    dst_zip_folder = os.path.join(root_path, local_tmp_folder)
    print("root_path2= {0}".format(root_path))
    if os.path.exists(dst_zip_folder):
        shutil.rmtree(dst_zip_folder, ignore_errors=True)

    ensure_dir_exist(dst_zip_folder)

    # 从 share 上将 SDK copy 到本地
    src_sdk_path = os.path.join(lib_path)
    zego_copy_file.copy_file_from_smb(src_sdk_path, dst_zip_folder)

    # 获取压缩文件名
    sdk_file_name = lib_path.strip().split('/')[-1]
    log.d('dst_zip_folder: {0}'.format(dst_zip_folder))
    log.d('sdk_file_name: {0}'.format(sdk_file_name))

    # 解压缩
    unzip_file.unzip_file(dst_zip_folder, sdk_file_name)
    os.remove(os.path.join(dst_zip_folder,sdk_file_name))

def _validate_sdk_pkg_info_config(config):
    schema_pathname = os.path.join(script_path, 'config-schema-sdk-pkg-info.json')
    with open(schema_pathname) as schema_file:
        schema = json.loads(schema_file.read())
        from jsonschema.validators import Draft4Validator
        validator = Draft4Validator(schema)
    
    validator.validate(config)    

# 根据sdk_pkg_info.json 文件规则从share目录拷贝到工程中
# 优先使用sdk_path_on_share环境变量的目录值
def cp_sdk_from_share_and_unzip_to_prj(sdk_pkg_info_path, prj_root_dir):

    global root_path
    root_path = prj_root_dir
    log.i("root dir: %s" % root_path)

    if not os.path.exists(sdk_pkg_info_path):
        log.e("sdk_pkg_info.json not exist at {0}".format(sdk_pkg_info_path))

    with open(sdk_pkg_info_path, 'r', encoding='UTF-8') as sdk_pkg_info:
        info_dict = json.load(sdk_pkg_info)
        #_validate_sdk_pkg_info_config(info_dict)        
        #print("sdk_pkg_info.json content is: {0}".format(info_dict))
        
    zego_sdk_path_env=""
    env_dist=os.environ
    if "sdk_path_on_share" in env_dist:    
        zego_sdk_path_env = env_dist['sdk_path_on_share']
    
    log.d("zego_sdk_path_env = {0}".format(zego_sdk_path_env))
    
    sdk_path_on_share = info_dict['sdk_path_on_share']
    if zego_sdk_path_env != "":
        sdk_path_on_share = zego_sdk_path_env
    log.d('<< sdk_path_on_share: {0}'.format(sdk_path_on_share))

    copy_liveroom(sdk_path_on_share)
    copy_file_dirs = info_dict['copy_file_dirs']
    for cp_info in copy_file_dirs:
        src_path = os.path.realpath(os.path.join(root_path, "zego_sdk_from_share_tmp", cp_info["src"]))
        dst_path = os.path.realpath(os.path.join(root_path, cp_info["dst"]))
        log.d ('<< src_path: {0}'.format(src_path))
        log.d ('<< dst_path: {0}'.format(dst_path))
        copy_dir(src_path, dst_path)

#if __name__ == '__main__':
#    cp_sdk_from_share_and_unzip_to_prj(os.path.join(script_path, "./../sdk_pkg_info.json"))

