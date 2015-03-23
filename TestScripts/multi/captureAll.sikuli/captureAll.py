scrs = []
dir = getBundlePath()
for n in range(getNumberScreens()):
  scrs.append(Screen(n).getScreen())
for scr in scrs:
  filename = "screen%d" % scr.getID()
  scr.capture().getFile(dir, filename)
