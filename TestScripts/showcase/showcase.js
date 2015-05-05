main = function() {
  link = "http://sikulix.com";
  worked = App.openLink(link);
  if (!worked) {
    Debug.error("App.openLink(%s): did not work", link);
    return false;
  }
  r = wait("sxpower", 30);
  if (isNull(r)) {
    Debug.error("App.openLink(%s): did not open", link);
    return false;
  }
  use1(r.below(100));
  click(wait("quickstart", 30));
  wait(3);
  closeBrowserWindow();
};

//Sikulix.popup("Hello from git showcase JavaScript");
main();
