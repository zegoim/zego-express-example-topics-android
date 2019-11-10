#!/usr/bin/env python -u
# coding: utf-8

import os
from zegopy.builder import zego_helper


def get_version_info(git_repo):    
    version_info = []
    time_str = zego_helper.get_time_str()
    version_info.append(time_str)

    git_ver = zego_helper.get_git_version(git_repo)
    version_info.append(git_ver)

    k_build_num = 'BUILD_NUMBER'
    if k_build_num in os.environ:
        build_num = os.environ[k_build_num]
        print ('[*] Build Num:{0}'.format(build_num))
        version_info.append("bn{}".format(build_num))
    else:
    	build_num = 0

    return '_'.join(version_info)
