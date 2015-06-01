print "*********** starting **********", sys.argv[0].split("testScripts")[1]
Debug.off()

if RUNTIME.runningMac:
  te = switchApp("textedit"); wait(1)
else:
  App.focus("_findBestScreen"); wait(1)
  
r = App.focusedWindow()
r.setAutoWaitTimeout(0)
r.highlight(1)

imgA = "imgA.png"
imgB = "imgB.png"
imgC = "imgC.png"
imgD = "imgD.png"
imgE = "imgE.png"
imgF = "imgF.png"
imgG = "imgG.png"
imgH = "imgH.png"
imgI = "imgI.png"
imgJ = "imgJ.png"

imgList = (imgA, imgB, imgC, imgD, imgE, imgF, imgG, imgH, imgI, imgJ,
           imgA, imgB, imgC, imgD, imgE, imgF, imgG, imgH, imgI, imgJ,
           imgA, imgB, imgC, imgD, imgE, imgF, imgG, imgH, imgI, imgJ,
           imgA, imgB, imgC, imgD, imgE, imgF, imgG, imgH, imgI, imgJ)

imgList = imgList[:2]
print "using %d images", len(imgList)

def findBestX(reg, imgList):
  score = 0;
  match = None;
  for img in imgList:
    m = r.exists(img, 0)
    if m and m.getScore() > score:
      match = m
      score = m.getScore()
  return match

start = time.time()
m = findBestX(r, imgList)
timer = int((time.time() - start) * 1000)
score = int(m.getScore() * 10000)/100.0
print "Xfound: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)

start = time.time()
m = r.findBest(imgList)
timer = int((time.time() - start) * 1000)
score = int(m.getScore() * 10000)/100.0
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)

start = time.time()
r = m.grow(10)
m = findBestX(r, imgList)
score = int(m.getScore() * 1000)/10.0
timer = int((time.time() - start) * 1000)
print "Xfound: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)

start = time.time()
m = r.findBest(imgList)
score = int(m.getScore() * 1000)/10.0
timer = int((time.time() - start) * 1000)
print "found: %s at %.2f%% in %d msec" % (imgList[m.getIndex()], score, timer)
m.highlight(1)
