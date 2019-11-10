#! /usr/bin/env python
# coding: utf-8

import os
import sys
from . import zego_helper
import shutil

# usage:
# mount share server folde (share_folder) to mount_path
# create a folder (mount_folder) optional
# copy file list (copy_list) to mount_path/mount_folder
def upload_apple(share_folder, mount_path, mount_folder, copy_list):
    print ("<< going to upload_apple")

    if os.path.exists(mount_path):
        if os.path.ismount(mount_path):
            zego_helper.getstatusoutput('umount -f {0}'.format(mount_path))
    else:
        os.makedirs(mount_path)

    print ("<< mount share")
    dest_share_path = '//share:share%40zego@192.168.1.3/share/{0}'.format(share_folder)
    print("dest share path:"+dest_share_path+", mount path:"+mount_path)
    ok, result = zego_helper.getstatusoutput('mount -t smbfs {0} {1}'.format(dest_share_path, mount_path))
    if ok != 0:
        print(result)
        raise  Exception(ok, 'mount failed')

    dest_path = mount_path
    if len(mount_folder) != 0:
        dest_path = os.path.realpath(os.path.join(mount_path, mount_folder))
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)

    print ("<< copy file")
    for file in copy_list:
        print ("<< copy {0}".format(file))
        if os.path.isdir(file):
            folder_name = os.path.split(file)[-1]
            dest_folder_path = os.path.join(dest_path, folder_name)
            if not os.path.exists(dest_folder_path):
                shutil.copytree(file, dest_folder_path, True)
            else:
                print ("{0} already exist".format(dest_folder_path))
        else:
            shutil.copy(file, dest_path)

    zego_helper.getstatusoutput('umount -f {0}'.format(mount_path))

    demo_dir_on_share = share_folder + '/' + mount_folder
    print ("<< upload to share finished, demo_dir_on_share: {0}".format(demo_dir_on_share))
    return demo_dir_on_share

def upload_linux(share_folder, mount_path, mount_folder, copy_list):
    print ("<< going to upload_apple")

    if os.path.exists(mount_path):
        if os.path.ismount(mount_path):
            zego_helper.getstatusoutput('umount -f {0}'.format(mount_path))
    else:
        os.makedirs(mount_path)

    print ("<< mount share")
    dest_share_path = 'username=share,password=share@zego //192.168.1.3/share/{0}'.format(share_folder)
    ok, result = zego_helper.getstatusoutput('mount -o {0} {1}'.format(dest_share_path, mount_path))
    if ok != 0:
        print(result)
        raise  Exception(ok, 'mount failed')

    dest_path = mount_path
    if len(mount_folder) != 0:
        dest_path = os.path.realpath(os.path.join(mount_path, mount_folder))
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)

    print ("<< copy file")
    for file in copy_list:
        print ("<< copy {0}".format(file))
        if os.path.isdir(file):
            folder_name = os.path.split(file)[-1]
            dest_folder_path = os.path.join(dest_path, folder_name)
            if not os.path.exists(dest_folder_path):
                shutil.copytree(file, dest_folder_path, True)
            else:
                print ("{0} already exist".format(dest_folder_path))
        else:
            shutil.copy(file, dest_path)

    zego_helper.getstatusoutput('umount -f {0}'.format(mount_path))

    demo_dir_on_share = share_folder + '/' + mount_folder
    print ("<< upload to share finished, demo_dir_on_share: {0}".format(demo_dir_on_share))
    return demo_dir_on_share

def upload_win32(share_folder, mount_folder, copy_list):
    print ("<< goint to upload_win32")
    print ("share_folder:{0}".format(share_folder))
    print ("mount_folder:{0}".format(mount_folder))

    dest_share_path = r'\\ZegoInside\share\{0}'.format(share_folder)
    ok, result = zego_helper.getstatusoutput(r'net use {0} share@zego /user:share'.format(dest_share_path))
    if ok != 0:
        print ("Error on Mount:{0}, {1}".format(ok, result))
        raise  Exception(ok, 'mount failed')

    dest_path = dest_share_path
    if len(mount_folder) != 0:
        dest_path = os.path.realpath(os.path.join(dest_path, mount_folder))
        print ("dest_path:{0}".format(dest_path))
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)

    print  ("<< copy file")

    for file in copy_list:
        print ("<< copy {0}".format(file))
        if os.path.isdir(file):
            folder_name = os.path.split(file)[-1]
            dest_folder_path = os.path.join(dest_path, folder_name)
            if not os.path.exists(dest_folder_path):
                shutil.copytree(file, dest_folder_path, True)
            else:
                print ("{0} already exist".format(dest_folder_path))
        else:
            shutil.copy(file, dest_path)

    code, result = zego_helper.getstatusoutput(r'net use {0} /delete /y'.format(dest_share_path))
    print ("umount result:{0}, {1}".format(code, result))

    demo_dir_on_share = share_folder + '/' + mount_folder
    print ("<< upload to share finished, demo_dir_on_share: {0}".format(demo_dir_on_share))
    return demo_dir_on_share

def upload(share_folder, mount_folder, copy_list):
    if sys.platform == 'win32':
        upload_win32(share_folder, mount_folder, copy_list)
    elif sys.platform == "darwin":
        mount_path = os.path.expanduser("~/smb_temp")
        print("mount path:"+mount_path)
        upload_apple(share_folder, mount_path, mount_folder, copy_list)
    else:
        mount_path = os.path.expanduser("~/smb_temp")
        upload_linux(share_folder, mount_path, mount_folder, copy_list)
        
    
if __name__ == '__main__':

    print ("<< zego upload")
    if sys.platform == 'win32':
        upload_win32(sys.argv[1], sys.argv[2], sys.argv[3])
    else:
        upload_apple(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])