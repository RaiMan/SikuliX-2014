"""
the active .ahk contains
#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
; #Warn  ; Enable warnings to assist with detecting common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
:*:sikulitest::
  MsgBox Coming from SikuliX
  return
"""
type("sikulitest")
ahkmsg = Pattern("ahkmsg.png").targetOffset(34,47)
if exists(ahkmsg):
  click(highlight(-2))
else:
  popup("AHK did not work")