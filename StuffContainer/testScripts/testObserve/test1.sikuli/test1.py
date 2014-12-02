def handler(e):
  print "*** in handler"
  e.match.hover()
  e.repeat(2)
  
onAppear("sikuli.png", handler)
observe(FOREVER, True)

for i in range(10):
  find("left.png")
  print "***** left"
  hover()
  find("right.png")
  print "***** right"
  hover()
