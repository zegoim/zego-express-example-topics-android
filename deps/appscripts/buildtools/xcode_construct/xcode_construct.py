#! /usr/bin/env python3 -u
# encoding: utf-8

import os
from zegopy.common import command
from zegopy.common import log
import json
import shutil


this_script_path = os.path.split(os.path.realpath(__file__))[0]


def _remove_file_or_dir(item_list):
    for item in item_list:
        log.i('try to remove: %s' % item)
        if os.path.isfile(item):
            log.i('going to remove file: %s' % item)
            os.remove(item)
        elif os.path.isdir(item):
            log.i('going to remove dir: %s' % item)
            shutil.rmtree(item, ignore_errors=True)


def xcode_construct(config, base_path):
    """使用 xcode 构建 iOS/macOS App
    config: 构建配置
    base_path: 配置内容中的基路径，配置文件中的路径字段都相对于此路径
    return: 构建结果信息
    """

    if config['type'] == 'xcode-ios-ipa':        
        BIN_SUFFIX = 'ipa'
        TARGET_DIR = 'ios'
        WORKSPACE = False
    elif config['type'] == 'xcode-macos-app':
        BIN_SUFFIX = 'app'
        TARGET_DIR = 'macos'
        WORKSPACE = False
    elif config['type'] == 'xcode-ios-workspace-ipa':
        BIN_SUFFIX = 'ipa'
        TARGET_DIR = 'ios'
        WORKSPACE = True
    elif config['type'] == 'xcode-macos-workspace-app':
        BIN_SUFFIX = 'app'
        TARGET_DIR = 'macos'
        WORKSPACE = True
    else:
        log.e('UNKNOWN TYPE: {}'.format(config['type']))
        BIN_SUFFIX = ''
        TARGET_DIR = ''
        WORKSPACE = False

    PRODUCT_PATH = os.path.join(base_path, config['product_path'])
    PRODUCT_BIN_PATH = os.path.join(PRODUCT_PATH, 'bin', TARGET_DIR)
    PRODUCT_SYMBOL_PATH = os.path.join(PRODUCT_PATH, 'symbol', TARGET_DIR)
    
    PRODUCT_ARCHIVE_PATHNAME = os.path.join(PRODUCT_PATH, config['xc_scheme']+'.xcarchive')
    PRODUCT_BIN_PATHNAME = os.path.join(PRODUCT_BIN_PATH, config['xc_scheme'] + '.' + BIN_SUFFIX)
    PRODUCT_SYMBOL_PATHNAME = os.path.join(PRODUCT_SYMBOL_PATH, config['xc_scheme'] + '.app.dSYM')
    PRODUCT_BIT_CODE_SYMBOL_MAPS_PATHNAME = os.path.join(PRODUCT_SYMBOL_PATH, 'BCSymbolMaps')
    
    _remove_file_or_dir([PRODUCT_ARCHIVE_PATHNAME, PRODUCT_BIN_PATHNAME, PRODUCT_SYMBOL_PATHNAME,
                         PRODUCT_BIT_CODE_SYMBOL_MAPS_PATHNAME])

    if WORKSPACE:
        project_cmd = '-workspace'
    else:
        project_cmd = '-project'

    # build archive
    archive_cmd = \
        'xcodebuild archive -allowProvisioningUpdates ' \
        '{0} {project_pathname} -scheme {scheme} -archivePath {archive_pathname}'.format(
        project_cmd,
        project_pathname=os.path.join(base_path, config['xc_project_path'], config['xc_project']),
        scheme=config['xc_scheme'],
        archive_pathname=PRODUCT_ARCHIVE_PATHNAME
    )

    state, text = command.execute(archive_cmd)
    log.d(text)
    if state != 0:
        raise Exception(state)

    # export ipa
    export_archive_cmd = \
        'xcodebuild -exportArchive -allowProvisioningUpdates ' \
        '-archivePath {archive_pathname} -exportPath {export_path} -exportOptionsPlist {export_options_plist}'.format(
        archive_pathname=PRODUCT_ARCHIVE_PATHNAME,
        export_path=PRODUCT_BIN_PATH,
        export_options_plist=os.path.join(base_path, config['xc_export_options_plist'])
    )

    state, text = command.execute(export_archive_cmd)
    log.d(text)
    if state != 0:
        raise Exception(state)

    # collect debug symbols
    shutil.move(os.path.join(PRODUCT_ARCHIVE_PATHNAME, 'dSYMs', config['xc_scheme'] + '.app.dSYM'), PRODUCT_SYMBOL_PATHNAME)

    # bit code
    symbol_map_path = os.path.join(PRODUCT_ARCHIVE_PATHNAME, 'BCSymbolMaps')
    if (os.path.exists(symbol_map_path)):
        log.i('copy bit code symbol maps')
        shutil.move(os.path.join(PRODUCT_ARCHIVE_PATHNAME, 'BCSymbolMaps'), PRODUCT_SYMBOL_PATH)
    
    return {
        'xc_ipa_pathname': PRODUCT_BIN_PATHNAME,
        'xc_symbol_path:': PRODUCT_SYMBOL_PATH
    }


def _validate_xcode_construct_config(config):
    schema_pathname = os.path.join(this_script_path, 'config-schema-xcode-construct.json')
    with open(schema_pathname) as schema_file:
        schema = json.loads(schema_file.read())
        from jsonschema.validators import Draft4Validator
        validator = Draft4Validator(schema)
    
    validator.validate(config)


def xcode_construct_with_config_file(config_pathname, project_base_path):
    with open(config_pathname) as config_file:
        cfg = json.loads(config_file.read())
        _validate_xcode_construct_config(cfg)
        result = xcode_construct(cfg, project_base_path)

        if len(cfg['output_product_info_json']) > 0:
            output_product_info_json = os.path.join(project_base_path, cfg['output_product_info_json'])
            with open(output_product_info_json, 'w') as result_file:
                json.dump(result, result_file)
    pass


if __name__ == '__main__':

    default_config_pathname = os.path.join(this_script_path, 'construct-config-LiveRoomPlayground-iOS.json')

    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('-config_pathname', action='store', default=default_config_pathname, help='配置文件路径（默认相对于脚本所在路径）')
    parser.add_argument('-project_base_path', action='store', default=this_script_path, help='配置内容基路径')
    args = parser.parse_args()

    log.i('GOING TO BUILD')
    log.i('CONFIG: %s' % args.config_pathname)
    log.i('PROJECT BASE PATH: %s' % args.project_base_path)

    xcode_construct_with_config_file(args.config_pathname, os.path.realpath(args.project_base_path))

    log.i('DONE.')
