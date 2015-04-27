#import sys
#import org.sikuli.script.SikulixForJython
#print "*****1***** symodules.com =", sys.modules.has_key("com")
#from sikuli import *
load("testLoadCom.jar")
import com.testcom.BaseCom as BC
BC.run()

exit()
Debug.on(3)
load("mongodb.jar")
show()
#print "*****1***** symodules.com =", sys.modules.has_key("com")
#import com.mongodb.MongoClient as MC 
#m = MC("10.0.3.2")
from com.mongodb import *