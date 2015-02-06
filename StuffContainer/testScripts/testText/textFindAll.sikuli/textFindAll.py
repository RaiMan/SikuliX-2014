# test change
switchApp("TextEdit")
win = App.focusedWindow()
ms = list(win.findAll("Jump"))
print ms
    