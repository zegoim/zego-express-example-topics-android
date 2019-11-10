#! /usr/bin/env python
# coding: utf-8

import sys
import os
from zipfile import *
import zipfile
import shutil

def zip_file(src_file_path, dst_file_path):
    with zipfile.ZipFile(dst_file_path, 'w') as z:
        [dirname,filename]=os.path.split(src_file_path)
        z.write(src_file_path, filename)


def zip_folder_list(source_folder_list, dest_folder, zip_name):

    if not os.path.exists(dest_folder):
        os.makedirs(dest_folder)

    zip_file = os.path.realpath(os.path.join(dest_folder, zip_name))
    f = zipfile.ZipFile(zip_file, 'w', zipfile.ZIP_DEFLATED)

    for source_folder in source_folder_list:
        if not os.path.exists(source_folder):
            print (">> source folder do not exist!")
            continue

        filelist = []
        if os.path.isfile(source_folder):
            filelist.append(source_folder)
        else:
            for root, dirs, files in os.walk(source_folder):
                for name in files:
                    filelist.append(os.path.join(root, name))

        for tar in filelist:
            basename = os.path.split(source_folder)[-1]
            arcname = tar[len(source_folder):]
            filename = basename + arcname
            print (">> zip file {0}".format(filename))
            f.write(tar, filename)

    f.close()

    return True


def zip_folder(source_folder, dest_folder, zip_name):
    if not os.path.exists(source_folder):
        print (">> source folder do not exist!")
        return False

    if not os.path.exists(dest_folder):
        os.makedirs(dest_folder)

    filelist = []
    if os.path.isfile(source_folder):
        filelist.append(source_folder)
    else:
        for root, dirs, files in os.walk(source_folder):
            for name in files:
                filelist.append(os.path.join(root, name))

    zip_file = os.path.realpath(os.path.join(dest_folder, zip_name))
    f = zipfile.ZipFile(zip_file, 'w', zipfile.ZIP_DEFLATED)
    for tar in filelist:
        basename = os.path.split(source_folder)[-1]
        arcname = tar[len(source_folder):]
        filename = basename + arcname
        print (">> zip file {0}".format(filename))
        f.write(tar, arcname)

    f.close()

    return True

def zip_folder_with_link(source_folder, dest_folder, zip_name):
    if not os.path.exists(dest_folder):
        os.makedirs(dest_folder)

    zip_file = os.path.realpath(os.path.join(dest_folder, zip_name))
    f = zipfile.ZipFile(zip_file, 'w', zipfile.ZIP_DEFLATED)

    rootLen = len(os.path.dirname(source_folder))
    archiveDirectory(source_folder, f, rootLen)


def archiveDirectory(parentDirectory, zipOut, rootLen):
    contents = os.listdir(parentDirectory)

    if not contents:
        archiveRoot = parentDirectory[rootLen:].replace('\\', '/').lstrip('/')
        zipInfo = zipfile.ZipInfo(archiveRoot+'/')
        zipOut.writestr(zipInfo, '')
    for item in contents:
        fullpath = os.path.join(parentDirectory, item)
        if os.path.isdir(fullpath) and not os.path.islink(fullpath):
            archiveDirectory(fullpath, zipOut, rootLen)
        else:
            archiveRoot = fullpath[rootLen:].replace('\\', '/').lstrip('/')
            print (">> zip file {0}".format(archiveRoot))
            if os.path.islink(fullpath):
                zipInfo = zipfile.ZipInfo(archiveRoot)
                zipInfo.create_system = 3
                # long type of hex val of '0xA1ED0000L',
                # say, symlink attr magic...
                zipInfo.external_attr = 2716663808
                zipOut.writestr(zipInfo, os.readlink(fullpath))
            else:
                zipOut.write(fullpath, archiveRoot, zipfile.ZIP_DEFLATED)


if __name__ == '__main__':
    zip_folder(sys.argv[1], sys.argv[2], sys.argv[3])

