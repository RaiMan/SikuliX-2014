Settings.ActionLogs = False
Settings.InfoLogs = False
Settings.ProfileLogs = False
Debug.on(3)
runs = 1
item = 1

if RUNTIME.runningMac:
  KM = "#M." # meta key on Mac, #C. elsewhere
  WD = WHEEL_UP # direction on Mac
else:
  KM = "#M." 
  WD = WHEEL_DOWN
  
Mouse.setMouseMovedAction(2)

def closeWindow(reg):
  image = Pattern(capture(reg.getCenter().grow(100))).exact()
  write(KM+"w")
  while reg.exists(image, 0):
    wait(1)

def waitFor(img, timeout, message = "", reg = SCREEN):
  if not reg.exists(img, timeout):
    if message <> "":
      Debug.user(message)
      return None
  else:
    return reg.getLastMatch()

def exitIfNot(reg):
  if not reg: 
    exit(1)
  return reg

def pop1Handler(e):
  Debug.user("handling popup")
  loc = e.match.offset(imgPopCancel)
  loc.click()
  loc.click()
  e.stopObserver()
  
imgPop1 = "imgPop1.png"
imgPopCancel = Location(253, 73)
onAppear(imgPop1, pop1Handler)
    
url = "http://www.appannie.com/apps/ios/top/?device=iphone"
urlBlank = "about:blank"
browser = App("Google Chrome")
browser.focus()
winBrowser = App.focusedWindow()
itunes = App("iTunes")

#App.setClipboard(urlBlank)
#write("#w500.#M.l#M.v#N.#W1.")
write("#w500."+KM+"l"); paste(urlBlank); write("#N.#W1.")

#App.setClipboard(url)
#write("#w500.#M.l#M.v#N.")
write("#w500."+KM+"l"); paste(url); write("#N.#W1.")

imgTopLeft = "appAnnie.png"
imgTitle = "imgTitle.png"
imgFree = "imgFree.png"
imgIOS = "imgIOS.png"
imgPreview = Pattern("imgPreview.png").exact()
imgView = "imgView.png"
imgITunes = Pattern("imgITUNES.png").similar(0.95)

refTitle = exitIfNot(waitFor(imgTitle, 5, "[ERROR] web page did not appear: %s" % url))
click(refTitle)
regTitle = refTitle.below(1).above()

refFree = exitIfNot(waitFor(imgFree, 0, "[ERROR] web page has wrong content: %s" % imgFree))

while refTitle:
  wheel(refTitle, WD, 2)  
  refTitle = regTitle.exists(imgTitle, 0)

if item > 1:
  runs = 1

lineHeight = 48
link = exists(imgFree, 0).offset(-58,34).offset(0, (item-1)*lineHeight)
if runs == 99:
  runs = int((winBrowser.getBottomLeft().y - link.y)/lineHeight)
  Debug.user("calculated runs: %d", runs)

exitIfNot(runs > 0)

for i in range(runs):
  start = time.time()
  link.rightClick()
  write("#D2.#N.")
  
  reg = exitIfNot(waitFor(imgTopLeft, 10, "[ERROR] app sub page did not appear"))
  reg.w = reg.h = 300
  
  click(exitIfNot(waitFor(imgIOS, 10, "[ERROR] app has no IOS", reg)).offset(-12,10))

  observe(FOREVER, True)
    
  reg = exitIfNot(waitFor(imgPreview, 10, "[ERROR] app has no Preview")).below()
  exitIfNot(waitFor(imgView, 10, "[ERROR] app has no Preview", reg)).click()  
  
  exitIfNot(waitFor(imgITunes, 20, "[ERROR] iTunes did not appear"))  
  wait(1)
  exitIfNot(waitFor(imgITunes, 20, "[ERROR] iTunes did not appear"))  
  hover()  
  wait(3)
  stopObserver()
  write(KM+"q#W1.")
  
  browser.focus()
  closeWindow(App.focusedWindow())
  closeWindow(App.focusedWindow())
  link.y += lineHeight
  Debug.user("loop: %d (%f)", i, time.time() - start)
