# Copyright 2010-2013, Sikuli.org
# Released under the MIT License.
# modified RaiMan 2013

import imp
import sys

import Sikuli
from org.sikuli.basics import Debug
from org.sikuli.script import ImagePath
import os

def _stripPackagePrefix(module_name):
    pdot = module_name.rfind('.')
    if pdot >= 0:
        return module_name[pdot+1:]
    return module_name
  
def _debug():
  if Debug.getDebugLevel() > 2:
    return True
  else:
    return False;

class SikuliImporter:

    class SikuliLoader:
        def __init__(self, path):
            self.path = path
        
        def _load_module(self, fullname):
            if _debug(): print "SikuliLoader._load_module", fullname
            try:
                (file, pathname, desc) =  imp.find_module(fullname)
            except:
                etype, evalue, etb = sys.exc_info()
                evalue = etype(fullname + ".sikuli has no " + fullname + ".py")
                raise etype, evalue, etb
              
            try:
                return imp.load_module(fullname, file, pathname, desc)
            except:
                etype, evalue, etb = sys.exc_info()
                evalue = etype("!!WHILE IMPORTING!! %s" % evalue)
                raise etype, evalue, etb
            finally:
                if file:
                    file.close()
        
        def load_module(self, module_name):
            if _debug(): print "SikuliLoader.load_module", module_name, self.path
            module_name = _stripPackagePrefix(module_name)
            if ImagePath.add(self.path):
              if _debug(): print "SikuliLoader.load_module: ImagePath add:", self.path
            else:
              if _debug(): print "SikuliLoader.load_module: ImagePath not added:", self.path
              return None
            Sikuli.addModPath(self.path)
            return self._load_module(module_name)

    def _find_module(self, module_name, fullpath):
        fullpath = fullpath + "/" + module_name + ".sikuli"
        if os.path.exists(fullpath):
            if _debug(): print "SikuliImporter found", fullpath
            return self.SikuliLoader(fullpath)
        return None

    def find_module(self, module_name, package_path):
        module_name = _stripPackagePrefix(module_name)
        if module_name[0:1] == "*": 
            return None
        if package_path:
            paths = package_path
        else:
            paths = sys.path
        if _debug(): 
          print "SikuliImporter.find_module", module_name
          for e in paths:
            print e
          print "SikuliImporter.find_module --- end ---"
        for path in paths:
            mod = self._find_module(module_name, path)
            if mod:
                return mod
        if Sikuli.load(module_name +".jar"):
            Debug.log(2,module_name + ".jar loaded")
            return None
        return None

sys.meta_path.append(SikuliImporter())
del SikuliImporter
