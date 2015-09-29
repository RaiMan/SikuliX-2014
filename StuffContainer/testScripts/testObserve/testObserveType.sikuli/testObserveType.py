def handler(e):
  print "event type is", e.getType()
  if e.isAppear():
    print "isAppear() returns: True"

onAppear("icon.png", handler)
observe()