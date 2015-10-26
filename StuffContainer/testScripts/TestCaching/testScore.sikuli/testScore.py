img = "img.png"
m = wait(img, 0)
popup("ok")
print wait(Pattern(img).similar(m.getScore()), 0)
