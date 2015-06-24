imgBase = "imgBase.png"
imgTarget = "imgTarget.png"

print "*** screen find"
m = find(imgBase)

print "*** Finder same"
f = Finder(imgBase)
print f.find(imgBase)
print f.hasNext()
print "same", f.next()

print "*** Finder contained"
f.find(imgTarget)
print f.hasNext()
print "contained", f.next()
