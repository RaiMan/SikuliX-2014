package com.sikulix.testrun;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.sikuli.script.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class TestRun {

  public static void main(String[] args) throws FindFailed, IOException {
    Debug.test("SikuliX 2014 TestRun: hello");
    Debug.setDebugLevel(0);
    Screen s = Sikulix.init();
    //TextRecognizer.getInstance();
		String imgN = "./target/classes/images/images.sikuli/image.png";
		URL dir = FileManager.makeURL("./target/classes/images/images.sikuli");
		Debug.test("URL: %s", FileManager.makeURL("./target/classes/images/images.sikuli"));
    Debug.setDebugLevel(3);
		Image img = Image.create(imgN);
		img = Image.create(imgN);
		Image.dump();
		Image.purge(dir);
		System.exit(1);
  }
}
