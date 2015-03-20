try:
  switchApp("Safari")
except:
  import sys
  import os
  dir = os.path.dirname(sys.argv[0])
  import org.sikuli.basics.SikulixForJython
  from sikuli import *
  setBundlePath(dir)
  switchApp("Safari")

hover("logo.png")
src = Mouse.at()
tgt = src.offset(0, -250)
hover(tgt)
tgt = Mouse.at()

dragDrop(src, tgt)

hover(src)
mouseDown(Button.LEFT)
hover(tgt)
mouseUp()
click(tgt)
