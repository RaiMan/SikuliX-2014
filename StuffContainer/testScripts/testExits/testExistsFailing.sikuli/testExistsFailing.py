base = find("1411456264567.png")
m = base.getCenter().grow(50)
small = Pattern(capture(m)).exact()
l1 = m.getCenter().offset(285, 250)
m1 = l1.grow(570, 500)
medium = Pattern(capture(m1)).exact()
m2 = Region(SCREEN)
large = Pattern(capture(m2)).exact()
switchApp("Finder")
wait(1)

for i in range(2):
  exists(small, 0)
  print "small:", getLastTime()
print m
for i in range(2):
  exists(medium, 0)
  print "medium:", getLastTime()
print m1
for i in range(2):
  exists(large, 0)
  print "large:", getLastTime()
print m2
  