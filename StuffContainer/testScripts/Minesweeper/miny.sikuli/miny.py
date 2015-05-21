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

#Debug.highlightOn()

pgmPath = r'"C:\Program Files\Microsoft Games\Minesweeper\MineSweeper.exe"'  
pgm = App(pgmPath);
if not pgm.isRunning():
    pgm.open()
    
pgmWin = None
while not pgmWin:
    pgmWin = pgm.window()
    wait(1)

timer = "timer.png"
setupOK = False
if pgmWin.exists(timer):
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

boxsize = minefield.w /xcount
halfabox = boxsize / 2
boxeslocations = [[0]*xcount for i in range(ycount)];
for x in range(xcount):
    for y in range(ycount):
        boxeslocations[x][y] = Location(x*boxsize + halfabox + minefield.x,y*boxsize + halfabox + minefield.y)

boxesstates = [[HID]*xcount for i in range(ycount)];

def regionforbox(x,y):
    return grid.getCell(x,y)        

def regionishidden(r):
    if r.exists("imgHidden.png",0): #or r.exists(HID2,0):
        #print r.x, r.y, r.getLastMatch().getScore()
        return True
    return False

found = True
for r in range(xcount):
    for c in range(ycount):
        reg = regionforbox(r, c)
        found = True
        if regionishidden(reg):
            reg.hover()
        else:
            reg.highlight(2)
            found = False
            break
    if not found:
        break
#type(Key.ESC)
#type(Key.F4, Key.ALT)
#Debug.highlightOff(); exit()

def hoverout():
    hover(Location(minefield.x + minefield.w, minefield.y))
    return

from java.awt import Color
from java.awt import Robot

def iscolorsimilar(location, color, rr, rg, rb):
    pixel = Robot().getPixelColor(location.x, location.y)
    pr = pixel.getRed();
    cr = color.getRed();
    print "pixel color: ", pixel
    if(pr > cr - rr and pr < cr + rr):
        pg = pixel.getGreen();
        cg = color.getGreen();
        if(pg > cg - rg and pg < cg + rg):
            pb = pixel.getBlue();
            cb = color.getBlue();
            if(pb > cb - rb and pb < cb + rb):
                return True
    return False

def issomecolorsimilar(region, color, rr, rg, rb, centerOffset=2):
    print "search for pixel similar to [", color.getRed(), ", ", color.getGreen(), ", ", color.getBlue(), "] in range of +-", rr, ", ", rg, ", ", rb
    center = region.getCenter() 
    if(iscolorsimilar(center, color, rr, rg, rb)):
        return True
    reg = regionfromlocation(center, centerOffset)
    if(iscolorsimilar(reg.getTopLeft(), color, rr, rg, rb)):
        return True
    if(iscolorsimilar(reg.getTopRight(), color, rr, rg, rb)):
        return True
    if(iscolorsimilar(reg.getBottomLeft(), color, rr, rg, rb)):
        return True
    if(iscolorsimilar(reg.getBottomRight(), color, rr, rg, rb)):
        return True
    return False

def areallcolorssimilar(region, color, rr, rg, rb):
    print "ensure all pixels are similar to [", color.getRed(), ", ", color.getGreen(), ", ", color.getBlue(), "] in range of +-", rr, ", ", rg, ", ", rb
    center = region.getCenter() 
    if(not iscolorsimilar(center, color, rr, rg, rb)):
        return False
    for i in range(2,4):
        reg = regionfromlocation(center, i)
        if(not iscolorsimilar(reg.getTopLeft(), color, rr, rg, rb)):
            return False
        if(not iscolorsimilar(reg.getTopRight(), color, rr, rg, rb)):
            return False
        if(not iscolorsimilar(reg.getBottomLeft(), color, rr, rg, rb)):
            return False
        if(not iscolorsimilar(reg.getBottomRight(), color, rr, rg, rb)):
            return False
    return True


def regionis1(region):
    if( issomecolorsimilar(region, Color( 62, 80 , 190), 5, 5, 5)):
        print "N1"
        return True
    return False

def regionis2(region):
    if( issomecolorsimilar(region, Color( 29, 104 , 1), 5, 5, 5, 3)):
        print "N2"
        return True
    if( issomecolorsimilar(region, Color( 29, 104 , 1), 5, 5, 5, 2)):
        print "N2"
        return True
    return False

def regionis3(region):
    if( issomecolorsimilar(region, Color( 170, 9 , 8), 5, 5, 5, 1)):
        print "N3a"
        return True
    if( issomecolorsimilar(region, Color( 170, 9 , 8), 5, 5, 5, 3)):
        print "N3b"
        return True
    return False

def regionis4(region):
    if(region.exists(Pattern("N4a.png").similar(0.80),0)):
        print "N41"
        return True
    return False

def regionis5(region):
    if(region.exists(Pattern("N5a.png").similar(0.76),0)):
        print "N51"
        return True
    return False

def regionis6(region):
    if( issomecolorsimilar(region, Color( 44, 141 , 146), 5, 5, 5)):
        print "N6"
        return True
    return False

def regionisempty(region):
    #if(region.exists(Pattern("emp.png").similar(0.90),0)):
        #print "EMP1"
        #return True
    if( areallcolorssimilar(region, Color( 199, 207 , 228), 29, 28, 20)):
        print "EMP"
        return True
    return False

def analyseRegionState(region):
    if(regionisempty(region)):
        return EMP
    if(regionis1(region)):
        return N1
    if(regionis2(region)):
        return N2
    if(regionis3(region)):
        return N3
    if(regionis4(region)):
        return N4
    if(regionis5(region)):
        return N5
    if(regionis6(region)):
        return N6
    if(regionishidden(region)):
        return HID
    return ERR
    
    
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
    focusrect = focusrect.nearby(30)
    paterntosearch = Pattern("langimg\\" + lang + "perc.png").similar(0.8)
    if(focusrect.exists(paterntosearch, 0.4)):
        isgamelost()
        isgamewin()
        return True
    return False

def updateboxstate(x,y):
    currentregion = regionforbox(x, y) 
    s = time.time()
    boxesstates[x][y] = analyseRegionState(currentregion)
    print "analyse time:", time.time()-s
    if(boxesstates[x][y] == ERR):
        print "ERR on [", x, ", ", y, "]"
        hover(boxeslocations[x][y])
        boxesstates[x][y] = input("Couldn't recognise field in column " + str(x+1) + ", row: " + str(y+1) + "\nPlease tell me what it is.\nPut number:\n0 if the field is still hidden\n1-8 if there is a specific number\n9 if the field is mine\n10 if the field is empty)", "-1")
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
    click(boxeslocations[x][y])
    wait(0.1)
#    if(isgamewin() or isgamelost()):
    if(isgameend()):
        return -2
    boxstate = updateboxstate(x,y)        
    print "Clicked field [" + str(x) + ", " + str(y) + "] and found it in state " + str(boxstate)
    if(boxstate == EMP):
        wait(0.2)
        updatestatesfromempty(i,j)
        #popup(str(boxesstates))
    return boxstate

def rightclickfield(x,y):
    rightClick(boxeslocations[x][y])
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
    #hoverout()

print "END"


