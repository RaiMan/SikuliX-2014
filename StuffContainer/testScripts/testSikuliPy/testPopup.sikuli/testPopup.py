popup("Hello World!\nHave fun with Sikuli!")

popError("Uuups, this did not work")

answer = popAsk("Should we really continue?")
if not answer: 
  pass #exit(1)

name = input("Please enter your name to log in:")
print "input:", name

name = input("Please enter your name to log in:", "anonymous")
print "inputPreset:", name
		    
password = input("please enter your secret", hidden = True)
print "inputHidden:", password

story = inputText("please give me some lines of text")
lines = story.split("\n")
for line in lines:
  print "inputText:", line

items = ("nothing selected", "item1", "item2", "item3")
selected = select("Please select an item from the list", options = items)
if selected == items[0]:
  popError("You did not select an item")
  exit(1)
