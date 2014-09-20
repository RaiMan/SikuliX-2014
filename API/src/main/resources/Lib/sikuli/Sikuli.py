# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
# modified RaiMan 2013

from __future__ import with_statement
from org.sikuli.basics import Debug
Debug.log(3, "Jython: sikuli: Sikuli: starting init")
import time
import __builtin__
#import __main__
import types
import sys
import os
import inspect

Debug.log(4, "Jython: sikuli: Sikuli: constants")
import org.sikuli.script.FindFailed as FindFailed
from org.sikuli.script.FindFailedResponse import *
from org.sikuli.script.Constants import *
import org.sikuli.script.Button as Button
from org.sikuli.script.Button import WHEEL_UP, WHEEL_DOWN
from org.sikuli.basics import OS

Debug.log(4, "Jython: sikuli: Sikuli: import Region")
from org.sikuli.script import Region as JRegion
from Region import *
from org.sikuli.script import Observing

Debug.log(4, "Jython: sikuli: Sikuli: import Screen")
from org.sikuli.script import Screen as JScreen
from Screen import *

Debug.log(4, "Jython: sikuli: Sikuli: Env.addHotkey")
from Env import *

Debug.log(4, "Jython: sikuli: Sikuli: import Match")
from org.sikuli.script import Match
Debug.log(4, "Jython: sikuli: Sikuli: import Pattern")
from org.sikuli.script import Pattern
Debug.log(4, "Jython: sikuli: Sikuli: import Location")
from org.sikuli.script import Location
Debug.log(4, "Jython: sikuli: Sikuli: import ScreenUnion")
from org.sikuli.script import ScreenUnion
Debug.log(4, "Jython: sikuli: Sikuli: import Finder")
from org.sikuli.script import Finder
from org.sikuli.script import ImageFinder
from org.sikuli.script import ImageFind

Debug.log(4, "Jython: sikuli: Sikuli: import Image")
from org.sikuli.script import Image
from org.sikuli.script import ImageGroup

Debug.log(4, "Jython: sikuli: Sikuli: import ImagePath")
from org.sikuli.script import ImagePath

Debug.log(4, "Jython: sikuli: Sikuli: import App")
from org.sikuli.script import App
Debug.log(4, "Jython: sikuli: Sikuli: import KeyBoard/Mouse")
from org.sikuli.script import Key
from org.sikuli.script import KeyModifier
from org.sikuli.script.KeyModifier import KEY_CTRL, KEY_SHIFT, KEY_META, KEY_CMD, KEY_WIN, KEY_ALT
from org.sikuli.script import Mouse
from org.sikuli.script import Keys

Debug.log(4, "Jython: sikuli: Sikuli: import from Basics")
from org.sikuli.basics import Settings
from org.sikuli.basics import ExtensionManager

Debug.log(4, "Jython: sikuli: Sikuli: import from compare")
from org.sikuli.script.compare import DistanceComparator
from org.sikuli.script.compare import VerticalComparator
from org.sikuli.script.compare import HorizontalComparator

Debug.log(4, "Jython: sikuli: Sikuli: init SikuliImporter")
import SikuliImporter

Debug.log(4, "Jython: sikuli: Sikuli: import Sikulix")
from org.sikuli.script import Sikulix

Debug.log(4, "Jython: sikuli: Sikuli: import ScriptRunner")

try:
	from org.sikuli.scriptrunner import ScriptRunner
except:
  pass

##
# some support for handling unicode and strings
#
## use instead of print if unicode strings present
# usage: uprint(s1, u1, u2, u3, s3, ...)
#
def uprint(*args):
    for e in args[:-1]:
        if isinstance(e, str): print e,
        else: print e.encode("utf8"),
    if isinstance(args[-1], str): print args[-1]
    else: print args[-1].encode("utf8")

##
# to make an utf8-encoded string from a str object
#
def unicd(s):
    return ucode(s)

def ucode(s):
    return (unicode(s, "utf8"))

##
# loads a Sikuli extension (.jar) from
#  1. user's sikuli data path
#  2. bundle path
#
def load(jar):
    def _load(abspath):
        if os.path.exists(abspath):
            if not abspath in sys.path:
                sys.path.append(abspath)
            return True
        return False

    if _load(jar):
        return True
    path = getBundlePath()
    if path:
        jarInBundle = os.path.join(path, jar)
        if _load(jarInBundle):
            return True
    path = ExtensionManager.getInstance().getLoadPath(jar)
    if path and _load(path):
        return True
    return False

## ----------------------------------------------------------------------
# append the given path sys.path if not yet contained
#
def addImportPath(path):
    _addModPath(path)

##
# append the given path image path list if not yet contained
#
def addImagePath(path):
    ImagePath.add(path)

##
# return the current image path list
#
def getImagePath():
    return [e.pathGiven for e in ImagePath.getPaths() if e]

##
# remove the given path from the image path
#
def removeImagePath(path):
    ImagePath.remove(path)

##
# reset the image path, so it only contains the bundlepath
#
def resetImagePath(path = None):
    if not path:
      path = getBundlePath();
    ImagePath.reset(path)

## ----------------------------------------------------------------------
# Sets the path for searching images in all Sikuli Script methods. <br/>
# Sikuli IDE sets this to the path of the bundle of source code (.sikuli)
# automatically. If you write Sikuli scripts using Sikuli IDE, you should
# know what you are doing.
#
def setBundlePath(path):
    ImagePath.setBundlePath(path)

##
# return the current bundlepath (usually the folder .sikuli) 
# or None if no bundlepath is defined
# no trailing path sep
#
def getBundlePath():
    return ImagePath.getBundlePath()

##
# return the current bundlepath (usually the folder .sikuli) 
# or None if no bundlepath is defined
# with a trailing path separator (for string concatenation)
#
def getBundleFolder():
    path = ImagePath.getBundlePath()
    if not path: return None
    return path + Settings.getFilePathSeperator();

##
# return the parent folder of the current bundlepath
# (usually the folder containing the current script folder.sikuli)
# or None if no bundlepath is defined
# no trailing path sep
#
def getParentPath():
    path = ImagePath.getBundlePath()
    if not path: return None
    return os.path.dirname(makePath(getBundlePath()));

##
# return the parent folder of the current bundlepath
# (usually the folder containing the current script folder.sikuli)
# or None if no bundlepath is defined
# no trailing path sep
#
def getParentFolder():
    path = getParentPath()
    if not path: return None
    return path + Settings.getFilePathSeperator();

##
# make a valid path by by using os.path.join() with the given elements
# always without a trailing path separator
#
def makePath(*paths):
  if len(paths) == 0: return None
  path = paths[0]
  if len(paths) > 1: 
    for p in paths[1:]:
      path = os.path.join(path, p)
  if path[-1] == Settings.getFilePathSeperator():
    return os.path.dirname(path)
  return path

##
# make a valid path by by using os.path.join() with the given elements
# with a trailing path separator (for string concatenation)
#
def makeFolder(*paths):
  path = makePath(*paths)
  if not path: return None
  path = path + Settings.getFilePathSeperator()
  return path

## ----------------------------------------------------------------------
# Sikuli shows actions (click, dragDrop, ... etc.) if this flag is set to <i>True</i>.
# The default setting is <i>False</i>.
#
def setShowActions(flag):
    Settings.setShowActions(flag)

## ----------------------------------------------------------------------
# Shows a message dialog containing the given message.
# @param msg The given message string.
# @param title gets the window title.
def popup(msg, title="Sikuli Info"):
    Sikulix.popup(msg, title)

# Show error popup (special icon) containing the given message.
# @param msg The given message string.
# @param title gets the window title.
def popError(msg, title="Sikuli Error"):
    Sikulix.popError(msg, title)

# Show a popup containing the given message asking for yes or no
# @param msg The given message string.
# @param title gets the window title.
# @return True if answered Yes, else False
def popAsk(msg, title="Sikuli Decision"):
    return Sikulix.popAsk(msg, title)

##
# Shows a question-message dialog requesting input from the user.
# @param msg The message to display.
# @param default The preset text of the input field (default empty).
# @param title the title for the dialog (default: Sikuli input request)
# @param hidden =true makes the dialog run as a password input (input hidden with bullets)
# @return The user's input string.
#
def input(msg="", default="", title="Sikuli Input", hidden=False):
    if (hidden):
      default = ""
    return Sikulix.input(msg, default, title, hidden)

##
# Shows a dialog request to enter text in a multiline text field
# Though not all text might be visible, everything entered is delivered with the returned text
# The main purpose for this feature is to allow pasting text from somewhere
# @param msg the message to display.
# @param title the title for the dialog (default: Sikuli input request)
# @param lines the maximum number of lines visible in the text field (default 9)
# @param width the maximum number of characters visible in one line (default 20)
# @return The user's input including the line breaks.
def inputText(msg="", title="Sikuli Text", lines=0, width=0):
    return Sikulix.inputText(msg, title, width, lines)

##
# Shows a dialog requesting to select an entry from the drop down list
# @param msg the message to display.
# @param title the title for the dialog
def select(msg="", title="Sikuli Select", options=(), default=None):
    if len(options) == 0:
        return ""
    if default:
        if not __builtin__.type(default) is types.StringType:
            try:
                default = options[default]
            except:
                default = None
    return Sikulix.popSelect(msg, title, options, default)

## ----------------------------------------------------------------------
def capture(*args):
    scr = ScreenUnion()
    if len(args) == 0:
        simg = scr.userCapture()
        if simg:
            return simg.getFilename()
        else:
            return None
    elif len(args) == 1:
        if __builtin__.type(args[0]) is types.StringType or __builtin__.type(args[0]) is types.UnicodeType:
            simg = scr.userCapture(args[0])
            if simg:
                return simg.getFilename()
            else:
                return None
        else:
            return scr.capture(args[0]).getFilename()
    elif len(args) == 4:
        return scr.capture(args[0], args[1], args[2], args[3]).getFilename()
    else:
        return None


def selectRegion(msg=None):
    if msg:
        r = ScreenUnion().selectRegion(msg)
    else:
        r = ScreenUnion().selectRegion()
    if r:
        return Region(r)
    else:
        return None

## ----------------------------------------------------------------------
# set the default screen to given or primary screen
#
# TODO where else to remember an opened remote screen?
remoteScreen = None

def use(scr = None, remote = False):
  if remote:
    theGlobals = inspect.currentframe().f_back.f_back.f_globals
  else:
    theGlobals = inspect.currentframe().f_back.f_globals
  global remoteScreen
  if remoteScreen:
    remoteScreen.close()
    remoteScreen = None
  if not scr:
    SCREEN = Screen()
  else:
    SCREEN = scr
  Debug.log(3, "Jython: requested to use as default region: " + SCREEN.toStringShort())
  globals()['SIKULISAVED'] = _exposeAllMethods(SCREEN, globals().get('SIKULISAVED'), theGlobals, None)
  theGlobals['SCREEN'] = SCREEN
  if remote:
    remoteScreen = SCREEN
  return SCREEN

##
# set the default screen to given remote screen
#
def useRemote(adr, port = 0):
  global remoteScreen
  import org.sikuli.script.ScreenRemote as SR
  SCREEN = SR(adr, str(port))
  if SCREEN.isValid():
    return use(SCREEN, True)
  else:
    return None

## ----------------------------------------------------------------------
# Switches the frontmost application to the given application.
# If the given application is not running, it will be launched by openApp()
# automatically. <br/>
# Note: On Windows, Sikule searches in the text on the title bar
# instead of the application name.
# @param app The name of the application. (case-insensitive)
#
def switchApp(app):
    return App.focus(app)

##
# Opens the given application. <br/>
# @param app The name of an application if it is in the environment variable PATH, or the full path to an application.
#
def openApp(app):
    return App.open(app)

##
# Closes the given application. <br/>
# @param app The name of the application. (case-insensitive)
#
def closeApp(app):
    return App.close(app)

##
# Sleeps until the given amount of time in seconds has elapsed.
# @param sec The amount of sleeping time in seconds.
def sleep(sec):
    time.sleep(sec)

## ----------------------------------------------------------------------
def reset():
  JScreen.resetMonitors();
  use();
  ALL = JScreen.all()

##
# shutdown and return given exit code
#
def exit(code=0):
    global remoteScreen
    if remoteScreen:
      remoteScreen.close()
      remoteScreen = None
    Sikulix.cleanUp(code)
    sys.exit(code)

## ----------------------------------------------------------------------
# Runs the given string command.
# @param msg The given string command.
# @return Returns the output from the executed command.
def run(cmd):
    return Sikulix.run(cmd)

##
# helper functions, that can be used when sorting lists of regions
#
def byDistanceTo(x, y=None):
    """ Method to compare two Region objects by distance of their top left.
    or a regions top left to the given point by coordinates"""
    return DistanceComparator(x, y)

def byX(m):
    """ Method to compare two Region objects by x value. """
    return HorizontalComparator().compare

def byY(m):
    """ Method to compare two Region objects by y value. """
    return VerticalComparator().compare

def verticalComparator():
    """ Method to compare two Region objects by y value. """
    return VerticalComparator().compare

def horizontalComparator():
    """ Method to compare two Region objects by x value. """
    return HorizontalComparator().compare

def distanceComparator(x, y=None):
    """ Method to compare two Region objects by distance of their top left.
    or a regions top left to the given point by coordinates"""
    if y is None:
        return DistanceComparator(x).compare # x is Region or Location
    return DistanceComparator(x, y).compare # x/y as coordinates

##
################## internal use only ###########################################
#
def _addModPath(path):
    if path[-1] == Settings.getFilePathSeperator():
        path = path[:-1]
    if not path in sys.path:
        sys.path.append(path)

def _exposeAllMethods(anyObject, saved, theGlobals, exclude_list):
    if not exclude_list:
        exclude_list = [ 'class', 'classDictInit', 'clone', 'equals', 'finalize',
                    'getClass', 'hashCode', 'notify', 'notifyAll',
                    'toGlobalCoord', 'toString', 'getLocationFromPSRML', 'getRegionFromPSRM',
                   'capture', 'selectRegion', 'create', 'observeInBackground', 'waitAll',
                    'updateSelf', 'findNow', 'findAllNow', 'getEventManager',
                    'lastMatch', 'lastMatches', 'lastScreenImage', 'lastScreenImageFile']
    #Debug.log(3, "Sikuli: _exposeAllMethods: %s called from: %s", anyObject, theGlobals['__name__'])
    tosave = []
    if not saved:
      saved = []
    for name in dir(anyObject):
        if name in exclude_list: continue
        try:
            if not inspect.ismethod(getattr(anyObject,name)): continue
        except:
            continue
        if name[0] != '_' and name[:7] != 'super__':
            try:
              saved.remove(name)
            except:
              pass
            tosave.append(name)
            #print "added:", name
            theGlobals[name] = eval("anyObject."+name)
            if name == 'checkWith': Debug.log(3, "%s %s", name, str(dict[name])[1:])
    for name in saved:
        if name in theGlobals:
          #print "removed:", name
          theGlobals.pop(name)
    return tosave

############### set SCREEN as primary screen at startup ################
use()
ALL = JScreen.all()
Debug.log(3, "Jython: sikuli: Sikuli: ending init")

