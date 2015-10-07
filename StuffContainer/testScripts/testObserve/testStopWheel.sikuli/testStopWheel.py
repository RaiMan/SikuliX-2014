#Debug.on(3)
Debug.off()

switchApp("Safari")
r = App.focusedWindow()
imgStop = "imgStop.png"
  
stopAppeared = r.onAppear(imgStop)
r.observeInBackground(FOREVER)
while not r.isObserving(): wait(0.3)

r.hover()
while r.isObserving():
  wheel(WHEEL_UP, 10)
  wait(1)

m = r.getEvent(stopAppeared).getMatch()
hover(m)
m.highlight(2)
