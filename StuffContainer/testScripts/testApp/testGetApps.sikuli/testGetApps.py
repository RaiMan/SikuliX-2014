print "*********************** starting testGetApps"

start = time.time()
App.getApps("cmd.exe")
print "*********************** one app: ", time.time() - start

start = time.time()
App.getApps()
print "*********************** all apps: ", time.time() - start
