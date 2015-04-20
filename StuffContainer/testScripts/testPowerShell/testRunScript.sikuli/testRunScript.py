# version to run a multiline Powershell script
cmd = """
powershell
get-process
"""
if 0 != runScript(cmd):
  popup("did not work\nsee error message")
  exit(1)

# version to run a one-line PowerShell script
if 0 != runScript('powershell get-process'):
  popup("did not work\nsee error message")

# to get the textual output of the command
textResult = RunTime.get().getLastCommandResult()
print textResult
