print "*********** starting **********", sys.argv[0].split("testScripts")[1]
#Debug.on(3)
Debug.off()

btnOn = "btnOn.png"
btnOff = "btnOff.png"

imgList = (btnOn, btnOff)

bs = switchApp("Safari"); wait(2)
r = bs.window()
r.w = 75
r.h = 40

start = time.time()
m = r.findBest(imgList)
score = int(m.getScore() * 10000)/100.0
timer = int((time.time() - start) * 1000)
print "*** should be ON"
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)

click(getBottomRight().offset(-5, -5)); wait(2)

start = time.time()
m = r.findBest(imgList)
score = int(m.getScore() * 10000)/100.0
timer = int((time.time() - start) * 1000)
print "*** should be OFF"
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)
