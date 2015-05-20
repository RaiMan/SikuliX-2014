print "******************* starting"
start = time.time()
print "******************* App()"
bc = App(r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"')
lap = time.time(); print "App():", lap - start; start = lap
wait(1); click(); wait(1)
start = time.time()
print "******************* app.focus()"
bc.focus()
lap = time.time(); print "App.focus:", lap - start; start = lap
wait(1); click(); wait(1)
start = time.time()
print "******************* app.focus()"
bc.focus()
lap = time.time(); print "App.focus:", lap - start; start = lap
wait(2)
print "******************* app.close()"
#bc.close()
#App.focus("Google Chrome")
wait(2)
type(Key.F4, Key.ALT)