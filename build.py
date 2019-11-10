#! /usr/bin/env python3
# coding: utf-8
# encoding: utf-8
import os
import json
import sys

script_path = os.path.dirname(os.path.realpath(__file__))

from zegopy.common import command
from zegopy.common import log

from deps.appscripts.buildtools import stage1_phase1_copy_sdk_to_project
from deps.appscripts.buildtools import stage1_phase3_pkg_product_files
from deps.appscripts.buildtools import stage1_phase4_archive_product_files

from helperscripts import compile_android


sdk_config_file_path = os.path.join(script_path,  "sdk_pkg_info.json")
product_pkg_config_file_path = os.path.join(script_path, "product_pkg_info.json")
archive_config_file_path = os.path.join(script_path,"product_archive_info.json")
root_dir = script_path
def parse_config_info(config_path):
    if not os.path.exists(config_path):
        log.e("config not exist at {0}".format(config_path))
        return

    with open(config_path, 'r', encoding='UTF-8') as config_info:
        info_dict = json.load(config_info)
        #print("sdk_pkg_info.json content is: {0}".format(info_dict))

    return info_dict

if __name__ == '__main__':

    print(sdk_config_file_path, product_pkg_config_file_path, archive_config_file_path)

    # 根据配置文件拷贝sdk到工程中
    log.d("<< begin copy sdk to prj from share dir")
    stage1_phase1_copy_sdk_to_project.cp_sdk_from_share_and_unzip_to_prj(sdk_config_file_path, root_dir)

    # 执行编译工程
    log.d("<< begin compile prj")

    compile_android.compile_prj()

    # 根据配置文件打包编译产物
    log.d("<< begin pkg archive product files")
    [product_dir, date_version]= stage1_phase3_pkg_product_files.pkg_product_files_from_json_config(product_pkg_config_file_path, root_dir)
    
    # 从product_archive_info.json解析获取到要上传的share目录地址
    upload_to_share_dir = parse_config_info(archive_config_file_path)['upload_dst_share_dir']

    print("upload dir path:"+upload_to_share_dir+", product dir:"+product_dir)
    
    # # 上传产物到指定shared目录的date_version文件夹中
    # log.d("<< begin upload archive product files to share dir")
    stage1_phase4_archive_product_files.upload_dir_to_share(product_dir, upload_to_share_dir, date_version)
    

    
        
