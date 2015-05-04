Debug.on(3)
#Debug.off()
#switchApp("NetBeans 8.0.2")
btn1 = "btn1.png"
btn2 = "btn2.png"
mBtn1 = wait(btn1)
click(mBtn1)
click(mBtn1)
wait(1)
mBtn2 = wait(btn2)
loopSave = 15
start = time.time()
for i in range(50):
  print "click1 %d %d" % (i, int(time.time()-start))
  loop1 = time.time()
  click(mBtn1)
  wait(1)
  print "click2 %d %d" % (i, int(time.time()-start))
  if (time.time()-loop1 < loopSave):
    click(mBtn2)
  loop2 = time.time()-loop1
  if (loop2 > loopSave * 2): 
    print "slowdown at %d after %f secs" % (i, time.time() - start)
    break
  if ((time.time()-start) > 300): 
    print "terminating after 3 minutes running fast"
    break
