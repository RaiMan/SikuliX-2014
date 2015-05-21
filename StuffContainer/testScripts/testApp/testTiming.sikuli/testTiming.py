print "******************* starting"
print "******************* App()"
bcPath = r'"C:\Program Files\Microsoft Games\Minesweeper\MineSweeper.exe"'
#bcPath = r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"'
start = time.time()
bc = App(bcPath)
while not bc.isRunning():
    wait(0.5)
print "App():", time.time() - start
wait(1); click(); wait(1)
print "******************* app.focus()"
start = time.time()
bc.focus()
print "App.focus:", time.time() - start
wait(1); click(); wait(1)
start = time.time()
print "******************* app.focus()"
bc.focus()
print "App.focus:", time.time() - start
wait(2)
print "******************* app.close()"
#bc.close()
#App.focus("Google Chrome")
wait(2)
type(Key.F4, Key.ALT)
