# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
# modified RaiMan 2013

from org.sikuli.basics import Debug
from org.sikuli.script import Region as JRegion
from org.sikuli.script import ObserverCallBack
from org.sikuli.script.Constants import *
import sys
import inspect

DEBUG=False

class Region(JRegion):

    # support for with:
    # override all global sikuli functions by this region's methods.
    def __enter__(self):
        exclude_list = [ 'ROI' ]
        if DEBUG: print "with: entering *****", self
        self._global_funcs = {}
        dict = sys.modules['__main__'].__dict__
        for name in dir(self):
            if name in exclude_list: continue
            try:
                if not inspect.ismethod(getattr(self,name)):
                    continue
            except:
                continue
            if dict.has_key(name):
                self._global_funcs[name] = dict[name]
                if DEBUG and name == 'checkWith': print "with: save %s ( %s )"%(name, str(dict[name])[1:])
                dict[name] = eval("self."+name)
                if DEBUG and name == 'checkWith': print "with: is now: %s"%(str(dict[name])[1:])
        return self

    def __exit__(self, type, value, traceback):
        if DEBUG: print "with: exiting ****", self
        dict = sys.modules['__main__'].__dict__
        for name in self._global_funcs.keys():
            dict[name] = self._global_funcs[name]
            if DEBUG and name == 'checkWith':
                print "with restore: %s"%(str(dict[name])[1:])
        self._global_funcs = None

#######################################################################
#---- SIKULI  PUBLIC  API
#######################################################################

# Python wait() needs to be here because Java Object has a final method: wait(long timeout).
# If we want to let Sikuli users use wait(int/long timeout), we need this Python method.
    def wait(self, target, timeout=None):
        if isinstance(target, int) or isinstance(target, long):
            target = float(target)
        if timeout == None:
            return JRegion.wait(self, target)
        else:
            return JRegion.wait(self, target, timeout)

# the new Region.text() feature (Tesseract 3) returns utf8
    def text(self):
        return JRegion.text(self).encode("utf8")

# observe(): Special setup for Jython
# assures, that in any case the same region object is used
    def onAppear(self, target, handler = None):
        if not handler:
            return self.onAppearJ(target, None)
        class AnonyObserver(ObserverCallBack):
            def appeared(self, event):
                handler(event)
        return self.onAppearJ(target, AnonyObserver())

    def onVanish(self, target, handler = None):
        if not handler:
            return self.onVanishJ(target, None)
        class AnonyObserver(ObserverCallBack):
            def vanished(self, event):
                handler(event)
        return self.onVanishJ(target, AnonyObserver())

    def onChange(self, arg1=0, arg2=None):
        if isinstance(arg1, int):
            min_size = arg1
            handler = arg2
        else:
            if (arg2 != None):
                raise Exception("onChange: Invalid parameters set")
            min_size = 0
            handler = arg1
        if not handler:
            return self.onChangeJ(min_size, None)
        class AnonyObserver(ObserverCallBack):
            def changed(self, event):
                handler(event)
        return self.onChangeJ(min_size, AnonyObserver())

    def observe(self, time=FOREVER, background=False):
        return self.observeJ(time, background)