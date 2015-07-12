def handler(e):
  loc = Mouse.at()
  print "in handler:", loc
  wait(5)
  print "leaving handler", Mouse.at()
  print loc.equals(Mouse.at())
  
Mouse.setMouseMovedCallback(handler)

for i in range(5):
  hover()
  wait(0.5)
  hover(getTopLeft())
  wait(0.5)