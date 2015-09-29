try:
  from selenium4sikulix import *
except:
  Debug.error("SikuliX environment not ready for Selenium4Sikulix")
  
driver = getFirefox()
if not driver: 
  Debug.error("Firefox could not be started")
  exit(1)

if RUNTIME.runningMac:
  # on Mac: the appname when running is not Firefox, but firefox-bin 
  ff = App("firefox-bin")
  webXoff = 0
  webYoff = 80
else:
  ff = App("Firefox")
  webXoff = 7
  webYoff = 88

if ff.isRunning():
  print "Firefox is started:", ff
  winFF = App.focusedWindow()
  winFF.highlight(2)
else:
  Debug.error("Firefox application not found")
  exit(1)  

# top left corner of web content on Screen
# offsets evaluated manually 
webTL = Location(winFF.x + webXoff, winFF.y + webYoff)

driver.get("http://sikulix.com")
items = driver.findElements(By.className("wsite-menu-item-wrap"))
mQuickStart = None
print "*** listing all menu entries"
for item in items:
  menu = item.getText()
  print menu
  if menu.startswith("QuickStart"):
    mQuickStart = item
    
print "*** act on QuickStart"
if mQuickStart:
  loc = mQuickStart.getLocation()
  loc = webTL.offset(loc.x, loc.y)
  hover(loc)
  wait(1)
  dim = mQuickStart.getSize()
  menu = Region(loc.x, loc.y, dim.width, dim.height)
  hover(webTL)
  menu.highlight(2)
  click(Mouse.at()) # reactivate page after loss of focus after highlight()

  # looks like clicks on div elements do not (always) work
  #mQuickStart.click()
  click(menu) # but SikuliX can ;-)
  wait(3)
  driver.quit()
