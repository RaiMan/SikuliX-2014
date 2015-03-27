def handler(e):
  print("in handler")
  exit()
  
Mouse.setMouseMovedCallback(handler)

for i in range(10):
  hover()
  wait(0.5)
  hover(getTopLeft())
  wait(0.5)