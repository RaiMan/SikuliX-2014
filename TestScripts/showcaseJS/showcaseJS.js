main = function() {
  link = "http://sikulix.co";
  worked = App.openLink(link);
  if (!worked) {
    Debug.error("App.openLink(%s): did not work", link);
    return false;
  }
	r = wait("sxpower", 5);
	use1(r.below(100));
	click("quickstart");
	wait(3);
	closeBrowserWindow();
};

Sikulix.popup("Hello from git showcase JavaScript");
main();
