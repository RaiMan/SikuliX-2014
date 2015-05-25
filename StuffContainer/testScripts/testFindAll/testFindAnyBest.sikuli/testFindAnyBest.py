print "*********** starting **********", sys.argv[0].split("testScripts")[1]
Debug.on(3)
te = switchApp("textedit"); wait(1)
r = te.window()
r.setAutoWaitTimeout(0)

imgA = "imgA.png"
imgB = "imgB.png"
imgC = "imgC.png"

imgList = (imgA, imgB, imgC)

start = time.time()
m = r.findBest(imgList)
score = int(m.getScore() * 10000)/100.0
timer = int((time.time() - start) * 1000)
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)

start = time.time()
m = m.grow(5).findBest(imgList)
score = int(m.getScore() * 1000)/10.0
timer = int((time.time() - start) * 1000)
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)
