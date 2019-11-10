#! /usr/bin/env python
# coding: utf-8

import os
import json
from zegopy.common import command
from . import zego_helper
from . import zip_folder
from . import zego_upload

script_path = os.path.dirname(os.path.realpath(__file__))
root_folder = zego_helper.get_root_path()

# 把dir_to_upload目录中的文件全部上传到shared目录to_share_dir
def upload_dir_to_share(dir_to_upload, to_share_dir, new_sub_dir):
    list = os.listdir(dir_to_upload)
    upload_file_list = []
    for i in range(0,len(list)):
        path = os.path.join(dir_to_upload,list[i])
        upload_file_list.append(path)
        
    zego_upload.upload(to_share_dir, new_sub_dir, upload_file_list)


        
        
        
        
        
        
        
        
        
        
        