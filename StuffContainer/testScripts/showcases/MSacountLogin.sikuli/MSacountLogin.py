# -------- the showcase
# the used link:
link = "https://login.live.com/ppsecure/post.srf"
# part of the used browser content
content = "content.png" # only to show, what I used
App.openLink(link) # opens the link in the default browser

if RunTime.get().runningMac:
  switchApp("Safari") # needed on Mac, to focus the browser

# --------------------- 
# the target offset was evaluated and set with Preview
start = Pattern("start.png").targetOffset(0,-130)
click(wait(start, 10)) # waits for and then clicks into and activates the username field
paste("username") # instead of type() in case it contains special characters
type(Key.TAB) # activates the password field
type("password") # be aware of the restrictions of type (US-Keyboard, not all special characters)
type(Key.ENTER)