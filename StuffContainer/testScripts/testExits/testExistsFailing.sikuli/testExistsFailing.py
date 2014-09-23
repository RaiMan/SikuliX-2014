# some base image to start with
base = find("1411456264567.png")

#create a small image
m = base.getCenter().grow(50)
small = Pattern(capture(m)).exact()

# create a medium image
m1 = grow(m.getTopLeft(), 0, 0, 570, 500)
medium = Pattern(capture(m1)).exact()

#create the large image
m2 = Region(SCREEN)
large = Pattern(capture(m2)).exact()

# hide the stuff
if Settings.isWindows():
  switchApp("SourceTree")
else:
  switchApp("Finder")
wait(1)

# try to find
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
  