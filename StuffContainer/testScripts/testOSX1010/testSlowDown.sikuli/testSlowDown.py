switchApp("Safari")
btnEdit = "btnEdit.png"
r1 = Region(0,0,100,100)
r2 = Region(0,700,100,100)
#Settings.MoveMouseDelay = 0

for i in range(50):
  r1.highlight(1)
  start = time.time()
  click(btnEdit)
  click("close.png")
  wait(2)
  click(btnEdit)
  click("close.png") 
  print i, time.time()-start
  r2.highlight(1)
