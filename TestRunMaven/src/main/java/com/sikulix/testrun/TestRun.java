package com.sikulix.testrun;

import org.sikuli.script.*;
import org.sikuli.basics.Debug;

public class TestRun {

  public static void main(String[] args) {
    Debug.test("SikuliX 2014 TestRun: hello");
    Debug.setDebugLevel(3);
    Screen s = Sikulix.init();
    TextRecognizer.getInstance();
  }
}
