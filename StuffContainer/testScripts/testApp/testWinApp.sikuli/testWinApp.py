# App() should not open not running app
appN = "notepad"

App.close(appN)
wait(2)
App.open(appN)
wait(3)
exit(1)

Debug.off()
app = App(appN)
print "before open", app
app.open()
print "after open", app
wait(3)
