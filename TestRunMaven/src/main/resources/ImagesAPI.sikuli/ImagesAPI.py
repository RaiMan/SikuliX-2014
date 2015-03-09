"nightly.png"
"netblogo.png"
slogo = "SikuliLogo.png"
"raimanlogo.png"
"btnnightly.png"
testfolder = "testfolder.png"

switchApp("Safari")
find(slogo)
src = getLastMatch()
switchApp("Finder")
find("ZEITmacapp.png")
trgt = getLastMatch().offset(0, 50)
click(trgt)
#dragDrop(src, trgt)

switchApp("Safari")
mouseMove(src)
mouseDown(Button.LEFT)
#wait(0.5)
switchApp("Finder")
mouseMove(trgt)
wait(0.5)
mouseUp()

trgt.click()
dragDrop("ZEITmacapp.png", testfolder)