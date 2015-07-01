def handler(e):
  print("in handler")
  #Device.setShouldTerminate()
  print("leaving handler")
  
Mouse.setMouseMovedCallback(handler)

for i in range(3):
  hover()
  wait(0.5)
  hover(getTopLeft())
  wait(0.5)