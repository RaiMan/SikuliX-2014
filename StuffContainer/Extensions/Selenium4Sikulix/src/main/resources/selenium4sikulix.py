import org.openqa.selenium.By as By
import org.openqa.selenium.WebDriver as WebDriver
import org.openqa.selenium.WebElement as WebElement
import org.openqa.selenium.htmlunit.HtmlUnitDriver as HtmlUnitDriver
import org.openqa.selenium.firefox.FirefoxDriver as FirefoxDriver;

import time
waitingUntil = time.time()

def getFirefox():
    try:    
        driver = FirefoxDriver()
    except:
        driver = None;
    return driver

def setWaiting(seconds):
    global waitingUntil
    waitingUntil = time.time() + seconds

def waiting():
    if time.time() > waitingUntil:
        return False
    return True