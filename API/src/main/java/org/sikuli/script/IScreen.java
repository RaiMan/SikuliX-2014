/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */
package org.sikuli.script;

import java.awt.Rectangle;
import java.io.IOException;

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

	public boolean isOtherScreen();

	public Rectangle getRect();

	public void showTarget(Location location);

	public int getID();

	public String getIDString();

	public ScreenImage getLastScreenImageFromScreen();

  public String getLastScreenImageFile(String path, String name) throws IOException;

	public int getX();

	public int getW();

	public int getY();

	public int getH();

	public ScreenImage userCapture(String string);

	public int getIdFromPoint(int srcx, int srcy);

	public String toStringShort();
}
