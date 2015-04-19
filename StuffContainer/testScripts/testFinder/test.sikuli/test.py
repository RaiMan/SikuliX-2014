stars = "stars.png"
star = "star.png"
f = Finder(stars)
f.findAll(star)
while f.hasNext():
  print f.next()

exit()

reg = selectRegion()
matches = list(reg.findAll(star))
for m in matches: print m