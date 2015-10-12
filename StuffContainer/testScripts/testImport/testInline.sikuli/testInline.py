import org.sikuli.util.JythonHelper as JH
JH.get().addSysPath(getBundlePath())
print "***** at start"
for e in sys.path: print e
import inline
inline.func()
print "***** at end"
for e in sys.path: print e