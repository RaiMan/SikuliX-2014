img = "img.png"
offset = Region(104, 82, 385, 22).asOffset()
pImg = Pattern(img).targetOffset(offset.x, 0)
print pImg
hover(pImg)