#! /usr/bin/env python3
# coding: utf-8

import os

from buildtools import upload
from zegopy.common import log

if __name__ == '__main__':

    env_dist = os.environ
    share_dir = ""
    # upload_task_dir = ""
    if "upload_path_on_share" in env_dist:    
        share_dir = env_dist['upload_path_on_share']
    # if "upload_task_path_on_share" in env_dist:
    #     upload_task_dir = env_dist['upload_task_path_on_share']
    if (share_dir != ""):
        log.d("<< create task config file ...")
        # if(upload_task_dir == ""):
        #     upload_task_dir = os.path.join("upload_task", share_dir)
        upload_task_dir = os.path.join("upload_task", share_dir)
        upload.upload_config_file_to_task_config(share_dir, upload_task_dir)
    
    #upload.upload_config_file_to_task_config("zego_demo/liveroom_topics_demo/android/181023_115448_master-0-g7539ed2", "upload_task/zego_demo/liveroom_topics_demo/android/181023_115448_master-0-g7539ed2")