def init(vars):
    global globalVars # needed to make globalVars a global variable in the namespace sub
    globalVars = vars

def someFunction():
    print "from sub:", globalVars["var1"]