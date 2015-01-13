reg = getTopLeft().grow(600)
reg.highlight(2)
reg.onAppear("1420978837040.png") { |e|
  Debug.user("in handler:\n%s", e)
}
reg.observeInBackground(10)
sleep(2)
App.focus("safari")
sleep(2)