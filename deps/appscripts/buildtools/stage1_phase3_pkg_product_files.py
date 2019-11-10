#! /usr/bin/env python
# coding: utf-8

import os
import json
import shutil
from zegopy.common import command
from . import zego_helper
from . import zip_folder
from . import zego_copy_file

script_path = os.path.dirname(os.path.realpath(__file__))
root_path = zego_helper.get_root_path()

def remove_file_list(file_list):
    for f in file_list:
        if(os.path.isfile(f)):
            print("remove {0}".format(f))
            os.remove(f)

def remove_dir_list(dir_list):
    for dir in dir_list:
        if os.path.isdir(dir):
            shutil.rmtree(dir)

def save_upload_config_file(product_output_dir, product_info):
    if not os.path.exists(os.path.join(root_path,"product_upload_config.json")):
        print("product_upload_config.json not exist at {0}".format(root_path))

    info_dict = {}

    with open(os.path.join(root_path,"product_upload_config.json"), 'r', encoding='UTF-8') as config_info:
        info_dict = json.load(config_info)
        date_version = zego_helper.get_date_version(root_path)
        date_version = date_version.replace('-dirty','')
        info_dict["github"]["comment"] = info_dict["github"]["comment"] + "_" + date_version
        info_dict["archive_file_list"] = []
        info_dict["archive_file_list"] = product_info
        #_validate_sdk_pkg_info_config(info_dict)
        #print("sdk_pkg_info.json content is: {0}".format(info_dict))
    with open(os.path.join(product_output_dir, "product_upload_config.json"),'w',encoding='utf-8') as json_file:
        json.dump(info_dict, json_file, ensure_ascii=False)


# 打包编译产物，根据product_pkg_info.json文件描述的规则进行打包到指定目录
def pkg_product_files_from_json_config(config_file_path, prj_root_dir):

    global root_path
    root_path = prj_root_dir

    if not os.path.exists(config_file_path):
        print("product_pkg_info not exist at {0}".format(config_file_path))
    with open(config_file_path, 'r',encoding='UTF-8') as config_info:
        config_dict = json.load(config_info)
        #print("product_pkg_info.json content is: {0}".format(config_dict))

    before_pkg_cmds = config_dict["before_pkg_cmd"]
    need_remove_files = before_pkg_cmds["remove_file_list"]
    need_remove_dirs = before_pkg_cmds["remove_dir_list"]
    need_add_files_and_file_to_dirs = before_pkg_cmds["add_file_to_dir_list"]

    need_remove_files_list = []
    for file in need_remove_files:
        file = os.path.join(root_path, file)
        need_remove_files_list.append(file)

    need_remove_dirs_list = []
    for dir in need_remove_dirs:
        dir = os.path.join(root_path, dir)
        need_remove_dirs_list.append(dir)

    remove_file_list(need_remove_files_list)
    remove_dir_list(need_remove_dirs_list)

    # 添加指定文件到指定目录
    for need_add_file_and_file_to_dir in need_add_files_and_file_to_dirs:
        target_file_path = os.path.join(root_path, need_add_file_and_file_to_dir["file_path"])
        target_dir_path = os.path.join(root_path, need_add_file_and_file_to_dir["dir_path"])
        # zego_copy_file.copy_file_from_local(target_file_path, target_dir_path, True)
        shutil.copyfile(target_file_path, os.path.join(target_dir_path, need_add_file_and_file_to_dir["file_path"]))

    product_output_dir = os.path.join(root_path, config_dict['product_output_dir'])

    if os.path.exists(product_output_dir):
        shutil.rmtree(product_output_dir, ignore_errors=True)

    if not os.path.exists(product_output_dir):
        os.mkdir(product_output_dir)

    product_info = []
    for config_item in config_dict['product_list']:
        #print("item : {0}".format(config_item))
        product_type = config_item["type"]
        product_src_path = config_item["src"]
        product_dst_file_base_name = config_item["file_base_name"]
        compress_product_type = config_item["compress"]
        #print("{0} {1} {2} {3} {4}".format(product_type, product_src_path, product_dst_path, product_dst_file_base_name, compress_product_type))

        date_version = zego_helper.get_date_version(root_path)
        date_version = date_version.replace('-dirty','')

        src_abs_path = os.path.join(root_path, product_src_path)
        dst_abs_path = product_output_dir
        product_file_name = ""
        if(compress_product_type == "zip"):
            zip_name = "{0}_{1}.zip".format(product_dst_file_base_name, product_type)
            product_file_name = zip_name
            if os.path.isdir(src_abs_path):
                zip_folder.zip_folder_with_link(src_abs_path, dst_abs_path, zip_name)
            else:
                file_name = "{0}_{1}.zip".format(product_dst_file_base_name, product_type)
                dst_abs_path = os.path.join(dst_abs_path, file_name)
                zip_folder.zip_file(src_abs_path, dst_abs_path)
        else:
            if os.path.isdir(src_abs_path):
                product_file_name = os.path.basename(src_abs_path)
                zego_copy_file.copy_file_from_local(src_abs_path, dst_abs_path)
            else:
                (shotname,extension) = os.path.splitext(src_abs_path)
                file_name = "{0}_{1}{2}".format(product_dst_file_base_name, product_type , extension)
                product_file_name = file_name
                dst_abs_path = os.path.join(dst_abs_path, file_name)
                shutil.copy(src_abs_path, dst_abs_path)

        product_info_item = {}
        product_info_item["type"] = product_type
        product_info_item["filename"] = product_file_name
        product_info_item["compress"] = compress_product_type
        product_info.append(product_info_item)
    # with open(os.path.join(product_output_dir, "product_info.json"),'w',encoding='utf-8') as json_file:
    #     json.dump(product_info, json_file, ensure_ascii=False)

    save_upload_config_file(product_output_dir, product_info)

    return [product_output_dir, date_version]
    
if __name__ == '__main__':
    #pkg_product_files_from_json_config(os.path.join(root_path, "product_pkg_info.json"))

    import os
    import json
    import shutil
    import io


    def copy_file_from_local(src_file_absolute_path, dst_file_absolute_dir, over_write=True):
        """
        将本地 src_file_absolute_path 中的源文件，复制到 dst_file_absolute_dir 中
        :param src_file_absolute_path: 源文件位置
        :param dst_file_absolute_dir: 目的位置
        """
        copy_result = io.copy(src_file_absolute_path, dst_file_absolute_dir, over_write)
        if copy_result == 0:
            pass
        else:
            raise Exception("copy file failed: {0}".format(copy_result))

        return copy_result

    config_file_path = "/Users/hsx2117/ZegoProject/liveroom-topics-android/product_pkg_info.json"
    root_path = "/Users/hsx2117/ZegoProject/liveroom-topics-android"
    with open(config_file_path, 'r',encoding='UTF-8') as config_info:
        config_dict = json.load(config_info)

    before_pkg_cmds = config_dict["before_pkg_cmd"]
    need_add_files_and_file_to_dirs = before_pkg_cmds["add_file_to_dir_list"]


    # 添加指定文件到指定目录
    for need_add_file_and_file_to_dir in need_add_files_and_file_to_dirs:
        target_file_path = os.path.join(root_path, need_add_file_and_file_to_dir["file_path"])
        target_dir_path = os.path.join(root_path, need_add_file_and_file_to_dir["dir_path"])
        # copy_file_from_local(target_file_path, target_dir_path, True)
        shutil.copyfile(target_file_path, os.path.join(target_dir_path, need_add_file_and_file_to_dir["file_path"]))
