#! /usr/bin/env python
# coding: utf-8

"""
将文件从源位置，复制到目的位置
"""

import os
import io
from . import zego_mount_smb
import sys
import shutil

from .zego_helper import getstatusoutput
from zegopy.common import command
from zegopy.common import io
from zegopy.common import log

MOUNT_PATH = os.path.expanduser("~/smb_temp")
MOUNT_PREFIX_DARWIN = os.path.expanduser("~/smb_temp")   # darwin 机器的 share 挂载点前缀
MOUNT_PREFIX_WIN32 = r'\\ZegoInside\share' # win32 的 share 挂载点前缀
MOUNT_PREFIX_LINUX = os.path.expanduser("~/smb_temp")

def copy_file_from_smb(src_file_relative_path, dst_file_absolute_dir, over_write=True):
    """ 
    将 smb server 上的 src_file_relative_path 里的文件，复制到 dst_file_absolute_dir 中
    :param src_file_relative_path: 要复制的文件位于 smb server 上的相对路径，例如：'zego_sdk/zegoliveroom_master/zegoliveroom_180515_105728_master-0-g660c350e_bn203_12_video/iOS/zegoliveroom_180515_105728_master-0-g660c350e_bn203_12_video_ios.zip'
    :param dst_file_absolute_dir: 文件要复制到本地目录的绝对路径，例如：'/Users/xia/zegolivedemo/projects/apple/LiveDemo3/libs'
    """
    log.d ("<< going to copy_file_from_smb_server")

    # mount
    zego_mount_smb.mount_smb(MOUNT_PATH)

    if sys.platform == "win32":
        src_file_relative_path = os.path.realpath(os.path.join(MOUNT_PREFIX_WIN32, src_file_relative_path))
    elif sys.platform == "darwin":
        src_file_relative_path = os.path.realpath(os.path.join(MOUNT_PREFIX_DARWIN, src_file_relative_path))
    elif sys.platform == "linux":
        src_file_relative_path = os.path.realpath(os.path.join(MOUNT_PREFIX_LINUX, src_file_relative_path))
    else:
        raise Exception("unknown platform")

    # copy file
    log.d ("** copy file: {0} -> {1}".format(src_file_relative_path, dst_file_absolute_dir))
    copy_result = io.copy(src_file_relative_path, dst_file_absolute_dir, over_write)
    if copy_result == 0:
        log.d("copy file succeeded")
    else:
        raise Exception("copy file failed: {0}".format(copy_result))

    # umount
    zego_mount_smb.umount_smb(MOUNT_PATH)
    return copy_result

def copy_file_from_smb_absolute(src_file_absolute_path, dst_file_absolute_dir, over_write):
    """ 
    将 smb server 上的 src_file_absolute_path 里的文件，复制到 dst_file_absolute_dir 中
    :param src_file_absolute_path: 要复制的文件位于 smb server 上的绝对路径
    :param dst_file_absolute_dir: 文件要复制到本地目录的绝对路径，例如：'/Users/xia/zegolivedemo/projects/apple/LiveDemo3/libs'
    """
    log.d ("<< going to copy_file_from_smb_server")

    # copy file
    log.d ("** copy file: {0} -> {1}".format(src_file_absolute_path, dst_file_absolute_dir))
    copy_result = io.copy(src_file_absolute_path, dst_file_absolute_dir, over_write)
    if copy_result == 0:
        log.d("copy file succeeded")
    else:
        raise Exception("copy file failed: {0}".format(copy_result))

    return copy_result


def copy_file_from_local(src_file_absolute_path, dst_file_absolute_dir, over_write=True):
    """
    将本地 src_file_absolute_path 中的源文件，复制到 dst_file_absolute_dir 中
    :param src_file_absolute_path: 源文件位置
    :param dst_file_absolute_dir: 目的位置
    """
    log.d ("<< going to copy_file_from_local")
    log.d ("** copy file: {0} -> {1}".format(src_file_absolute_path, dst_file_absolute_dir))
    copy_result = io.copy(src_file_absolute_path, dst_file_absolute_dir, over_write)
    if copy_result == 0:
        log.d("copy file succeeded")
    else:
        raise Exception("copy file failed: {0}".format(copy_result))

    return copy_result


def copy_file_to_smb(src_absolute_path, dst_relative_dir):
    """
    将本地的文件，复制到 smb server
    :param relative_path_on_share:
    :param mount_root:
    :param local_task_dir:
    """
    if sys.platform == "darwin":
        _copy_file_to_smb_from_darwin(src_absolute_path, dst_relative_dir)
    elif sys.platform == "win32":
        _copy_file_to_smb_from_win32(src_absolute_path, dst_relative_dir)
    else:
        _copy_file_to_smb_from_linux(src_absolute_path, dst_relative_dir)

def _copy_file_to_smb_from_darwin(src_absolute_path, dst_relative_dir):
    """
    从 darwin 提交任务文件到 smb server
    """
    log.d ("<< going to _copy_file_to_smb_from_darwin, dst_relative_dir: {0}, dst_relative_dir: {1}".format(src_absolute_path, dst_relative_dir))

    zego_mount_smb.mount_smb(MOUNT_PATH)
    dst_absolute_dir = os.path.join(MOUNT_PREFIX_DARWIN, dst_relative_dir)

    # make sure dst folder is exist and empty
    if os.path.exists(dst_absolute_dir):
        shutil.rmtree(dst_absolute_dir, ignore_errors=True)
    os.makedirs(dst_absolute_dir)

    # copy files from src to dst
    try:
        io.copy(src_absolute_path, dst_absolute_dir, True)
    except Exception as e:
        log.d (e.args)
        raise e
    finally:
       	zego_mount_smb.umount_smb(MOUNT_PATH)

    log.d ("<< _copy_file_to_smb_from_darwin done")

def _copy_file_to_smb_from_linux(src_absolute_path, dst_relative_dir):
    """
    从 darwin 提交任务文件到 smb server
    """
    log.d ("<< going to _copy_file_to_smb_from_darwin, dst_relative_dir: {0}, dst_relative_dir: {1}".format(src_absolute_path, dst_relative_dir))

    zego_mount_smb.mount_smb(MOUNT_PATH)
    dst_absolute_dir = os.path.join(MOUNT_PREFIX_LINUX, dst_relative_dir)

    # make sure dst folder is exist and empty
    if os.path.exists(dst_absolute_dir):
        shutil.rmtree(dst_absolute_dir, ignore_errors=True)
    os.makedirs(dst_absolute_dir)

    # copy files from src to dst
    try:
        io.copy(src_absolute_path, dst_absolute_dir, True)
    except Exception as e:
        log.d (e.args)
        raise e
    finally:
       	zego_mount_smb.umount_smb(MOUNT_PATH)

    log.d ("<< _copy_file_to_smb_from_linux done")

def _copy_file_to_smb_from_win32(src_absolute_path, dst_relative_dir):
    """
    从 windows 提交任务文件到 smb server
    """
    log.d ("<< going to _copy_file_to_smb_from_win32, src_absolute_path: {0}, dst_relative_dir: {1}".format(src_absolute_path, dst_relative_dir))

    zego_mount_smb.mount_smb(MOUNT_PATH)
    dst_absolute_dir = os.path.realpath(os.path.join(MOUNT_PREFIX_WIN32, dst_relative_dir))

    # make sure dst folder is exist and empty
    if os.path.exists(dst_absolute_dir):
        shutil.rmtree(dst_absolute_dir, ignore_errors=True)
    os.makedirs(dst_absolute_dir)

    # copy files from src to dst
    try:
        io.copy(src_absolute_path, dst_absolute_dir, True)
    except Exception as e:
        log.d (e.args)
        raise e
    finally:
        zego_mount_smb.umount_smb(mount_point)

    log.d ("<< _copy_file_to_smb_from_win32 done")

if __name__ == '__main__':
    pass

    copy_file_to_smb("/Users/xia/zegolivedemo/projects/apple/VideoTalk/sdk_pkg_info.json", "zego_demo/sdk_pkg_info/videotalk_test")



