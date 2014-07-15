package org.sikuli.script;

import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;
import org.sikuli.basics.Debug;

public class APITest {
  
  @BeforeClass
  public static void init() {
    Debug.test("APITest: init: starting");
    Debug.setDebugLevel(3);
    Screen s = SikuliX.init();
    Debug.test("APITest: init: ending");
  }

  @Test
  public void testAPI() {
    Debug.test("APITest: testAPI: starting");
    Debug.test("APITest: testAPI: ending");
    assertTrue(true);
  }
}
