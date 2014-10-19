package com.sikulix.testrun;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.sikuli.script.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class TestRun {

  private static void p(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

  public static void main(String[] args) throws FindFailed, IOException {
    Debug.test("SikuliX 2014 TestRun: hello");
    Debug.setDebugLevel(3);
    Screen s = Sikulix.init();

		//TextRecognizer.getInstance();

		String imgN = "./target/classes/images/images.sikuli";
		String img = "image.png";
    Debug.setDebugLevel(3);
//    ImagePath.setBundlePath(imgN);
    ImagePath.add("com.sikulix.testrun.TestRun/images/images.sikuli");
		Debug.test("***** 1st image");
		Image iimg = Image.create(img);
		Debug.test("***** 2nd image");
		iimg = Image.create(img);
		Image.dump();

    System.exit(1);
  }

  public void loggerCallBack(String msg) {
    p("from loggerCallBack: redirection: %s", msg);
  }
}
