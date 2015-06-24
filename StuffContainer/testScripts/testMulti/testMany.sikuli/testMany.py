imgLogo = "imgLogo.png" 

bc = App("chrome")
bf = App("firefox")
be = App("iexplore")
brs = (be, bf, bc)

if not bc.isRunning():
  print "not running"

exit()

s0 = SCREEN
s1 = Screen(1)
s2 = Screen(2)
scrs = (s0, s1, s2)
for scr in scrs:
  scr.highlight(1)
  
for scr in scrs:
  reg = scr.getCenter().grow(50)
  reg.highlight(1)
  reg.hover()
  wait(1)

for br in brs:
  if not br.isRunning():
    continue
  br.focus()
  reg = App.focusedWindow().highlight(1)
  reg.find(imgLogo).hover()
  wait(1)
hover(s0)

