img = Pattern("1412875178325.png").exact()
reg = exists(img)

i = 0
start = time.time()
while True:
  if reg.exists(img, 0):
    wait(0.01)
    i += 1
  else:
    print i, (time.time()-start)/i
    break