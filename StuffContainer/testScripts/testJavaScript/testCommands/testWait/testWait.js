main = function() {
  image = "nbicons";
  match = exists(image, 0, 99);
	println(match);
  if (isNull(match)) {
    Debug.error("exists: not found");
    return;
  }
  match = wait(new Pattern(image).similar(0.8), 0.0);
	println(match);
  hover(match, 300, 300);
  hover(image, 0, 99);
  loc = doubleClick(0, 50);
  while (loc.x < 500) {
    loc = hover(loc, 100, 30);
  }
  println("rightClick: " + rightClick(0,50));
	return;
};

Debug.on(2);
Debug.user("Starting test");
use();
//use(Region.create(500,0,300,300));
main();
Debug.user("Ending test");

SIKULIX.terminate(1, "");


