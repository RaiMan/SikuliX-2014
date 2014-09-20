popup("testing the new popups")
resp = popAsk("want to proceed")
if resp: 
  popup("you said YES")
else: 
  popError("you did no say YES")
pw = input("I need your secret password", hidden=True)
popError("Take care! I know your secret!\n%s"%pw)
list = inputText("build a selection list")
popup("the list I got:\n%s"%list)
items = list.split()
popup("you selected:\n%s"%select(options=items))