reg = Region(0,23,1420,880)
setROI(reg)
Debug.highlightOn()
img = "img.png"
hover(img)
hover(getCenter())
wait(1)
reg.hover(img)
Debug.highlightOff()
