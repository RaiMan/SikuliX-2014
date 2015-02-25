class aClass():
    def aFunction(self, msg):
        print "oClass.aFunction got:", msg

def handler(e):
        print "handler got:", e
    
Debug.setLogger(aClass())
Debug.setLoggerAll("aFunction")

onAppear("1424873087752.png", handler)
observe()
