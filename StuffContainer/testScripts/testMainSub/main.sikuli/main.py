globalVars = {}
globalVars["var1"] = "whatever"

import sub
reload(sub)

print "***** sys.path"
for e in sys.path: print e

print "***** ImagePath"
for e in getImagePath(): print e
print "***** "

sub.init(globalVars)
sub.someFunction()
globalVars["var1"] = "somethingelse"
sub.someFunction()

