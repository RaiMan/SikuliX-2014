# check wether a variable contains unicode text
def isUnicode(var):
  if isinstance(var, unicode):
    return True
  elif isinstance(var, str):
    return False
  else:
    raise Exception("isUnicode: not unicode nor string: %s" % var)

# check wether a variable contains is list
def isList(var):
  if isinstance(var, list):
    return True
  else:
    return False

# a list of unicode strings
uList = [u"xyz", u"abc"]
print "uList is", uList
print "uList is a list", isList(uList)

# a list of plain ascii strings
sList = ["xyz", "abc"]
print "sList is", sList
print "sList is a list", isList(sList)

# a unicode string variable
uVar = uList[0]
print "uList[0] =", uVar
print "uVar is a list", isList(uVar)

# a plain ascii variable
sVar = sList[0]
print "sList[0]", sVar
print "sVar is a list", isList(sVar)

# a plain ascii variable from unicode string
# crashes if string not only contains ascii characters (x00 .. xFF)
sVar = str(uList[0])
print "str(uList[0])", sVar

# SikuliX feature, to print a mix of unicode and ascii variables
# needed, when running with Jython 2.5
uprint("use of uprint", uVar, sVar)

# check type and convert 
# str(someUnicode) will crash if non-ascii chars present
print "isUnicode(uVar)", isUnicode(uVar)
print "isUnicode(str(uVar))", isUnicode(str(uVar))
print "isUnicode(sVar)", isUnicode(sVar)
print "isUnicode(unicode(sVar))", isUnicode(unicode(sVar))
try:
  print "isUnicode(uList)", isUnicode(uList)
except:
  print "not unicode nor string:\n%s" % uList
  
# example with real unicode characters
uVar = "漢字"
# this crashes with Jython 2.5 (use uprint)
print "real unicode:", uVar
