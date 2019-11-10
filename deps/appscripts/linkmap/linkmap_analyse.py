#! /usr/bin/env python3

from zegopy.common import command


def debug_log(l):
  print("[*] {}".format(l))


def demangle(mangled_sym):
  state, text = command.execute('/usr/bin/c++filt %s' % mangled_sym, False)
  if state != 0:
    return mangled_sym
  
  demangled = text[:-1]  # remove tailing '\n'
  if demangled.find('\n') != -1:
    # ignore string literal
    demangled = mangled_sym
  
  return demangled


class Symbol(object):
  def __init__(self):
    self.name = ''
    self.address = 0
    self.size = 0


class ObjectFile(object):
  def __init__(self):
    self.file_index = 0
    self.name = ''
    self.symbol_list = list()
    self.size = 0
    

class Archive(object):
  def __init__(self):
    self.name = ''
    self.index_object = dict()
    self.size = 0


class LinkMap(object):
  state_reading_object_files = 'reading-object-files'
  state_reading_symbols = 'reading-symbols'
  state_waiting_tag = 'waiting-tag'
  
  def __init__(self):
    self.archive_map = dict()
    self.current_state = self.state_waiting_tag

  def add_object_file(self, l):
    index = l.find("]")
    if index != -1:
      object_file = ObjectFile()
      object_file.file_index = int(l[1: index])
      object_file.name = l[index + 2:-1]

      if object_file.file_index == 0:
        # skip bitcode
        return
    
      archive_name = "SDK-SRC"
      if l.find(')') != -1:
        begin = l.rfind('/')
        end = l.rfind('(')
        archive_name = l[begin + 1:end]
    
      if archive_name in self.archive_map:
        archive = self.archive_map[archive_name]
      else:
        debug_log("new archive: {}".format(archive_name))
        archive = Archive()
        archive.index_object = dict()
        archive.name = archive_name
        archive.size = 0
        self.archive_map[archive_name] = archive
    
      archive.index_object[object_file.file_index] = object_file

  def add_symbol(self, l):
    symbols_array = l.split("\t")
    if len(symbols_array) == 3:
      file_key_and_name = symbols_array[2]
      size = int(symbols_array[1], 16)
      index = file_key_and_name.find("]")
      if index != -1:
        key = file_key_and_name[1:index]
        key = int(key)
      
        if key == 0:
          return
      
        for _, archive in self.archive_map.items():
          found = False
          for index, object_file in archive.index_object.items():
            if index == key:
              sym = Symbol()
              sym.size = size
              sym.name = file_key_and_name
              object_file.symbol_list.append(sym)
              object_file.size += size
              archive.size += size
              found = True
              break
          if found:
            break
  
  def load(self, file_pathname):
    # with open() as f:
    with open(file_pathname) as f:
      while True:
        line = ""
        try:
          line = f.readline()
        except:
          debug_log(line)
          continue
          
        if not line:
          break
      
        if self.current_state == self.state_waiting_tag:
          if not line.startswith('#'):
            # skip unwanted line
            continue
          else:
            # enter search for content
            if line.startswith('# Object files:'):
              self.current_state = self.state_reading_object_files
            elif line.startswith('# Address	Size    	File  Name'):
              self.current_state = self.state_reading_symbols
        else:
          if line.startswith('#'):
            self.current_state = self.state_waiting_tag
          else:
            if self.current_state == self.state_reading_object_files:
              self.add_object_file(line)
            elif self.current_state == self.state_reading_symbols:
              self.add_symbol(line)

  def pretty_print(self):
    archive_list = list(self.archive_map.values())
    archive_list.sort(key=lambda s: s.size, reverse=True)
  
    sdk_src_max_symbols = []
    for archive in archive_list:
      debug_log("{} object file count {}, size: {}".format(archive.name, len(archive.index_object.items()), archive.size))
      
      object_list = list(archive.index_object.values())
      object_list.sort(key=lambda s: s.size, reverse=True)
    
      rtti_size = 0
      for o in object_list:
        print("[{:>4}] sym count: {:>6}, size: {:>8} {}".format(o.file_index, len(o.symbol_list), o.size, o.name))

        rtti_info_size = 0
        if o.file_index == 103 or True:
          o.symbol_list.sort(key=lambda s: s.size, reverse=True)
          for sym in o.symbol_list:
            mangled = sym.name[sym.name.find(']') + 1:-1]
            demangled_sym = demangle(mangled)
            if demangled_sym.startswith('GCC_except'):
              # print(sym.name)
              # print(demangled_sym)
              # return
              rtti_info_size += sym.size
              # print("{:>10} {}".format(sym.size, demangled_sym))
        rtti_size += rtti_info_size
        
        if rtti_info_size > 0:
          print('typeinfo size: {}'.format(rtti_info_size))
      
      if rtti_size > 0:
        print('{} rtti size: {}'.format(archive.name, rtti_size))


if __name__ == '__main__':

  default_src = '/Users/randyqiu/dev/zego/app/queue/__builds__/_builds/ios-nocodesign-dep-8-0-cxx14/queue/zgbase.build/Release-iphoneos/ZegoQueue.build/ZegoQueue-LinkMap-normal-arm64.txt'

  import argparse
  parser = argparse.ArgumentParser()
  parser.add_argument('-src', action='store', type=str, help='path to linkmap file', default=default_src)
  arguments = parser.parse_args()

  link_map = LinkMap()
  link_map.load(arguments.src)
  link_map.pretty_print()
