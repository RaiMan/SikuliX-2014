Debug.on(3)

for n in range(Screen.getNumberScreens()):
  exec("s%s = Screen(n); Debug.logp('%%s: %%s', n, s%s)" % (n, n))

ss = (s0, s1, s2)
img = "img.png"

for s in ss:
  s.highlight(2).hover()
  m = s.find(img)
  m.highlight(2)
  
exit()



hover(m)
hover(s1)
saveCapture("test")
m = s1.find("_test.png")
hover(m)



