main = function() {
	importClass(java.awt.Desktop);
	importClass(java.net.URI);

	if (!Desktop.isDesktopSupported()) {
		return;
	}
	dt = Desktop.getDesktop();
	dt.browse(new URI("http://sikulix.com"));

	r = wait("sxpower", 5);
	use1(r.below(100));
	click("quickstart");
	wait(3);
	closeBrowserWindow();
};

Sikulix.popup("Hello from a JavaScript script ;-)");
Debug.on(3);
main();
