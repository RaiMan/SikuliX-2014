hello = function() {
  print("Hello SikuliX!");
};

hello();

Sikulix.popup("Hello from JavaScript");
s = new Screen();
s.find("nbicons");
s.highlight(-2);
s.hover();
Runner.run("./test1js");
