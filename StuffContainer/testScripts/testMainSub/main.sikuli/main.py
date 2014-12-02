globalVars = {}
globalVars["var1"] = "whatever"

import sub
reload(sub)
sub.init(globalVars)
sub.someFunction()
globalVars["var1"] = "somethingelse"
sub.someFunction()

