print "**************** starting"
bcPath0 = r'"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"'
bcPath1 = bcPath0 + ' http://sikulix.com'
title = "SikuliX powered"

App.open(bcPath1)
print bc
wait(2)

popup("title")
bc = App.focus(title)
print bc
wait(2)

popup("change")
bc.focus()
print bc
wait(2)

popup("change")
bc.focus()
print bc

wait(2)
type(Key.F4, Key.ALT)

