main = function() {
  worked = App.openLink("http://sikulix.com");
  if (!worked) {
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
