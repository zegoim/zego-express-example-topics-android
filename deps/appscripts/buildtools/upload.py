#! /usr/bin/env python
# coding: utf-8
import os
import json
import sys
import shutil
import glob
import stat
from . import zego_mount_smb
from . import unzip_file
from . import zego_copy_file
from . import zego_helper
from zegopy.common import log


root_path = zego_helper.get_root_path()
script_path = os.path.dirname(os.path.realpath(__file__))
official = False
MOUNT_PATH = os.path.expanduser("~/smb_temp")

def parse_config_info(config_path):
    config_abs_path = os.path.join(root_path, config_path)

    if not os.path.exists(config_abs_path):
        print("config_path not exist at {0}".format(config_abs_path))
        return

    with open(config_abs_path, 'r', encoding='UTF-8') as config_info:
        info_dict = json.load(config_info)
        #print("sdk_pkg_info.json content is: {0}".format(info_dict))

    return info_dict

def get_pgyer_config_json(of, target):
    """创建pgyer配置"""
    config_item = {}
    config_item["type"] = "pgyer"
    config_item["official"] = of
    config_item["local_demo_target"] = target
    return config_item

def get_github_config_json(git_repo, git_demo_target, local_demo_target, project, comment):
    """创建github配置"""
    config_item = {}
    config_item["type"] = "github"
    config_item["git_repo"] = git_repo
    config_item["git_demo_target"] = git_demo_target
    config_item["local_demo_target"] = local_demo_target
    config_item["project"] = project
    config_item["comment"] = comment
    config_item["official"] = official
    return config_item

def get_bugly_config_json(platform, bundle, appid, appkey, version, local_demo_target):
    """创建bugly配置"""
    config_item = {}
    config_item["type"] = "bugly"
    config_item["platform"] = platform
    config_item["version"] = version
    config_item["local_demo_target"] = local_demo_target
    config_item["bundle"] = bundle
    config_item["appid"] = appid
    config_item["appkey"] = appkey
    config_item["official"] = official    
    return config_item

def get_alicdn_config_json(local_demo_target, filename):
    """创建alicdn配置"""
    config_item = {}
    config_item["type"] = "alicdn"
    config_item["local_demo_target"] = local_demo_target
    config_item["filename_on_server"] = filename
    config_item["official"] = official    
    return config_item

# 发布上传描述文件product_upload_config.json 生成config.json,
# 把归档目录转到upload_task_dir目录
def upload_config_file_to_task_config(share_dir, upload_task_dir):
    
    env_dist = os.environ
    
    share_dir = zego_helper.get_abs_share_dir(share_dir)
    
    upload_task_dir = zego_helper.get_abs_share_dir(upload_task_dir)

    log.d("dir {0} , upload task dir {1}".format(share_dir, upload_task_dir))

    if(sys.platform == 'darwin'):
        zego_mount_smb.mount_smb(MOUNT_PATH)

    product_config_file_path = os.path.join(share_dir, "product_upload_config.json")
    if not os.path.exists(product_config_file_path):
        log.e("config_path not exist at {0}".format(product_config_file_path))
        raise  Exception(err, 'product_upload_config.json not found')

    info_dict = {}
    with open(os.path.join(share_dir, "product_upload_config.json"), 'r', encoding='UTF-8') as config_info:
        info_dict = json.load(config_info)
        print("product_upload_config.json content is: {0}".format(info_dict))
        
    enable_pgyer = info_dict["pgyer"]["enable_upload"]
    enable_bugly = info_dict["bugly"]["enable_upload"]
    enable_github = info_dict["github"]["enable_upload"]
    enable_alicdn = info_dict["alicdn"]["enable_upload"]

    if(zego_helper.get_env_value("upload_pgyer") != None):
        if(zego_helper.get_env_value("upload_pgyer") == "true"):
            enable_pgyer = True
        else:
            enable_pgyer = False

    if(zego_helper.get_env_value("upload_bugly") != None):
        if(zego_helper.get_env_value("upload_bugly") == "true"):
            enable_bugly = True
        else:
            enable_bugly = False

    if(zego_helper.get_env_value("upload_github") != None):
        if(zego_helper.get_env_value("upload_github") == "true"):
            enable_github = True
        else:
            enable_github = False

    if(zego_helper.get_env_value("upload_alicdn") != None):
        if(zego_helper.get_env_value("upload_alicdn") == "true"):
            enable_alicdn = True
        else:
            enable_alicdn = False

    log.d("<< pgyer = {0}, bugly = {1}, github = {2}, alicdn = {3}".format(enable_pgyer, enable_bugly, enable_github, enable_alicdn))

    official = False
    if(zego_helper.get_env_value("Official") != None):
        if(zego_helper.get_env_value("Official") == "true"):
            official = True
    
    log.d("<< use official {0}".format(official))
    
    config = []

    bin_is_zip = True
    src_is_zip = True
    symbol_is_zip = True
    
    have_source = False
    have_symbol = False
    have_bin = False

    bin_name = ""
    src_name = ""
    symbol_name = ""

    for archive_item in info_dict["archive_file_list"]:
        if(archive_item["type"] == "source"):
            have_source = True
            src_name = archive_item["filename"]
            if(archive_item["compress"] == ""):
                src_is_zip = False
        if(archive_item["type"] == "symbol"):
            have_symbol = True
            symbol_name = archive_item["filename"]
            if(archive_item["compress"] == ""):
                symbol_is_zip = False
        if(archive_item["type"] == "bin"):
            have_bin = True
            bin_name = archive_item["filename"]
            if(archive_item["compress"] == ""):
                bin_is_zip = False                                
    if (have_bin):
        if (bin_is_zip):
            zego_helper.ensure_dir_exist(os.path.join(upload_task_dir, "bin_dir"))
            unzip_file.unzip_file(os.path.join(upload_task_dir, "bin_dir"), os.path.join(share_dir, bin_name))
        else:
            zego_copy_file.copy_file_from_smb_absolute(os.path.join(share_dir, bin_name), upload_task_dir,True)

    if(have_source):
        if(src_is_zip):
            zego_helper.ensure_dir_exist(os.path.join(upload_task_dir, "src_dir"))
            unzip_file.unzip_file(os.path.join(upload_task_dir, "src_dir"), os.path.join(share_dir, src_name))
        else:
            zego_copy_file.copy_file_from_smb_absolute(os.path.join(share_dir, src_name), upload_task_dir,True)    

    if(have_symbol):
        if(symbol_is_zip):
            zego_helper.ensure_dir_exist(os.path.join(upload_task_dir, "symbol_dir"))
            unzip_file.unzip_file(os.path.join(upload_task_dir, "symbol_dir"), os.path.join(share_dir, symbol_name))
        else:
            zego_copy_file.copy_file_from_smb_absolute(os.path.join(share_dir, symbol_name), upload_task_dir,True)

    if (enable_pgyer):    
        for item in info_dict["pgyer"]["upload_list"]:
            if (bin_is_zip):
                if(item == "" or item =="."):
                    item = "bin_dir"
                else:
                    #item = os.path.join("bin_dir", item)  
                    item = "bin_dir/" + item                
            config.append(get_pgyer_config_json(official, item))
    
    if(enable_bugly):
        target = info_dict["bugly"]["local_target"]
        if (bin_is_zip):
            if(target == "" or target == "."):
                target = "symbol_dir"
            else:
                #target = os.path.join("symbol_dir",target)
                target = "symbol_dir/" + target
        config.append(get_bugly_config_json(
        info_dict["platform"],
        info_dict["bugly"]["bundle_id"],
        info_dict["bugly"]["app_id"],
        info_dict["bugly"]["app_key"],
        info_dict["bugly"]["version"],
        target))

    if(enable_github):
        target = info_dict["github"]["local_target"]
        if (src_is_zip):
            if(target == "" or target == "."):
                target = "src_dir"
            else:
                #target = os.path.join("src_dir", target)
                target = "src_dir/" + target
        commit_comment = info_dict["github"]["comment"]
        if "github_commit_comment" in env_dist:
            commit_comment = env_dist['github_commit_comment']
            if (commit_comment != ""):
                commit_comment = commit_comment + "_" + info_dict["github"]["comment"]
            else:
                commit_comment = info_dict["github"]["comment"]
        config.append(get_github_config_json(info_dict["github"]["repo"], 
        info_dict["github"]["remote_tartget"],
        target,
        info_dict["github"]["project"],
        commit_comment))
    
    if(enable_alicdn):
        target = info_dict["alicdn"]["local_target"]
        if (bin_is_zip):
            if(target == "" or target == "."):
                target = "bin_dir"
            else:
                #target = os.path.join("bin_dir", target)  
                target = "bin_dir/" + target  
        config.append(get_alicdn_config_json(target
        , info_dict["alicdn"]["filename_on_server"]))
    
    log.d("<< gen upload task file config.json {0}".format(config))
    with open(os.path.join(upload_task_dir, "config.json"),'w',encoding='utf-8') as json_file:
        json.dump(config, json_file, ensure_ascii=False)

#if __name__ == '__main__':
#
#    env_dist = os.environ
#    share_dir = ""
#    # upload_task_dir = ""
#    if "upload_path_on_share" in env_dist:    
#        share_dir = env_dist['upload_path_on_share']
#    # if "upload_task_path_on_share" in env_dist:
#    #     upload_task_dir = env_dist['upload_task_path_on_share']
#    if (share_dir != ""):
#        log.d("<< create task config file ...")
#        # if(upload_task_dir == ""):
#        #     upload_task_dir = os.path.join("upload_task", share_dir)
#        upload_task_dir = os.path.join("upload_task", share_dir)
#        upload_config_file_to_task_config(share_dir, upload_task_dir)
    
