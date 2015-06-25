#for e in sys.path: print e
#import testloadClass.doload
#testloadClass.doload.test()
#import compileall
#compileall.compile_dir(getBundlePath())
current = getBundlePath()
srcdir = os.path.join(current, "testload")
import org.sikuli.basics.FileManager as FM
FM.buildJar(os.path.join(current, "test.jar"), (None,),
    (os.path.join(current, "testload"),), ("testload",), None)
    