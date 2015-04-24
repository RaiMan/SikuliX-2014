Debug.on(3)
#Debug.off()
App.open("Notes")
#runScript('applescript tell app "Notes" to activate') 
btn1 = Pattern("btn1.png").targetOffset(-19,30)
btn2 = "btn2.png"
wait("AllNotes.png", 10)
reg = App.focusedWindow()
reg.highlight(1)

loopSave = 60
start = time.time()
for i in range(50):
  loop1 = time.time()
  click(btn1)
  if (time.time()-loop1 < loopSave):
    click(btn2)
  if (time.time()-loop1 < loopSave): 
    click(btn1)
  if (time.time()-loop1 < loopSave): 
    type("%d %.1f %d" % (i, time.time()-loop1, int(time.time()-start)))
    type(Key.ENTER, Key.ALT)
    wait(50)
  loop2 = time.time()-loop1
  if (loop2 > loopSave * 2): 
    print "slowdown at %d after %f secs" % (i, time.time() - start)
    break
  if ((time.time()-start) > 180): 
    print "terminating after 3 minutes running fast"
    break
