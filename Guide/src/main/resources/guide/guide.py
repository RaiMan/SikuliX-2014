from sikuli.Sikuli import *

from org.sikuli.script import ScreenUnion
from org.sikuli.script import Pattern
from org.sikuli.script import Region
from org.sikuli.basics import Debug

from org.sikuli.guide import SikuliGuide

from org.sikuli.guide import SikuliGuideComponent
from org.sikuli.guide.SikuliGuideComponent import Layout

from org.sikuli.guide import SikuliGuideAnchor
from org.sikuli.guide import SikuliGuideArea
from org.sikuli.guide import SikuliGuideArrow
from org.sikuli.guide import SikuliGuideBracket
from org.sikuli.guide import SikuliGuideButton
from org.sikuli.guide import SikuliGuideCallout
from org.sikuli.guide import SikuliGuideCircle
from org.sikuli.guide import SikuliGuideClickable
from org.sikuli.guide import SikuliGuideFlag
from org.sikuli.guide import SikuliGuideHotspot
from org.sikuli.guide import SikuliGuideImage
from org.sikuli.guide import SikuliGuideRectangle
from org.sikuli.guide import SikuliGuideSpotlight
from org.sikuli.guide import SikuliGuideText

"""
RaiMan: currently not used
from org.sikuli.guide import SikuliGuideMagnet
from org.sikuli.guide import Portal
from org.sikuli.guide import TransitionDialog
from org.sikuli.guide import TreeSearchDialog
from org.sikuli.guide.model import GUIModel
from org.sikuli.guide.model import GUINode
"""

#s = ScreenUnion()
_g = SikuliGuide()

#TODO special Steffen
specialSteffen = False
#END TODO special Steffen


#######################
#      Core API       #
#######################

#================
# Area Components
#================

def circle(target, **kwargs):
#    comp = SikuliGuideCircle()
    comp = _g.circle()
#TODO special Steffen
    col = kwargs.pop("back", None)
    if col:
        comp.setColor(col[0], col[1], col[2])
    else:
        if specialSteffen: comp.setColor(255, 145, 0)
#END TODO special Steffen
    return _addComponentHelper(comp, target, side = 'over', **kwargs)

def rectangle(target, **kwargs):
    comp = SikuliGuideRectangle(None)
    return _addComponentHelper(comp, target, side = 'over',  **kwargs)

def spotlight(target, shape = 'circle', **kwargs):
    comp = SikuliGuideSpotlight(None)
    if shape == 'rectangle':
        comp.setShape(SikuliGuideSpotlight.RECTANGLE)
    elif shape == 'circle':
        comp.setShape(SikuliGuideSpotlight.CIRCLE)
    return _addComponentHelper(comp, target, side = 'over', **kwargs)

def arrow(srcTarget, destTarget, **kwargs):
    def getComponentFromTarget(target):
        if isinstance(target, str) or isinstance(target, Pattern):
            return anchor(target)
        elif isinstance(target, SikuliGuideComponent):
            return target
    comp1 = getComponentFromTarget(srcTarget)
    comp2 = getComponentFromTarget(destTarget)
    comp = SikuliGuideArrow(comp1, comp2)
#TODO special Steffen
    col = kwargs.pop("front", None)
    if col:
        comp.setColor(col[0], col[1], col[2])
    else:
        if specialSteffen: comp.setColor(255, 145, 0)
#END TODO special Steffen
    _g.addToFront(comp)
    return comp

#=====================
# Positioning Elements
#======================

def anchor(target):
    if isinstance(target, Pattern):
        pattern = target
    elif isinstance(target, str):
        pattern = Pattern(target)
    comp = SikuliGuideAnchor(pattern)
    _g.addToFront(comp)
    return comp

def area(targets):
    patterns = [Pattern(target) for target in targets]
    comp = SikuliGuideArea()
    for pattern in patterns:
        anchor = SikuliGuideAnchor(pattern)
        _g.addToFront(anchor)
        comp.addLandmark(anchor)
    _g.addToFront(comp)
    return comp

#================
# Text Elements
#================

def bracket(target, side='left', **kwargs):
    comp = SikuliGuideBracket()
    return _addComponentHelper(comp, target, side = side, **kwargs)

def flag(target, t='    ', **kwargs):
    comp = SikuliGuideFlag(t)
    s = kwargs.pop("side", 'left')
    return _addComponentHelper(comp, target, side = s, **kwargs)

def text(target, txt, **kwargs):
    comp = SikuliGuideText(txt)
    s = kwargs.pop("side", 'bottom')
    f = kwargs.pop("fontsize", 16)
    return _addComponentHelper(comp, target, side = s, fontsize = f, **kwargs)

def callout(target, txt, **kwargs):
    comp = SikuliGuideCallout( txt)
    s = kwargs.pop("side", 'right')
    f = kwargs.pop("fontsize", 16)
    return _addComponentHelper(comp, target, side = s, fontsize = f, **kwargs)

def tooltip(target, txt,**kwargs ):
    return text(target, txt, fontsize = 8, **kwargs)

def image(target, imgurl, **kwargs):
    comp = SikuliGuideImage(imgurl)
    return _addComponentHelper(comp, target, **kwargs)

#TODO special Steffen
def textcolored(target, txt, front=(255,255,255), back=(242,145,0), **kwargs):
    return text(target, txt, front=(255,255,255), back=(242,145,0), **kwargs)

def calloutcolored(target, txt, front=(255,255,255), back=(242,145,0), **kwargs):
    return callout(target, txt, front=(255,255,255), back=(242,145,0), **kwargs)
#END TODO special Steffen

#=====================
# Interactive Elements
#=====================

def clickable(target, name = "", **kwargs):
    comp = SikuliGuideClickable(None)
    comp.setName(name)
    return _addComponentHelper(comp, target, side = 'over', **kwargs)

def button(target, name, side = 'bottom', **kwargs):
    comp = SikuliGuideButton(name)
    return _addComponentHelper(comp, target, side = side, **kwargs)

def addCloseButton(a):
    button(a,"Close", side="left", offset = (200,0))
def addNextButton(a):
    button(a,"Next",side="left", offset = (60,0))
def addPreviousButton(a):
    button(a,"Previous", side ="left", offset = (0,0))

def hotspot(target, message, side = 'right'):
    # TODO allow hotspot's positions to be automatically updated
    r = _getRegionFromTarget(target)
    txtcomp = SikuliGuideCallout(message)
    r1 = Region(r)
    r1.x -= 10
    r1.w += 20
    _setLocationRelativeToRegion(txtcomp,r1,side)
    txtcomp.setShadow(10,2)
    comp = SikuliGuideHotspot(r, txtcomp, _g)
    _g.addToFront(comp)
    return comp

#=====================
# Show the Elements
#=====================

def show(arg = None, timeout = 5):
    # show a list of steps
    if isinstance(arg, list) or isinstance(arg, tuple):
        _show_steps(arg, timeout)
    # show a single step
    elif callable(arg):
        arg()
        return _g.showNow(timeout)
    # show for some period of time
    elif isinstance(arg, float) or isinstance(arg, int):
        return _g.showNow(arg)
    # show
    else:
        return _g.showNow()

def setDefaultTimeout(timeout):
    _g.setDefaultTimeout(timeout)

# showing steps, that are defined in a list of functions
def _show_steps(steps, timeout = None):
    # only keep callables
    steps = filter(lambda x: callable(x), steps)
    print steps
    n = len(steps)
    i = 0
    while True:
        step = steps[i]
        step()
        msg = "Step %d of %d" % (i+1, n)
        a = rectangle(Region(100,100,0,0))
        text((10,50), msg, fontsize = 10)
        if n == 1: # only one step
            addCloseButton(a)
        elif i == 0: # first step
            addNextButton(a)
            addCloseButton(a)
        elif i < n - 1: # between
            addPreviousButton(a)
            addNextButton(a)
            addCloseButton(a)
        elif i == n - 1: # final step
            addPreviousButton(a)
            addCloseButton(a)
        ret = _g.showNow()
        if (ret == "Previous" and i > 0):
            i = i - 1
        elif (ret == "Next" and i < n - 1):
            i = i + 1
        elif (ret == None and i < n - 1): # timeout
            i = i + 1
        elif (ret == "Close"):
            return
        else:
            # some other transitions happened
            if (i < n - 1):
                i = i + 1
            else:
                return

#########################
# Cursor Enhancement    #
#########################

def beam(target):
    r = s.getRegionFromTarget(target)
    c = _g.addBeam(r)
    return c

def magnet(arg):
    m = Magnet(_g)
    def addTarget(x):
        if (isinstance(x, Pattern)):
            pattern = x
        elif (isinstance(x, str)):
            pattern = Pattern(x)
        m.addTarget(pattern)
    if isinstance(arg, list) or isinstance(arg, tuple):
        for x in arg:
            addTarget(x)
    else:
        addTarget(x)
    _g.addTransition(m)

####################
# Helper functions #
####################

def _addComponentHelper(comp, target, side = 'best', margin = 0, offset = (0,0), 
                        horizontalalignment = 'center', verticalalignment = 'center', 
                        font = None, fontsize = 0, width = 0,
                        shadow = 'default', front = None, back = None, frame = None, text = None):

    # set the component's colors
    comp.setColors(front, back, frame, text)
    
    # set the component's font
    comp.setFont(font, fontsize)
    
    # set the components width
    if width > 0: comp.setMaxWidth(width)
    
    # Margin
    if margin:
        if isinstance(margin, tuple):
            (dt,dl,db,dr) = margin
        else:
            (dt,dl,db,dr) = (margin,margin,margin,margin)
        comp.setMargin(dt,dl,db,dr)

    # Offset
    if offset:
        (x,y) = offset
        comp.setOffset(x,y)

    # Side
    if (side == 'right'):
        sideConstant = Layout.RIGHT
    elif (side == 'top'):
        sideConstant = Layout.TOP
    elif (side == 'bottom'):
        sideConstant = Layout.BOTTOM
    elif (side == 'left'):
        sideConstant = Layout.LEFT
    elif (side == 'inside'):
        sideConstant = Layout.INSIDE
    elif (side == 'over'):
        sideConstant = Layout.OVER

    # Alignment
#    if (horizontalalignment == 'left'):
#        comp.setHorizontalAlignmentWithRegion(r,0.0)
#    elif (horizontalalignment == 'right'):
#   if (verticalalignment == 'top'):
#       comp.setVerticalAlignmentWithRegion(r,0.0)
#   elif (verticalalignment == 'bottom'):
#        comp.setVerticalAlignmentWithRegion(r,1.0)

    # target and position
    comp.setTarget(target)
    comp.setLayout(sideConstant)

#    if isinstance(target, Region):
#        # absolute location wrt a Region
#        comp.setLocationRelativeToRegion(target, sideConstant)
#    elif isinstance(target, tuple):
#        # absolute location wrt a point (specified as (x,y))
#        comp.setLocationRelativeToRegion(Region(target[0], target[1],1,1), Layout.RIGHT)
#    else:
#        targetComponent = None
#        if isinstance(target, str):
#            # relative location to a string (image filename)
#            targetComponent = anchor(Pattern(target))
#            targetComponent.setOpacity(0)
#        elif isinstance(target, Pattern):
#            # relative location to a pattern
#            targetComponent = anchor(target)
#            targetComponent.setOpacity(0)
#        elif isinstance(target, SikuliGuideComponent):
#            targetComponent = target
#        if targetComponent:
#            comp.setLocationRelativeToComponent(targetComponent, sideConstant)
#        else:
#            Debug.error("GuideComponentSetup: invalid target: ", target)
#            return None

    # set shadow, different sizes for different types of components
#TODO shadow handling
    if shadow == 'default':
        if (isinstance(comp, SikuliGuideCircle) or \
                isinstance(comp, SikuliGuideRectangle) or \
                isinstance(comp, SikuliGuideBracket)):
            comp.setShadow(5,2)
        elif not (isinstance(comp, SikuliGuideSpotlight)):
            comp.setShadow(10,2)

    # add the component to guide
    comp.updateComponent()
#    _g.addToFront(comp)
    return comp


def _setLocationRelativeToRegion(comp, r_, side='left', offset=(0,0), expand=(0,0,0,0), \
                                 horizontalalignment = 'center', \
                                 verticalalignment = 'center'):
    r = Region(r_)
    # Offset
    (dx,dy) = offset
    r.x += dx
    r.y += dy
    # Side
    if (side == 'right'):
        comp.setLocationRelativeToRegion(r, Layout.RIGHT);
    elif (side == 'top'):
        comp.setLocationRelativeToRegion(r, Layout.TOP);
    elif (side == 'bottom'):
        comp.setLocationRelativeToRegion(r, Layout.BOTTOM);
    elif (side == 'left'):
        comp.setLocationRelativeToRegion(r, Layout.LEFT);
    elif (side == 'inside'):
        comp.setLocationRelativeToRegion(r, Layout.INSIDE);
    # Alignment
    if (horizontalalignment == 'left'):
        comp.setHorizontalAlignmentWithRegion(r,0.0)
    elif (horizontalalignment == 'right'):
        comp.setHorizontalAlignmentWithRegion(r,1.0)
    if (verticalalignment == 'top'):
        comp.setVerticalAlignmentWithRegion(r,0.0)
    elif (verticalalignment == 'bottom'):
        comp.setVerticalAlignmentWithRegion(r,1.0)

def _getRegionFromTarget(target):
    if isinstance(target, SikuliGuideComponent):
        return Region(target.getBounds())
    else:
        return s.getRegionFromTarget(target)

"""
# RaiMan currently not used
#
def _addSideComponentToTarget(comp, target, **kwargs):
    r = _getRegionFromTarget(target)
    _setLocationRelativeToRegion(comp,r,**kwargs)
    if isinstance(target, str):
        _g.addTracker(Pattern(target),r,comp)
    elif isinstance(target, Pattern):
        _g.addTracker(target,r,comp)
    elif isinstance(target, SikuliGuideComponent):
        target.addFollower(comp)
    _g.addComponent(comp)
    return comp

def _addAraeComponentToTarget(comp_func, target, **kwargs):
    r = _getRegionFromTarget(target)
    r1 = _adjustRegion(r, **kwargs)
    comp = comp_func(r1)
    if isinstance(target, str):
        _g.addTracker(Pattern(target),r1,comp)
    elif isinstance(target, Pattern):
        _g.addTracker(target,r1,comp)
    elif isinstance(target, SikuliGuideComponent):
        target.addFollower(comp)
    _g.addComponent(comp)
    return comp

def _adjustRegion(r_, offset = (0,0), expand=(0,0,0,0))
    r = Region(r_)
    # Offset
    (dx,dy) = offset
    r.x += dx
    r.y += dy
    # Expansion
    if isinstance(expand, tuple):
        (dt,dl,db,dr) = expand
    else:
        (dt,dl,db,dr) = (expand,expand,expand,expand)
    r.x -= dl
    r.y -= dt
    r.w = r.w + dl + dr
    r.h = r.h + dt + db
    return r

# RaiMan: seems not to be used anymore
#
# functions for showing
def _show_steps_old(steps, timeout = None):

    # only keep callables
    steps = filter(lambda x: callable(x), steps)
    print steps
    n = len(steps)
    i = 0

    while True:
        step = steps[i]
        step()

        d = TransitionDialog()

        text = "Step %d of %d" % (i+1, n)
        d.setText(text)

        if n == 1: # only one step
            d.addButton("Close")
        elif i == 0: # first step
            d.addButton("Next")
            d.addButton("Close")
        elif i < n - 1: # between
            d.addButton("Previous")
            d.addButton("Next")
            d.addButton("Close")
        elif i == n - 1: # final step
            d.addButton("Previous")
            d.addButton("Close")

        d.setLocationToUserPreferredLocation()
        if timeout:
            d.setTimeout(timeout*1000)

        _g.setTransition(d)
        ret = _g.showNow()

        if (ret == "Previous" and i > 0):
            i = i - 1
        elif (ret == "Next" and i < n - 1):
            i = i + 1
        elif (ret == None and i < n - 1): # timeout
            i = i + 1
        elif (ret == "Close"):
            return
        else:
            return

RaiMan: Temporarily switched off
#########################
# Experimental Features #
#########################

def portal(targets):
    p = Portal(_g)
    for target in targets:
        r = s.getRegionFromTarget(target)
        p.addEntry("",r)
    _g.addSingleton(p)

def magnifier(target):
    r = s.getRegionFromTarget(target)
    _g.addMagnifier(r)

def parse_model(gui, level=0):
    for i in range(0,level):
        print "----",
    n = gui[0]
    ps,name = n
    node_n = GUINode(Pattern(ps).similar(0.75))
    node_n.setName(name)
    print node_n
    children = gui[1:]
    for c in children:
        node_c = parse_model(c, level+1)
        node_n.add(node_c)
    return node_n


def do_search(guidefs, guide):
    root = GUINode(None)
    model = GUIModel(root)
    for guidef in guidefs:
        root.add(parse_model(guidef))

    search = TreeSearchDialog(guide, model)
    search.setLocationRelativeTo(None)
    search.setAlwaysOnTop(True)
    guide.setSearchDialog(search)
    guide.showNow()


h = dict()
def addEntry(target, keys):
    r = s.getRegionFromTarget(target)
    for k in keys:
        if isinstance(k, tuple):
            h[k[0]] = k[1]
            _g.addSearchEntry(k[0], r)
        else:
            _g.addSearchEntry(k, r)


def gui_search(guidefs, keyword):
    root = GUINode(None)
    model = GUIModel(root)
    for guidef in guidefs:
        root.add(parse_model(guidef))

    model.drawPathTo(_g, keyword);
    _g.showNow(3);


def search(model = None):
    if model:
        do_search(model, _g)
    else:
        ret = _g.showNow()
        if ret in h:
            h[ret]()
"""