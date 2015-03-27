def handler(e):
  print("in handler")
  Sikulix.terminate(1)
  print("leaving handler")
  
Mouse.setMouseMovedCallback(handler)

for i in range(3):
  hover()
  wait(0.5)
  hover(getTopLeft())
  wait(0.5)