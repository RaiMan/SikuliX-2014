def handler_ca_x(e):
  global should_stop
  print "in handler_ca_x"
  should_stop=True
  
Env.addHotkey("x", KeyModifier.CTRL+KeyModifier.ALT, handler_ca_x)
#Env.removeHotkey("x", KeyModifier.CTRL+KeyModifier.ALT)

should_stop = False
while not should_stop:
  wait(1)

print "requested to stop"
exit(1)