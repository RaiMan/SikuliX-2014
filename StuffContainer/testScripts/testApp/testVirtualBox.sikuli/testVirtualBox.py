Debug.off()
vb = App("Win7-64")
if not vb.isRunning():
  print "not running"
  App.open(r'"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm Win7-64')
  while not vb.isRunning():
    wait(1) 
vb.focus()
App.focusedWindow().highlight(2)
