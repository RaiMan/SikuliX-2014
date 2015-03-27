popup("hallo")
class myLogger():
  def callBack(x, msg):
    print "REDIRECTED", msg

Debug.setLogger(myLogger())
Debug.setLoggerAll("callBack")

Debug.log("test")