runScript("""
robot

*** Variables ***
${USERNAME}               demo
${PASSWORD}               mode
${TESTSITE}               http://test.sikuli.de

*** Settings ***
Library           ./inline/LoginLibrary
Test Setup        start firefox and goto testsite    ${TESTSITE}
Test Teardown     stop firefox

*** Test Cases ***
User can log in with correct user and password
    Attempt to Login with Credentials    ${USERNAME}    ${PASSWORD}
    Status Should Be    Accepted

User cannot log in with invalid user or bad password
    Attempt to Login with Credentials    betty    wrong
    Status Should Be    Denied

""")

try:
  from selenium4sikulix import *
except:
  Debug.error("SikuliX environment not ready for Selenium4Sikulix")
from sikuli import *

class LoginLibrary(object):

  def __init__(self):
    self.driver = None
    self.winFF = None
    self.webTL = None
    self.status = "" 

  def start_firefox_and_goto_testsite(self, page):
    self.driver = getFirefox()
    if not self.driver: 
      Debug.error("Firefox could not be started")
    else:    
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
        # top left corner of web content on Screen
        # offsets evaluated manually 
        self.webTL = Location(winFF.x + webXoff, winFF.y + webYoff)
        self.winFF = winFF
      else:
        Debug.error("Firefox application not found")
      # goto given website
      if not page == "":
        self.driver.get(page)

  def stop_firefox(self):
    print "stop_firefox"
    if self.driver:
      self.driver.quit()
      
  def attempt_to_login_with_credentials(self, username, password):
    print "attempt_to_login_with_credentials", username, password
    login = Pattern("login.png").targetOffset(13,106)
    match = self.winFF.exists(login)
    if match:
      match.highlight(2)
    item = self.driver.findElement(By.name("username_field"))
    visualName = self._getVisual(item)
    if visualName:
      visualName.highlight(2)
    item.click()
    type(username)
    item = self.driver.findElement(By.name("password_field"))
    visualPass = self._getVisual(item)
    if visualPass:
      visualPass.highlight(2)
    item.click()
    type(password)
    item = self.driver.findElement(By.name("login_button"))
    visualName = self._getVisual(item)
    if visualName:
      visualName.highlight(2)
    item.click()
    
  def status_should_be(self, expected):
    print "status_should_be", expected
    wait(2)

  def _getVisual(self, item):
    if not item:
      return None
    else:
      loc = item.getLocation()
      loc = self.webTL.offset(loc.x, loc.y)
      dim = item.getSize()
      return Region(loc.x, loc.y, dim.width, dim.height)
 