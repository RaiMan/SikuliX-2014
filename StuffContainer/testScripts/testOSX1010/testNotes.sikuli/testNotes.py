#Debug.on(3)
Debug.off()
App.open("Notes")
btn1 = Pattern("btn1.png").targetOffset(-19,30)
btn2 = "btn2.png"
wait("AllNotes.png", 10)
reg = App.focusedWindow()
reg.highlight(1)

loopSave = 15
start = time.time()
for i in range(100):
  loop1 = time.time()
  click(btn1)
  if (time.time()-loop1 < loopSave):
    click(btn2)
  if (time.time()-loop1 < loopSave): 
    click(btn1)
  if (time.time()-loop1 < loopSave): 
    type("%d %f" % (i, time.time()-loop1))
    type(Key.ENTER, Key.ALT)
    wait(1)
  loop2 = time.time()-loop1
  if (loop2 > loopSave * 2): 
    print "slowdown at %d after %f secs" % (i, time.time() - start)
    break