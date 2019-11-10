#! /usr/bin/env python
# coding: utf-8


def getstatusoutput(cmd):
    """Return (status, output) of executing cmd in a shell.
    @ref https://mail.python.org/pipermail/python-win32/2008-January/006606.html
    """

    import sys
    mswindows = (sys.platform == 'win32')

    import os
    if not mswindows:
        cmd = '{ ' + cmd + '; }'

    pipe = os.popen(cmd + ' 2>&1', 'r')
    text = pipe.read()
    sts = pipe.close()
    if sts is None: sts = 0
    if text[-1:] == '\n': text = text[:-1]
    return sts, text


def get_git_version(git_respo):
    import sys
    git_command = ''
    if sys.platform == "linux":
        git_command = 'git -c {0} describe --all --long --dirty'.format(git_respo)
    else:
        git_command = 'git -C {0} describe --all --long --dirty'.format(git_respo)
    ok, ver = getstatusoutput(git_command)
    if ok == 0:
        ver = ver.replace('/', '_')
        ver = ver.replace('remotes_origin_', '')

    return ok, ver


def get_date_version(git_respo):
    import time
    date = time.strftime('%y%m%d_%H%M%S')
    (ok, ver) = get_git_version(git_respo)
    if ok == 0:
        ver_str = '{0}_{1}'.format(date, ver)
        return ver_str

    return ""


def get_time_str():
    import time
    return time.strftime('%y%m%d.%H%M%S')


def copy_dir(src, dst):
    """Copy directory src to dst

    :param src: eg. '/var/include'
    :param dst: eg. '/usr/local/xx-include'
    """
    import shutil
    shutil.rmtree(dst, ignore_errors=True)
    shutil.copytree(src, dst, symlinks=True)


def get_root_path():
    import os

    script_path = os.path.split(os.path.realpath(__file__))[0]
    return os.path.realpath(os.path.join(script_path, '../../'))

def delete_dir(filePath):
    import os
    import stat
    import shutil
    if os.path.exists(filePath):
        for fileList in os.walk(filePath):
            for name in fileList[2]:
                os.chmod(os.path.join(fileList[0],name), stat.S_IWRITE)
                os.remove(os.path.join(fileList[0],name))
        shutil.rmtree(filePath)

def get_abs_share_dir(dir):
    import sys
    import os
    if(sys.platform == 'win32'):
        abs_dir = r'\\ZegoInside\share\{0}'.format(dir)
    elif(sys.platform == 'darwin'):
        #MOUNT_PATH = os.path.expanduser("~/smb_temp")
        abs_dir = os.path.join(os.path.expanduser("~/smb_temp"),dir)
    return abs_dir

def get_env_value(env_key):
    import os
    env_dist = os.environ
    if env_key in env_dist:    
        return env_dist[env_key]
    return None

    
def ensure_dir_exist(path):
    import os
    if not os.path.exists(path):
        os.makedirs(path)
        
        