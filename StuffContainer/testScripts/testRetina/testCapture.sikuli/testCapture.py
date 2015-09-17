img = Image.create(capture(0, 0, 100, 100))
print img
m = find(img)
print m
m.highlight(2)