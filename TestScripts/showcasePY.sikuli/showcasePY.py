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


if RUNTIME.runningMac:
  wait(2)
  write("#M.w")
else:
  popup("Click OK:\n- to close the browser window\n- and terminate")
  write("#C.w")
