# make sure the app is running
mail = App("Mail")
mail.focus()
until = time.time() + 10
while not mail.isRunning(): 
  if time.time() > until: break
  wait(1)
if not mail.isRunning(): 
  print "Mail did not start"
  exit(1)

# resize/move window using applescript
cmd = """
applescript
tell application "Mail" to set the bounds of the first window to {0, 23, 800, 600}
"""
runScript(cmd)
