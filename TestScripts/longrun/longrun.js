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
	use1(r.below(100));
	click("stories");

	win = App.focusedWindow();
  use(win)
  
	click("knowhow");
	click("topic");
  r = win.getLastMatch().below(1).below(350).grow(0, 300, 0, 0)
  r.highlight(4);
	r.click("reply");
  click("form");
  
  type("Raimund Hocke" + Key.TAB + Key.TAB + Key.TAB)
	type("Now that I am here, I can go back and start again")
  wait(2);
	closeBrowserWindow();
  win.above(1).below(300).click("close");
  wait(3);
};

torun = Sikulix.input("Hello from git showcase JavaScript: longrun\n" +
         "How often should it be run? (per run about 20 secs)", "1");
RUNTIME.setOption("forJS", torun);
max = RUNTIME.getOptionNumber("forJS", 10);
for (i = 0; i < max; i++) {
  main();
}
