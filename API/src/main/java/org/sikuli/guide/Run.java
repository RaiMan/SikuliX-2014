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
	Screen scr;
  static Visual sgc;

  
public static void main(String[] args) {
  Run sgr = new Run();
  sgr.scr = new Screen();
  ImagePath.add("org.sikuli.script.RunTime/ImagesAPI.sikuli");
  sgr.setUp();
  sgr.tearDown();
}
  
	private void setUp() {
		guide = new Guide();
	}

	private void tearDown() {
		guide.showNow(2f);
		guide = null;
	}

	/**
	 * Test of button method, of class Guide.
	 */
	private void testButton() {
		System.out.println("button");
		String name = "";
		Guide instance = new Guide();
		Visual expResult = null;
		Visual result = instance.button(name);
	}

	/**
	 * Test of arrow method, of class Guide.
	 */
	private void testArrow() {
		System.out.println("arrow");
		Object from = null;
		Object to = null;
		Guide instance = new Guide();
		Visual expResult = null;
		Visual result = instance.arrow(from, to);
	}
}
