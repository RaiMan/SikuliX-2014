Debug.on(3)
Settings.UseImageFinder = False
start = time.time()
find("icon.png")
find("icon.png")
print "TOTAL:", int(1000*(time.time()-start))