#! /usr/bin/env python
# coding: utf-8

"""
解析 sdk_pkg_info.json 文件，从文件中读取各种信息到字典中
"""

import os
import json

script_path = os.path.dirname(os.path.realpath(__file__))

# 配置文件基本信息
config_file_name = "sdk_pkg_info.json"

def parse_sdk_pkg_info():
    sdk_pkg_info_path = os.path.join(
        script_path, "..", config_file_name)

    if not os.path.exists(sdk_pkg_info_path):
        print("sdk_pkg_info.json not exist at {0}".format(sdk_pkg_info_path))
        return

    with open(sdk_pkg_info_path, 'r') as sdk_pkg_info:
        info_dict = json.load(sdk_pkg_info)
        #print("sdk_pkg_info.json content is: {0}".format(info_dict))

    return info_dict

