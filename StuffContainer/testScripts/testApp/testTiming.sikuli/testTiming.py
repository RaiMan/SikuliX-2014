Debug.off()
print "******************* starting"
print "******************* App()"
bcPath0 = r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"'
bcPath1 = bcPath0 + ' http://sikulix.com'

print "******************* app.isRunning(wait)"
bcPathx = r'"C:\Program Files (x86)\Google\Chrome\Application\chrom.exe"'
bc = App.open(bcPathx)
print bc
if not bc.isRunning(5):
  print "not running"

def test():
  wait(1); click(); wait(1)
  print "******************* app.focus()"
  start = time.time()
  bc.focus()
  print "App.focus:", time.time() - start, bc
  wait(2)
  print "******************* app.close()"
  #bc.close()
  #App.focus("Google Chrome")
  wait(2)
  type(Key.F4, Key.ALT)

print "******************* app.open(with param)"
start = time.time()
bc = App.open(bcPath1)
print "App():", time.time() - start, bc
ALL.wait("sikulix.png", 10)
test()
wait(3)

App.open(bcPath0); wait(3)
print "******************* app.open()"
start = time.time()
bc = App.open(bcPath0)
print "App():", time.time() - start, bc
wait(1)
test()
wait(2)
App("chrome.exe").focus()
wait(1)
type(Key.F4, Key.ALT)
