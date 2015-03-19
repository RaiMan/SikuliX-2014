whenFix = function(img, reg, timeout) {
  m = null;
  while(true) {
    m = reg.wait(img, timeout);
    wait(0.5);
    if (!isNull(m.exists(img, 0))) break;
  }
  return m;
}

main = function() {
  link = "http://sikulix.com";
  worked = App.openLink(link);
  if (!worked) {
    Debug.error("App.openLink(%s): did not work", link);
    return false;
  }
	r = wait("sxpower", 10);
  if (isNull(r)) {
    Debug.error("App.openLink(%s): did not open", link);
    return false;
  }
	r.below(100).click("stories");

	win = App.focusedWindow();

	click(win.wait("knowhow", 10.0));
	click(win.wait("topic", 10.0));

  r = win.getLastMatch().below(1).below(350).grow(0, 300, 0, 0)
	r.click(whenFix("reply", r, 10.0));

  click(whenFix("form", win, 10.0), -60, 50);

  type("Raimund Hocke" + Key.TAB + Key.TAB + Key.TAB)
	type("Now that I am here, I can go back and start again")
  wait(2);
	closeBrowserWindow();
  click(whenFix("close", win.above(1).below(300), 5.0));
  wait(3);
};

torun = Sikulix.input("Hello from git showcase JavaScript: longrun\n" +
         "How often should it be run? (per run about 20 secs)", "1");
RUNTIME.setOption("forJS", torun);
max = RUNTIME.getOptionNumber("forJS", 1);
for (i = 0; i < max; i++) {
  main();
}
