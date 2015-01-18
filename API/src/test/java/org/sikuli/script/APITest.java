package org.sikuli.script;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;

public class APITest {

  public static Screen s = null;

  @BeforeClass
  public static void init() {
//    Debug.test("APITest: init: starting");
//    Debug.setDebugLevel(3);
//		if (!Sikulix.canRun()) {
//			Debug.test("ERROR: running headless - all tests will be skipped while showing success");
//		} else {
//			if (! Settings.isLinux()) s = Sikulix.init();
//		}
//    Debug.test("APITest: init: ending");
  }

  @Test
	@Ignore
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
	@Ignore
  public void findAndHighlightTopLeftSixth() {
		if (!Sikulix.canRun()) {
			Assert.assertTrue(true);
			return;
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

	@Test
	@Ignore
	public void addingPrivateLogger() {
		Debug.test("addingPrivateLogger");
		Debug.info("this should be visible as info message");
		Debug.action("this should be visible as action log message");
		Debug.log("this should be visible as debug message");
		Debug.error("this should be visible as error message");
		boolean success = true;
		Debug.setLogger(new APITest());
		success &= Debug.setLoggerAll("info");
		Assert.assertTrue(success);
	}

	@Test
	@Ignore
	public void usingPrivateLoggerInfo() {
		Debug.test("usingPrivateLogger");
		Debug.info("test message info");
		Debug.action("test message action log");
		Debug.error("test message error");
		Assert.assertTrue(true);
	}

	public void info(String msg) {
		System.out.println("[TEST] myLogger.info: " + msg);
	}
}
