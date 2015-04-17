print "*** contents of sys.argv"
for e in sys.argv:
   print e
print "*** contents of SikuliX args"
for e in RunTime.get().getSikuliArgs():
   print e
