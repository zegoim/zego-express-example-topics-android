# encoding: utf-8
#! /usr/bin/env python3
# coding: utf-8
import os
import json
from zegopy.common import command
import shutil,os
script_path = os.path.dirname(os.path.realpath(__file__))


# 编译android工程
def compile_prj():
    os.chdir(script_path + "/../ZegoExpressExample/");
    build_cmd = "sh gradlew clean assemblerelease"
    print("build cmd : {0}".format(build_cmd))
    err, result = command.execute(build_cmd)
    if err != 0:
        raise Exception(err, 'build failed')
    else:
        shutil.copy(script_path + "/../ZegoExpressExample/main/build/outputs/apk/release/main-release.apk", script_path + "/../")
        print ("build succeeded")
        
        
