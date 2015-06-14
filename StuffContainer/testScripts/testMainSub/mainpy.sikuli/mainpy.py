print "************** starting mainpy.sikuli"
import sys
from os.path import dirname
import traceback

def log_uncaught_exceptions(type, value, tb):
    print "*** exception_hook"
    for e in traceback.format_tb(tb): print e
    print "--- error"
    print str(value)

sys.excepthook = log_uncaught_exceptions

print "**** sys.argv"
for e in sys.argv: print e

dir = dirname(dirname(sys.argv[0]))
if not dir in sys.path: sys.path.append(dir)

print "*** sys.path"
for e in sys.path: print e

from subpy import *

func()