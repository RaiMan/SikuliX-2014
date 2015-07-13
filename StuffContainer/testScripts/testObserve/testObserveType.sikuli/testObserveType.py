def handler(e):
  print str(e.type) == "APPEAR"

onAppear("icon.png", handler)
observe()