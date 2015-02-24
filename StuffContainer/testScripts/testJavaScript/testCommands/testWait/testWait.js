main = function() {
  image = "nbicons";
  match = exists(image, 0, 99);
  if (isNull(match)) {
    Debug.error("exists: not found");
    return;
  }
  match = wait(new Pattern(image).similar(0.8), 0.0);
  hover(fromJSON(match), 300, 300);
  hover(image, 0, 99);
	jsonOff();
  loc = doubleClick(0, 50);
  while (loc.x < 500) {
    loc = hover(loc, 100, 30);
  }
	jsonOn();
  println(rightClick(0,50));
	return;
}

Debug.on(2);
Debug.user("Starting test");
jsonOff();
//use();
//println(use(Region.create(500,0,300,300)))
main();
Debug.user("Ending test");

SIKULIX.terminate(1, "");


