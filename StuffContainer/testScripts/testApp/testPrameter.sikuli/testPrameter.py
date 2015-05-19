cmd = r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" http://sikulix.com'
App.open(cmd)
wait(3)
App.close("chrome")

exit(1)

bc = App(cmd)
print bc
if not bc.isRunning():
  bc.open()
wait(3)
bc.close()
bc.setUsing("http://sikulix.com/quickstart")
bc.open()
print bc
wait(3)
bc.close()
