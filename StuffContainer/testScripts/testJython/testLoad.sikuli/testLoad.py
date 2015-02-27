load("../../testLoadSomeJar")
load("testSomeExtension")
print "***** sys.path"
for e in sys.path: print e
RunTime.get().dumpClassPath()