main = function() {
	logo = "logo.png";
	home = new Pattern(logo).targetOffset(0,100);
	quick = new Pattern(logo).targetOffset(100,100);

	Mouse.setMouseMovedAction(2);

	while (true) {
		click(home);
		wait(3);
		click(quick);
		wait(3);
	}
};

main();