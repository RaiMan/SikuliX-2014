from guide import *
import java.awt.Color as Color

App.openLink("http://www.sikulix.com/uploads/1/4/2/8/14281286/1389697664.png")
tl = wait("logo.png", 10)
wait(1)

t1 = getCenter().offset(-300, 0).grow(100)
t2 = getCenter().offset(300, 0).grow(100)

img = capture(tl)
t3 = getCenter().grow(tl.h)

circle(t1)

flag(t1, "look at this").below()

txt1 = text(t1, "some text<br>to show").left(10)
txt1.setColor(Color.blue).setTextColor(Color.white)

rectangle(t2)

text(t2.grow(10), "some text<br>to show")

tooltip(t2, "this is a tooltip").above().setColor(Color.yellow)

bracket(t2).below(2).setColor(Color.green)

flag(t2, "a bracket").below(15).setColor(Color.green)

callout(t3, "This image<br>is from<br>above")

image(t3, img)

arrow(tl.getBottomRight().grow(1), t3.getTopLeft().grow(1))

bContinue = button(t3, "Continue").below(50)
flag(bContinue.getRegion(),
    "this all is shown for 30 seconds. To stop: click Continue").left(10)

print show(30)
wait(1)

popup("You should close the browser window/tab now")