scrs = []
dir = getBundlePath()
for n in range(getNumberScreens()):
  scrs.append(Screen(n))
for s in scrs:
  scr = s.getScreen()
  filename = "screen%d" % scr.getID()
  scr.capture(s).getFile(dir, filename)
