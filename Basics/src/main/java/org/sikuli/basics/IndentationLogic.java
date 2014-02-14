/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sikuli.basics;

/**
 *
 * @author rhocke
 */
public interface IndentationLogic {

	public void setTabWidth(int tabwidth);

	public int checkDedent(String leadingWhitespace, int line);

	public void checkIndent(String leadingWhitespace, int line);

	public boolean shouldAddColon();

	public void setLastLineEndsWithColon();

	public int shouldChangeLastLineIndentation();

	public int shouldChangeNextLineIndentation();

	public void reset();

	public void addText(String text);
}
