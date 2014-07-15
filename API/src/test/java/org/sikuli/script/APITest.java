package org.sikuli.script;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
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

  @Ignore
  public void clickCenterOfPrimaryScreen_2Times_WithPause5() {
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: starting");
    s.click();
    s.wait(5f);
    s.click();
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: ending");
    Assert.assertEquals(s.getCenter(), Mouse.at());
  }

  @Ignore
  public void findAndHighlightTopLeftSixth() {
    Debug.test("APITest: findAndHighlightTopLeftSixth: starting");
    Region r = s.get(Region.NORTH_WEST);
    Match m = s.exists(new Image(s.capture(r).getImage()));
    if (m != null) {
      m.highlight(2f);
    }    
    Debug.test("APITest: findAndHighlightTopLeftSixth: ending");
    Assert.assertTrue(m != null);
  }
}
