#! /usr/bin/env python
# coding: utf-8

"""
在构建机上 mount、umount smd server
区分构建机的 sys.platform
"""

import os
import zipfile
import shutil
import sys
import tempfile

from .zego_helper import getstatusoutput
from zegopy.common import command

root_share = '//share:share%40zego@192.168.1.3/share/'
home = os.path.expanduser("~")
windows_root_path = r'\\ZegoInside\share'
linux_root_path = r'username=share,password=share@zego //192.168.1.3/share'

def mount_smb(mount_path):
	if sys.platform == "darwin":
		_mount_smb_darwin(mount_path)
	elif sys.platform == "win32":
		_mount_smb_windows(windows_root_path)
	else:
		_mount_smb_linux(mount_path)

def umount_smb(mount_path):
	if sys.platform == "darwin":
		_umount_smb_darwin(mount_path)
	elif sys.platform == "win32":
		_umount_smb_windows(windows_root_path)
	else:
		_umount_smb_linux(mount_path)

def _mount_smb_darwin(mount_path):
	""" 
	将 root_share mount 到本地的 mount_path 路径下。不再用 zegohelper.getstatusoutput()，统一用 command.execute()
	:param mount_path: mount 操作的本地绝对路径
	"""
	print ("<< going to _mount_smb_darwin, mount_path: {0}".format(mount_path))

	if os.path.exists(mount_path):
		print ("mount_path already exists, do umount")
		_umount_smb_darwin(mount_path)
	else:
		print ("mount_path does not exist, do create")
		os.makedirs(mount_path)

	err, result = command.execute("mount -t smbfs {0} {1}".format(root_share, mount_path))
	if err != 0:
		raise Exception(err, 'mount share failed')
	else:
		print ("mount share succeeded")


def _umount_smb_darwin(mount_path):
	"""
	umount 操作。不再用 zegohelper.getstatusoutput()，统一用 command.execute()
	:param mount_path: umount 操作的本地绝对路径
	"""
	print ("<< going to _umount_smb_darwin")

	if os.path.exists(mount_path):
		print ("mount_path exists, do umount")
		if os.path.ismount(mount_path):
			err, result = command.execute("umount -f {0}".format(mount_path))
			if err != 0:
				print(result)
				raise  Exception(err, 'umount share failed')
			else:
				print ("umount share succeeded")
		else:
			print ("mount_path is not mounted, ignore")
	else:
		print ("mount_path not exits, ignore")

def _mount_smb_linux(mount_path):

	print ("<< going to _mount_smb_linux, mount_path: {0}".format(mount_path))

	if os.path.exists(mount_path):
		print ("mount_path already exists, do umount")
		_umount_smb_linux(mount_path)
	else:
		print ("mount_path does not exist, do create")
		os.makedirs(mount_path)

	err, result = command.execute("mount -o {0} {1}".format(linux_root_path, mount_path))
	if err != 0:
		raise Exception(err, 'mount share failed')
	else:
		print ("mount share succeeded")

def _umount_smb_linux(mount_path):

	print ("<< going to _umount_smb_linux")

	if os.path.exists(mount_path):
		print ("mount_path exists, do umount")
		if os.path.ismount(mount_path):
			err, result = command.execute("umount -f {0}".format(mount_path))
			if err != 0:
				print(result)
				raise  Exception(err, 'umount share failed')
			else:
				print ("umount share succeeded")
		else:
			print ("mount_path is not mounted, ignore")
	else:
		print ("mount_path not exits, ignore")
        
def _mount_smb_windows(mount_path):
	""" 
	将 root_share mount 到本地的 mount_path 路径下。不再用 zegohelper.getstatusoutput()，统一用 command.execute()
	:param mount_path: mount 操作的本地绝对路径
	"""
	print ("<< going to _mount_smb_windows")

	# if os.path.exists(mount_path):
	# 	print ("mount_path already exists, going to umount")
	# 	_umount_smb_windows(mount_path)
	# else:
	# 	print ("mount_path does not exist, going to create")
	# 	os.makedirs(mount_path)

	err, result = command.execute(r"net use {0} share@zego /user:share".format(mount_path))
	if err != 0:
		print("mount share failed: {0}".format(result))
		raise Exception(err, 'mount share failed')
	else:
		print ("mount share succeeded")


def _umount_smb_windows(mount_path):
	"""
	umount 操作。不再用 zegohelper.getstatusoutput()，统一用 command.execute()
	:param mount_path: umount 操作的本地绝对路径
	"""
	print ("<< going to _umount_smb_windows")

	if os.path.exists(mount_path):
		print ("mount_path exists, do umount")
		if os.path.ismount(mount_path):
			err, result = command.execute(r'net use {0} /delete /y'.format(mount_path))
			if err != 0:
				print(result)
				raise  Exception(err, 'umount share failed')
			else:
				print ("umount share succeeded")
		else:
			print ("mount_path is not mounted, ignore")
	else:
		print ("mount_path not exits, ignore")

		
if __name__ == '__main__':
	mount_path = os.path.realpath(os.path.join(home, "smb_temp"))
	mount_smb(mount_path)
	umount_smb(mount_path)


