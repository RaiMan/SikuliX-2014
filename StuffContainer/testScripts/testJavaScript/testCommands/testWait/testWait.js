main = function() {
  image = "nbicons";
  image1 = "nbiconsbuild";
  reg1 = new Region(0,0,400,300);
  reg2 = new Region(400,0,400,300);
  use(reg1);
  match = exists(image, 0, 99);
	print(match);
  if (isNull(match)) {
    Debug.error("exists: not found");
    return;
  }
  hover(exists(image1));
  use1(reg2);
  hover(exists(image1));
  match = wait(new Pattern(image).similar(0.8), 0.0);
	print(match);
  hover(match, 300, 300);
  hover(image, 0, 99);
  loc = doubleClick(0, 50);
  while (loc.x < 500) {
    loc = hover(loc, 100, 30);
  }
  print("rightClick: " + rightClick(0,50));
	return;
};

Debug.on(2);
Debug.user("Starting test");
//use();
main();
Debug.user("Ending test");

SIKULIX.terminate(1, "");


