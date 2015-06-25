#import compileall
#compileall.compile_dir(getBundlePath())
Debug.on(3)
current = getBundlePath()
srcdir = os.path.join(current, "testload")
Sikulix.buildJarFromFolder(os.path.join(current, "test.jar"), os.path.join(current, "testload_"))
print "jar loaded", load("test.jar")

for e in sys.path: print e
import testload.doload
testload.doload.test()

Sikulix.buildJarFromFolder(os.path.join(getParentPath(), "test.jar"), getBundlePath())
print "jar loaded", load("test.jar")
