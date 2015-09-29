reg = selectRegion()
base = capture(reg)
f = Finder(base)
#target = Pattern("target.png")
target = "target.png"
f.findAll(target)
if f.hasNext():
  while f.hasNext():
    m = f.next()
    hover(reg.getTopLeft().offset(m.getCenter()))
