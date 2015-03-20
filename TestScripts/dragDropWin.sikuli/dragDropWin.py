try:
  hover()
except:
  import sys
  import os
  dir = os.path.dirname(sys.argv[0])
  import org.sikuli.basics.SikulixForJython
  from sikuli import *
  setBundlePath(dir)

popup("move mouse to source and press enter")
src = Mouse.at()
hover(src.offset(0,50))
hover(src)
popup("move mouse to target and press enter")
tgt = Mouse.at()
hover(tgt.offset(0,50))
hover(tgt)
click(src)
dragDrop(src, tgt)