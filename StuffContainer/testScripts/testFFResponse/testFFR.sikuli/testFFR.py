img = "icon.png"
r = get(Region.TL)
r.highlight(2)
Settings.setImageCache(0)
r.setFindFailedResponse(PROMPT)
r.wait(img, 0)
print r.getLastMatch()