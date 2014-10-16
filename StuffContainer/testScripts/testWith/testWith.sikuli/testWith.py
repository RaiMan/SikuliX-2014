reg = Region(0,0,100,100)
reg.setAutoWaitTimeout(0.5)
print "reg.TO:", reg.getAutoWaitTimeout()

with reg:
  print "TO:", getAutoWaitTimeout()
  start = time.time()
  i = 0
  while not exists(Pattern("1413454244155.png").exact()):
    print time.time() - start
    start = time.time()
    i += 1
    if i > 1: break
