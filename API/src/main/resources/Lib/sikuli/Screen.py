# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
# modified RaiMan 2013

import types
import sys

from org.sikuli.script import RunTime as RUNTIME
from org.sikuli.script import Screen as JScreen
from org.sikuli.basics import Debug
from Region import *
import Sikuli

class Screen(Region):
    def __init__(self, id=None):
        try:
            #Debug.log(3, "Screen.py: init: %s", id)
            if id != None:
                r = JScreen.getBounds(id)
                s = JScreen.getScreen(id)
            else:
                id = JScreen.getPrimaryId()
                r = JScreen.getBounds(id)
                s = JScreen.getScreen(id)
            (self.x, self.y, self.w, self.h) = (int(r.getX()), int(r.getY()), \
                            int(r.getWidth()), int(r.getHeight()))
            #Debug.log(3, "Screen.py: before initScreen: %s", s)
            self.initScreen(s)
        except:
            Debug.error("Jython: init: exception while initializing Screen\n%s", sys.exc_info(0))
            sys.exit(1)

    @classmethod
    def getNumberScreens(cls):
        return JScreen.getNumberScreens()

    @classmethod
    def all(cls):
        return JScreen.all()

#TODO check wether needed (Region.setROI() resets to bounds too)
#    def resetROI(self):
#        # Debug.log(3, "Screen.py: resetROI: %s", self.getScreen())
#       self.setRect(self.getScreen().getBounds())

    def getBounds(self):
        return self.getScreen().getBounds()

    def toString(self):
        return self.getScreen().toString()

## ----------------------------------------------------------------------

