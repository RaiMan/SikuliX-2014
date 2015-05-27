Debug.off()
vb = App("Win7-64")
if not vb.isRunning():
  print "not running"
  App.open(r'"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm Win7-64')
  if not vb.isRunning(10):
    exit(1) 
vb.focus()
App.focusedWindow().highlight(2)
wait(2)
