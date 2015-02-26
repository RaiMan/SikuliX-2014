img = "1424955061648.png"
dir = getBundlePath()
print "BundlePath:", dir
dir = getBundleFolder()
print "BundleFolder:", dir
dir = getParentPath()
print "ParentPath:", dir
dir = getParentFolder()
print "ParentFolder:", dir
hover(img)
if setBundlePath("../StuffContainer/testScripts/Some Folder WithBlanks"):
  print "WithBlanks", getBundlePath()
else:
  print "setBundlePath: did not work"
find(img).highlight(2)
