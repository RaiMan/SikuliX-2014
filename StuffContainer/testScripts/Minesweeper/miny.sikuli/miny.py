Settings.MoveMouseDelay = 0
lang = "EN"
HID = 0 #hidden
EMP = 10 #empty
ERR = -1 #error
BOO = 9 #mine
N1 = 1
N2 = 2
N3 = 3
N4 = 4
N5 = 5
N6 = 6
N7 = 7
N8 = 8
fields = ["imgHidden.png", "N1.png", "N2.png", "N3.png", "N4.png", "N5.png", None, None, None, None, "N0.png"]

#Debug.highlightOn()

pgmPath = r'"C:\Program Files\Microsoft Games\Minesweeper\MineSweeper.exe"'  
pgm = App(pgmPath)
if pgm.isRunning:
    pgm.close()
    wait(2)
pgm.open()
    
pgmWin = None    
waitTime = 5
while not pgmWin and waitTime > 0:
    pgmWin = None    
    for n in range(10):
        w = pgm.window(n)
        print waitTime, n, w.toJSON()
        if not w:            
            break
        if w.w < 100:
            continue
        pgmWin = w
        break
    waitTime -= 1
    wait(1)
    
timer = "timer.png"
setupOK = False
if pgmWin.exists(timer, 5):
    setupOK = True
    type(Key.F5)
    while pgmWin.x == pgm.window().x:
        wait(1) 
    options = pgm.window()
    wait(1) 
    click(options.getTopLeft().offset(Region(924, 163, 72, 90).asOffset()))
    type(Key.ENTER)
    wait(2)
    pgmWin = pgm.window()
    minefield = pgmWin.exists(Pattern("minefield.png"), 3)
    if not minefield:
        setupOK = False
if not setupOK:
    popup("Minesweeper not found\n" +
          "or not setup as needed\n" +
          "--- exiting")
    print "windowtitle:%s:" % pgm.getWindow(), "window: %s" % pgmWin.toJSON()
    exit(1)

xcount = 9;
ycount = 9;

minefield.x += 2
minefield.y += 2
minefield.w += -3
minefield.h += -3
grid = Region(minefield)
grid.setRaster(xcount, ycount)
print "grid:", grid.toJSON()
print "cell:", grid.getCell(0,0).toJSON()
#grid.getCell(0,0).highlight(2)
#grid.getCell(4,4).highlight(2)
#grid.getCell(8,8).highlight(2)

boxesstates = [[HID]*xcount for i in range(ycount)];

def regionforbox(x,y):
    return grid.getCell(x,y)        

def analyseRegionState(reg):
    for n in range(len(fields)):
        if not fields[n]:
            continue
        if reg.exists(fields[n], 0):
            return n
    return ERR    

Debug.off()
def scanfields():
    found = -1
    for r in range(xcount):
        for c in range(ycount):
            reg = regionforbox(r, c)
            found = analyseRegionState(reg)
            if found < 0:
                reg.highlight(2)
                break
            else:
                reg.hover()
        if found < 0:
            break
#scanfields()

#type(Key.ESC)
#type(Key.F4, Key.ALT)
#Debug.highlightOff(); exit()
Debug.on(3)

def hoverout():
    hover(Location(minefield.x + minefield.w, minefield.y))
    return
    
def regionfromlocation(location, halfsize):
    newreg = location.grow(halfsize)
    return newreg

def isgamelost():
    focusrect = App.focusedWindow()
    focusrect = focusrect.nearby(30)
    paterntosearch = Pattern("langimg\\" + lang + "end.png").similar(0.8)
    if(focusrect.exists(paterntosearch, 0.4)):
        print "BOOM!"
        return True
    return False

def isgamewin():
    focusrect = App.focusedWindow()
    focusrect = focusrect.nearby(30)
    paterntosearch = Pattern("langimg\\" + lang + "win.png").similar(0.8)
    #print "focusrect (x,y,w,h): (" + str(focusrect.x) + ", " + str(focusrect.y) + ", " + str(focusrect.w) + ", " + str(focusrect.h) + ")"
    if(focusrect.exists(paterntosearch, 0.4)):
        print "HOORAY!"
        return True
    return False

def isgameend():
    focusrect = App.focusedWindow()
    if (focusrect.x == pgmWin.x):
        return False
    paterntosearch = Pattern("langimg\\" + lang + "perc.png").similar(0.8)
    if(focusrect.exists(paterntosearch, 0.4)):
        isgamelost()
        isgamewin()
        dragDrop(focusrect.aboveAt(10),focusrect.aboveAt(10).offset(-focusrect.w, 0))
        pgmWin.getScreen().capture(pgmWin).getFile(getParentPath(), "lastGame")
        wait(2)
        type(Key.ESC); type(Key.F4, Key.ALT)
        return True
    return False

def updateboxstate(x,y):
    currentregion = regionforbox(x, y) 
    s = time.time()
    boxesstates[x][y] = analyseRegionState(currentregion)
    print "analyse time:", time.time()-s
    if(boxesstates[x][y] == ERR):
        print "ERR on [", x, ", ", y, "]"
        hover(grid.getCell(x, y))
        retVal = input("Couldn't recognise field in column " + str(x+1) + 
                ", row: " + str(y+1) + 
                "\nPlease tell me what it is.\nPut number:\n" + 
                "0 if the field is still hidden\n" + 
                "1-8 if there is a specific number\n" + 
                "9 if the field is mine\n" + 
                "10 if the field is empty)\n" + 
                "press cancel to terminate", "-1")
        if not retVal:
            if not isgameend():
                type(Key.F4, Key.ALT)
                wait(1)
                type(Key.TAB)
                type(" ")
            exit();
        try:
            boxesstates[x][y] = int(retVal);
        except:
            boxesstates[x][y] = -1;        
    return boxesstates[x][y]

def hasstate(x,y,state):
    if(boxesstates[x][y] == state):
        return True
    return False

def updatestatesfromempty(x,y):
    hoverout()
    hiditems = getaround(x,y,[HID])
    for item in hiditems:
        if(boxesstates[item[0]][item[1]] == HID): #additional state check is necessary because it may be changed by previous items
            if(updateboxstate(item[0],item[1]) == EMP):
                updatestatesfromempty(item[0],item[1])
    return

def clickfield(x,y):
    click(grid.getCell(x,y))
    wait(0.1)
#    if(isgamewin() or isgamelost()):
    boxstate = updateboxstate(x,y)        
    if(isgameend()):
        return -2
    print "Clicked field [" + str(x) + ", " + str(y) + "] and found it in state " + str(boxstate)
    if(boxstate == EMP):
        wait(0.2)
        updatestatesfromempty(i,j)
        #popup(str(boxesstates))
    return boxstate

def rightclickfield(x,y):
    rightClick(grid.getCell(x,y))
    boxesstates[x][y] = BOO
    return

def getaround(x,y,states):
    points = []
    for state in states:
        if(x > 0):
            if(boxesstates[x-1][y] == state):
                points.append((x-1,y))
            if(y > 0):
                if(boxesstates[x-1][y-1] == state):
                    points.append((x-1,y-1))
            if(y < (ycount-1)):
                if(boxesstates[x-1][y+1] == state):
                    points.append((x-1,y+1))
        if(y > 0):
            if(boxesstates[x][y-1] == state):
                points.append((x,y-1))
        if(y < (ycount-1)):
            if(boxesstates[x][y+1] == state):
                points.append((x,y+1))
        if(x < (xcount-1)):
            if(boxesstates[x+1][y] == state):
                points.append((x+1,y))
            if(y > 0):
                if(boxesstates[x+1][y-1] == state):
                    points.append((x+1,y-1))
            if(y < (ycount-1)):
                if(boxesstates[x+1][y+1] == state):
                   points.append((x+1,y+1))
    return points

def countaround(x,y,states):
    return len(getaround(x,y,states))

def markminesaround(x,y):
    hiditems = getaround(x,y,[HID])
    for item in hiditems:
        rightclickfield(item[0],item[1])
    return

def listlength(list):
    len = 0
    for i in list:
        len = len + 1
    return len

freefields = [];

def findmines():
    for x in range(xcount):
        for y in range(ycount):
            state = boxesstates[x][y]
            if(state > 0 and state < 9):
                if(state == countaround(x,y,[HID, BOO])):
                    markminesaround(x,y)
    return

def findfree():
    for x in range(xcount):
        for y in range(ycount):
            state = boxesstates[x][y]
            if(state > 0 and state < 9):
                if(state == countaround(x,y,[BOO])):
                    newitems = getaround(x,y,[HID])
                    for item in newitems:
                        unique = True
                        for olditem in freefields:
                            if(olditem[0] == item[0] and olditem[1] == item[1]):
                                unique = False
                                break
                        if(unique):
                            freefields.append(item)
    return 0

def chooseField():
    point = 0
    findmines()
    if(len(freefields) == 0):
        findfree()
    if(len(freefields) > 0):
        point = freefields.pop()
    if(point == 0):
        point = (random.randint(0,xcount-1), random.randint(0,ycount-1))
        print "trying random field ", point
    else:
        print "using free field ", point
    return point

import random
gameend = False
while(not gameend):
    point = chooseField();
    i = point[0]
    j = point[1]
    if(hasstate(i,j,HID)):
        s = time.time()
        if(clickfield(i,j) == -2):
            gameend = True
        print "click time:", time.time()-s

print "END"


