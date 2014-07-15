package org.sikuli.script;

import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;
import org.sikuli.basics.Debug;

public class APITest {
  
  public static Screen s;
  
  @BeforeClass
  public static void init() {
    Debug.test("APITest: init: starting");
    Debug.setDebugLevel(3);
    s = SikuliX.init();
    Debug.test("APITest: init: ending");
  }

  @Test
  public void clickCenterOfPrimaryScreen_2Times_WithPause5() {
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: starting");
    s.click();
    s.wait(5f);
    s.click();
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: ending");
    assertTrue(true);
  }
}
