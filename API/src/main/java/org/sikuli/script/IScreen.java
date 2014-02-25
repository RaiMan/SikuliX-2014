/*
 * Copyright 2010-2014, Sikuli.org, SikuliX.com
 * Released under the MIT License.
 *
 * modified RaiMan
 */
package org.sikuli.script;

import java.awt.Rectangle;

/**
 * INTERNAL USE
 * function template for (alternative) Screen implementations
 */
public interface IScreen {

	public IRobot getRobot();

	public Rectangle getBounds();

	public ScreenImage capture();

	public ScreenImage capture(int x, int y, int w, int h);

	public ScreenImage capture(Rectangle rect);

	public ScreenImage capture(Region reg);
}
