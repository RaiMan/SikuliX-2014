main = function() {
  image = "nbicons";
  match = exists(image, 0, 99);
  if (match == null) {
    Debug.error("exists: not found");
    return;
  }
  match = wait(new Pattern(image).similar(0.8), 0.0);
  hover(match, 300, 300);
  loc = hover(image, 0, 99);
  loc = doubleClick(0, 50);
  while (loc.x < 500) {
    loc = hover(loc, 100, 30);
  }
  rightClick(0,50);
}

Debug.user("Starting test");
use();
main();
Debug.user("Ending test");


