print "***** starting testFinder"
Debug.off()
bs = App("Safari")
bs.focus(); wait(1)
img = capture(selectRegion())

def compare(base, target):
  f = Finder(base)
  f.find(target)
  if f.hasNext():
    print "** found", f.next()
  else:
    print "** notFound"

print "*** should be found (same image)"
compare(img, img)

print "*** should be found (contained image)"
icon = capture(selectRegion())
compare(img, icon)

print "*** should not be found (different image, same size)"
mIcon = find(icon)
icon2 = capture(mIcon.right(100).right(mIcon.w))
compare(icon, icon2)

print "*** should not be found (same image, different size)"
mIcon = find(icon)
icon2 = capture(mIcon.grow(10))
compare(icon, icon2)