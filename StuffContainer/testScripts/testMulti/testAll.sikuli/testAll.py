#Settings.CheckLastSeen = False
scr = ALL
#scr = Screen(1)
#scr = selectRegion()
print scr
m = scr.find("logo.png")
m.highlight(2)
#scr.saveLastScreenImage()
hover(m)
