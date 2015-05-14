n = App("notepad")
nw = App("Untitled")
npp = App(r"C:\Program Files (x86)\Notepad++\notepad++.exe")
nppw = App("new")
def doit(app):
  if not app.isRunning():
    app.open()
  print app
  wait(3)
  app.focus()
  type("xxx")
  wait(3)

doit(n)
doit(nw)
n.close()
doit(npp)
doit(nppw)
npp.close()