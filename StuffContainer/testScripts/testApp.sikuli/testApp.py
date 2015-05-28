stPath = '\"C:\Program Files (x86)\Atlassian\SourceTree\SourceTree.exe\"'
print "************** starting path only"
st = App.open(stPath)
wait(2)
wait(1); click(); wait(1)
App.focus(stPath)
wait(2)
App.close(stPath)

print "************** starting App.open"
st = App.open(stPath)
st.isRunning(3)
wait(1); click(); wait(1)
st.focus()
wait(1); click(); wait(1)
st.focus()
#print st
wait(2)
st.close()

print "************** starting App()"
st = App(stPath)
if not st.isRunning():
  print "not running"
  App.open(stPath)
else:
  st.focus()
#print st
wait(2)
App.focusedWindow().highlight(1)
click(); wait(1)
st.focus()
wait(2)
st.close()
wait(2)
