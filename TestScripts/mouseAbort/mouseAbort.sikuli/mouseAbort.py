runScript(getParentPath())

exit()

logo = "logo.png"
home = Pattern(logo).targetOffset(0,100)
quick = Pattern(logo).targetOffset(100,100)

Mouse.setMouseMovedAction(2)

while True:
  click(home)
  wait(3)
  click(quick)
  wait(3)