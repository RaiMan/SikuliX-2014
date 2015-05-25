switchApp("textedit"); wait(1)
icon = Pattern("icon.png").exact()
#r = selectRegion()
te = switchApp("textedit"); wait(1)
r = te.window()
r.setAutoWaitTimeout(0)
matches = r.findAllByRow(icon)
if matches:
  for m in matches:
    m.highlight(1);
else:
  popup("not found")

matches = r.findAllByColumn(icon)
if matches:
  for m in matches:
    m.highlight(1);
else:
  popup("not found")