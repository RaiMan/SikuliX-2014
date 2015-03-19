main = function() {
  link = "http://sikulix.com";
  worked = App.openLink(link);
  if (!worked) {
    Debug.error("App.openLink(%s): did not work", link);
    return false;
  }
	r = wait("sxpower", 5);
  if (isNull(r)) {
    Debug.error("App.openLink(%s): did not open", link);
    return false;
  }
	use1(r.below(100));
	click("stories");

	win = App.focusedWindow();
	win.highlight(2);
	click("knowhow");
	click("topic");
	loc = click("reply");
	wait(2);
	click(loc.offset(-500, 100));
	type("Raimund Hocke" + Key.TAB + Key.TAB + Key.TAB)
	type("Now that I am here, I can go back and start again")
  wait(3);
	//closeBrowserWindow();
};

Sikulix.popup("Hello from git showcase JavaScript: longrun");
main();
