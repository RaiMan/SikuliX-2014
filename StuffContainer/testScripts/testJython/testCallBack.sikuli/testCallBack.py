class aClass():
    def aMethod(self, msg):
        print "oClass.aMethod got:", msg

    def aHandler(self, e):
        print "oClass.aHandler got:", e

    def aObserve(self):
        onAppear("1424873087752.png", self.aHandler)
        observe()

def handler(e):
        print "handler got:", e

Debug.setLogger(aClass())
Debug.setLoggerAll("aMethod")

onAppear("1424873087752.png", handler)
observe()

onAppear("1424873087752.png", aClass().aHandler)
observe()

aClass.aObserve()
