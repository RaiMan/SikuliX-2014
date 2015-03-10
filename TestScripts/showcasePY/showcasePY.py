popup("Hello from Git showcase Jython")

import java.awt.Desktop as DT
import java.net.URI as URI

if not DT.isDesktopSupported():
	exit(1)

dt = DT.getDesktop()
dt.browse(URI("http://sikulix.com"))

r = wait("sxpower", 5)
use(r.below(100))
click("quickstart")
use()
wait(3)

if RUNTIME.runningMac:
  write("#M.w")
else:
  write("#C.w")
