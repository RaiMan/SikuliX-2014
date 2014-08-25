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

//		String imgN = "./target/classes/images/images.sikuli/image.png";
//		URL dir = FileManager.makeURL("./target/classes/images/images.sikuli");
//		Debug.test("URL: %s", FileManager.makeURL("./target/classes/images/images.sikuli"));
//    Debug.setDebugLevel(3);
//		Debug.test("***** 1st image");
//		Image img = Image.create(imgN);
//		Debug.test("***** 2nd image");
//		img = Image.create(imgN);
//		Image.dump();
//		Debug.test("***** purge");
//		Image.purge(dir);

//    Debug.setDebugLevel(3);
//		Debug.log(3, "trying testSetup");
//		Debug.test("testSetup: #returned#", Sikulix.testSetup());
    
    Debug.setLogger(new TestRun());
    new TestRun().loggerCallBack("nothing");
    Debug.setLoggerAll("loggerCallBack");
    Debug.log(3, "debug message");
		System.exit(1);
  }
  
  public void loggerCallBack(String msg) {
    p("from loggerCallBack: redirection: %s", msg);
  }
}
