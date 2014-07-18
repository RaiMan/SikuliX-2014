package org.sikuli.script;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.sikuli.basics.Debug;

public class APITest {

  public static Screen s = null;

  @BeforeClass
  public static void init() {
    Debug.test("APITest: init: starting");
    Debug.setDebugLevel(3);
		if (!Sikulix.canRun()) {
			Debug.test("ERROR: running headless - all tests will be skipped while showing success");
		} else {
			s = Sikulix.init();
		}
    Debug.test("APITest: init: ending");
  }

  @Test
  public void clickCenterOfPrimaryScreen_2Times_WithPause5() {
		if (!Sikulix.canRun()) {
			Assert.assertTrue(true);
			return;
		}
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: starting");
    s.click();
    s.wait(5f);
    s.click();
    Debug.test("APITest: clickCenterOfPrimaryScreen_2Times_WithPause5: ending");
    Assert.assertEquals(s.getCenter(), Mouse.at());
  }

  @Test
  public void findAndHighlightTopLeftSixth() {
		if (!Sikulix.canRun()) {
			Assert.assertTrue(true);
		}
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
