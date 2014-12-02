img = Image.create(capture())

ar = Image(img.resize(3.0))
print img
print ar
Debug.setDebugLevel(3)
img.text()
ar.text()