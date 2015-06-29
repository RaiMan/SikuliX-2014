subGlobal = "sub-global"
#1/0

def init(vars):
    global globalVars # needed to make globalVars a global variable in the namespace sub
    globalVars = vars
    #1/0

def someFunction():
    print subGlobal
    print "from sub:", globalVars["var1"]