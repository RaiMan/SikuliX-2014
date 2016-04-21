# Copyright 2010-2016, Sikuli.org, sikulix.com
# Released under the MIT License.
# modified RaiMan 2016

import org.sikuli.script.Region as JRegion
from org.sikuli.script.Constants import *
import sys
import inspect

DEBUG = False

class Region(JRegion):
  # support for with:
  # override all global sikuli functions by this region's methods.
  def __enter__(self):
    exclude_list = ['ROI']
    self._global_funcs = {}
    dict = sys.modules['__main__'].__dict__
    for name in dir(self):
      if name in exclude_list: continue
      try:
        if not inspect.ismethod(getattr(self, name)):
          continue
      except:
        continue
      if dict.has_key(name):
        self._global_funcs[name] = dict[name]
        dict[name] = eval("self." + name)
    return self

  def __exit__(self, type, value, traceback):
    dict = sys.modules['__main__'].__dict__
    for name in self._global_funcs.keys():
      dict[name] = self._global_funcs[name]
    self._global_funcs = None

  #######################################################################
  # ---- SIKULI  PUBLIC  API
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

  # still needed, to be backwards compatible
  def observe(self, waitTime=FOREVER, background=False):
    return JRegion.observeJ(waitTime, background)
