popup("Hello from Git showcase Jython")

import java.awt.Desktop as DT
import java.net.URI as URI

if not DT.isDesktopSupported():
	exit(1)

dt = DT.getDesktop()
dt.browse(URI("http://sikulix.com"))

Debug.on(3)
r = ALL.wait("sxpower.png", 30)
r.below(100).click("quickstart.png")

popup("Click OK:\n- to close the browser window\n- and terminate")

if RUNTIME.runningMac:
  write("#M.w")
else:
  write("#C.w")
