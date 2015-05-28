# chrome to open a given webpage
cmd = r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" http://sikulix.com'

# using an App instance
bc = App(cmd)
print bc
if not bc.isRunning():
  bc.open()
wait(3)
bc.close()

# reusing the App instance but changing open parameters
bc.setUsing("http://sikulix.com/quickstart")
bc.open()
wait(3)
bc.close()
print bc

# using class methods
App.open(cmd)
wait(3)
# this works, because a running Chrome has the exe-name chrome.exe
App.close("chrome")
