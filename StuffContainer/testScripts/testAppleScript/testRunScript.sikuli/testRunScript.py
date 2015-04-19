# version to run a multiline AppleScript script
cmd = """
applescript
tell application "Mail" to activate
display alert "Mail should have started" 
"""
if 0 != runScript(cmd):
  popup("did not work\nsee error message")
  exit(1)

wait(3); App.close("Mail"); wait(3)

# version to run a one-line AppleScript script
if 0 != runScript('applescript tell application "Mail" to activate'):
  popup("did not work\nsee error message")

# to get the textual output of the command
textResult = RunTime.get().getLastCommandResult()
