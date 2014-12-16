gc = App("Google Chrome")
gc.focus()
while not gc.window():
  wait(1)
type("l", Key.CMD)
type("sikulix.com")
type(Key.ENTER)
click("NightlyBuild.png")
wait(3)
while gc.window():
  type("w", Key.CMD)
  wait(0.5)
type("q", Key.CMD)
