cmd = """
  applescript
  do shell script "open /Users/raimundhocke/Documents/ZEIT2013.dmg"
"""

cmd = """
  applescript
  tell application "Finder" to open ((path to documents folder as text) & "ZEIT2013.dmg") 
"""

print runScript(cmd)

