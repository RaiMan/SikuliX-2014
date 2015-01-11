def handler(e):
    print "in handler:", e
reg = Region(getTopLeft().grow(600))
reg.highlight(2)
reg.onAppear("1420978837040.png", handler)
reg.observeInBackground(10)
wait(2)
switchApp("safari")
wait(2)