#! /usr/bin/env python
# coding: utf-8

"""
解压缩 .zip 文件
"""

import os
from . import zego_helper
import shutil
import sys
import zipfile
from zegopy.common import command
from zegopy.common import log


def unzip_file(dst_zip_folder, zip_file_name):
    if sys.platform == "darwin":
        _unzip_file_darwin(dst_zip_folder, zip_file_name)
    elif sys.platform == "win32":
        _unzip_file_win32(dst_zip_folder, zip_file_name)
    else:
        _unzip_file_linux(dst_zip_folder, zip_file_name)


def _unzip_file_linux(dst_zip_folder, zip_file_name):
    log.d("<< going to unzip file on linux:\n\t{0} -> {1}".format(
        os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder))
        
    unzip_cmd = "unzip -q {0} -d {1}".format(os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder)
    err, result = command.execute(unzip_cmd)
    if err == 0:
        log.d("unzip file on darwin succeeded")
    else:
        log.e("unzip file failed: {0}".format(result))
    return

    log.d("unzip file on linux succeeded")

def _unzip_file_darwin(dst_zip_folder, zip_file_name):
    log.d("<< going to unzip file on darwin:\n\t{0} -> {1}".format(
        os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder))

    # osx 平台的 framework 中含有 alias 文件，不能用 zipfile 处理
    # if "osx" in dst_zip_folder:
        
    unzip_cmd = "unzip -q {0} -d {1}".format(os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder)
    err, result = command.execute(unzip_cmd)
    if err == 0:
        log.d("unzip file on darwin succeeded")
    else:
        log.e("unzip file failed: {0}".format(result))
    return

    # with zipfile.ZipFile(os.path.join(dst_zip_folder, zip_file_name)) as zf:
    #     zf.extractall(dst_zip_folder)
    
    log.d("unzip file on darwin succeeded")

def _unzip_file_win32(dst_zip_folder, zip_file_name):
    """
    解压缩 zip 文件
    :param dst_zip_folder: 目的解压缩目录
    :param zip_file_name: 源压缩文件名
    """ 
    # print(">>> going to unzip file on win32: {0} -> {1}".format(
    #     os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder))

    # dst_folder = dst_zip_folder
    # unzip_cmd = "7z x {0} -y -o {1}".format(
    #     os.path.join(dst_zip_folder, zip_file_name), dst_folder)
    # err, result = command.execute(unzip_cmd)
    # if err == 0:
    #     print("unzip file succeeded")
    # else:
    #     print(result)
    #     raise Exception(err)

    log.d("<< going to unzip file on win32:\n\t{0} -> {1}".format(
        os.path.join(dst_zip_folder, zip_file_name), dst_zip_folder))
    with zipfile.ZipFile(os.path.join(dst_zip_folder, zip_file_name)) as zf:
        zf.extractall(dst_zip_folder)
    log.d("unzip file on win32 succeeded")


#if __name__ == '__main__':

    #unzip_file("/Users/xia/zegolivedemo/projects/osx/LiveDemo5OSX", "/Users/xia/zegolivedemo/projects/osx/LiveDemo5OSX/zegoliveroom-mac-zegoliveroom_180420_223246_master-0-g73f8256_bn94_12_video-Release.zip")
