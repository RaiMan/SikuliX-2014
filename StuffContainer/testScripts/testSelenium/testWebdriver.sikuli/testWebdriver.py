from selenium import webdriver
#from selenium.webdriver.firefox import firefox_binary
#driver = webdriver.Firefox(firefox_binary("/Applications/Firefox.app/Contents/MacOS/firefox"))
driver = webdriver.Firefox()
#import platform
#print platform.system()
driver.get("http://www.python.org")