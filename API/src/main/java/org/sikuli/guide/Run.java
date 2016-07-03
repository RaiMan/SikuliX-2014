/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sikuli.guide;

import java.awt.Color;
import org.sikuli.script.*;

/**
 *
 * @author rhocke
 */
public class Run {

	Guide guide = null;
	static Screen scr;
  static Visual sgc;

public static void main(String[] args) {
  Run sgr = new Run();
  sgr.scr = new Screen();
  ImagePath.add("org.sikuli.script.RunTime/ImagesAPI.sikuli");
  sgr.setUp();
  sgr.testButton();
  sgr.tearDown();
}

	private void setUp() {
		guide = new Guide();
	}

	private void tearDown() {
		System.out.println(guide.showNow(5f));
		guide = null;
	}

	public void testButton() {
		System.out.println("button");
		Visual g = guide.button("Continue");
    g.setLocationRelativeToRegion(scr.getCenter().grow(500), Visual.Layout.BOTTOM);
//    g.setFontSize(12);
//    g.setColor(Color.white);
//    g.setTextColor(Color.black);
	}
}
