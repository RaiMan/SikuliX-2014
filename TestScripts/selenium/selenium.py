try:
  from selenium4sikulix import *
except:
  Debug.error("SikuliX environment not ready for Selenium4SikuliX")

driver = getFirefox()
if not driver:
  Debug.error("Firefox could not be started")
  exit(1)

ff = App("Firefox")
if ff.isRunning():
  print "Firefox is started:", ff
else:
  Debug.error("Firefox application not found")

winFF = App.focusedWindow()
winFF.highlight(2)

# top left corner of web content on Screen
# offsets evaluated manually (Firefox on Windows)
webXoff = 7
webYoff = 88
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
  click(Mouse.at()) # reactivate page
  hover(menu)
  mQuickStart.click() # seems not to work
#  click(Mouse.at())
  wait(3)
  driver.quit()
