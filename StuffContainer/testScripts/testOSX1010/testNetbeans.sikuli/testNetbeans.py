Debug.on(3)
#Debug.off()
switchApp("NetBeans 8.0.2")
btn1 = "btn1.png"
btn2 = "btn2.png"
click(btn1)
click(btn2)
wait(1)

loopSave = 15
start = time.time()
for i in range(50):
  loop1 = time.time()
  click(btn1)
  wait(btn2)
  type("%d %d" % (i, int(time.time()-start)))
  wait(1)
  if (time.time()-loop1 < loopSave):
    click(btn2)
  loop2 = time.time()-loop1
  if (loop2 > loopSave * 2): 
    print "slowdown at %d after %f secs" % (i, time.time() - start)
    break
  if ((time.time()-start) > 180): 
    print "terminating after 3 minutes running fast"
    break
