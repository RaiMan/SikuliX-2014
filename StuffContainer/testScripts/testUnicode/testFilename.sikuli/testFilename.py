data = u"c:\SikuliX\依頼分\sikuli.funcあ.txt"
dir = os.path.dirname(data)
uprint("*** content of " + dir)
for fname in os.listdir(dir):
  uprint(fname)
uprint("*** content of " + data)
ftxt = open(data)
for line in ftxt.readlines():
  uprint(line.strip())