ex = switchApp("SikulixSetup")
print ex
if not ex.isRunning(): exit()
wait(2)
np = openApp("notepad")
#App.focus("notepad")
wait(2)
ex.focus()
print ex
ex.close()
#np.close()
wait(2)
closeApp("notepad")
