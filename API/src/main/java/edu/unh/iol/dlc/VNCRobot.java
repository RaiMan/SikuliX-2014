/*
 *                       University of New Hampshire
 *                       InterOperability Laboratory
 *                           Copyright (c) 2014
 *
 * This software is provided by the IOL ``AS IS'' and any express or implied
 * warranties, including, but not limited to, the implied warranties of
 * merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the InterOperability Lab be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages.
 *
 * This software may not be resold without the express permission of
 * the InterOperability Lab.
 *
 * Feedback on this code may be sent to Mike Johnson (mjohnson@iol.unh.edu)
 * and dlnalab@iol.unh.edu.
 */
package edu.unh.iol.dlc;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.sikuli.script.IRobot;
import org.sikuli.script.IScreen;
import org.sikuli.script.Location;
import org.sikuli.script.ScreenImage;
import org.sikuli.basics.*;

/**
 * VNCRobot is an implementation of the IRobot interface
 * that controls the VNC stack through a connection controller.
 */
public class VNCRobot implements IRobot {

	private ConnectionController con;
	private int index;
	private boolean shiftFlag = false;
	/**
	 * Constructor. Only accepts Framebuffer GraphicsDevices
	 */
	public VNCRobot(GraphicsDevice gDev) throws AWTException{
		if(gDev instanceof Framebuffer){
			con = ConnectionController.getActiveController(0);
			for(int i = 0; i < con.threads.size(); i++){
				if(gDev == con.getF(i)){
					index = i;
				}
			}
		}
		else{
			throw new AWTException("Error cannot instantiate Robot" +
					" for non-remote screen");
		}
	}

//IRobot implementation*******************************************************/

	//last positions of mouse cursor
    private int last_x = -1;
    private int last_y = -1;
    private int autodelay = 0;
    private boolean waitForIdle = false;
    final static int MAX_DELAY = 60000;

	/**
     * Presses a key
     *
     * @param keycode the key
     */
    @Override
    public void keyDown(int keycode){
    	if(keycode == KeyEvent.VK_SHIFT){
    		shiftFlag = true;
    		return;
    	}
    	int key = getKeysym(keycode);
    	if(key == 0xffffff){
    		Debug.log(-1, "Error: Key not supprted-"+keycode);
    	}
        else{
            try{
               con.getC(index).keyDown(key);
            }
            catch(IOException e){
            	Debug.log(-1, "Cannot KeyDown: "+e);
            }
        }
        tidyUp();
    }

    /**
     * Releases a key
     *
     * @param keycode the key
     */
    @Override
    public void keyUp(int keycode){
    	if(keycode==KeyEvent.VK_SHIFT){
    		shiftFlag = false;
    		return;
    	}
    	int key = getKeysym(keycode);
        if(key==0xffffff){
        	Debug.log(-1, "Key not supported "+keycode);
        }
        else{
            try{
            	con.getC(index).keyUp(key);
            }
            catch(IOException e){
            	Debug.log(-1, "Cannot KeyUp: "+e);
            }
        }
        tidyUp();
    }

    /**
     * Moves the mouse to the specified x and y position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    @Override
    public void mouseMove(int x, int y){
    	try{
    		con.getC(index).mouseEvent(0, 0, 0, 0, 0, 0, 0, 0, x, y);
    	}
    	catch(IOException e){
    		Debug.log(-1, "Cannot generate mouse event: "+e);
    	}
    	last_x = x;
		last_y = y;
    	tidyUp();
    }

    /**
     * Presses a mouse button at the current location
     *
     * @param buttons can be InputEvent.BUTTON1_MASK
	 *						InputEvent.BUTTON2_MASK
	 *						InputEvent.BUTTON3_MASK
     */
	@Override
	public void mouseDown(int buttons) {
		int[] b = {0,0,0,0,0,0,0,0};
        switch(buttons){
                case InputEvent.BUTTON1_MASK: b[0]=1; break;
                case InputEvent.BUTTON2_MASK: b[1]=1; break;
                case InputEvent.BUTTON3_MASK: b[2]=1; break;
                default: throw new IllegalArgumentException();
        }
        try{
        	con.getC(index).mouseEvent(b[0], b[1], b[2],
                    b[3], b[4], b[5], b[6], b[7], last_x, last_y);
        }
        catch(IOException e){
        	Debug.log(-1, "Cannot generate mouse event: "+e);
        }
        tidyUp();
	}

	/**
     * Releases mouse buttons at last_x and last_y positions
     *
     */
	@Override
	public int mouseUp(int buttons) {
		try{
			con.getC(index).mouseEvent(0,0,0,0,0,0,0,0, last_x, last_y);
        }
        catch(IOException e){
        	Debug.log(-1, "Cannot generate mouse event: "+e);
        }
        tidyUp();

        return 0;
	}

  @Override
  public void mouseReset() {
    //TODO implement mouse reset
  }

	/**
     * Method moves the mouse wheel at an x and y position an indicated amount.
     * If wheelAmt is positive the wheel moves up, if it is
     * negative, it moves down.
     *
     * @param wheelAmt Amount to move wheel
     */
	@Override
	public void mouseWheel(int wheelAmt) {
		if(wheelAmt > 0){
            for(int i=0 ; i < wheelAmt; i++){
                try{
                	con.getC(index).mouseEvent(0,0,0,0,1,0,0,0,last_x,last_y);
                	con.getC(index).mouseEvent(0,0,0,0,0,0,0,0,last_x,last_y);
                }
                catch(IOException e){
                	Debug.log(-1, "Cannot generate mouse event: "+e);
                }
            }
        }
        else{
            for(int j = 0; j > (-wheelAmt); j--){
                try{
                	con.getC(index).mouseEvent(0,0,0,1,0,0,0,0,last_x,last_y);
                	con.getC(index).mouseEvent(0,0,0,0,0,0,0,0,last_x,last_y);
                }
                catch(IOException e){
                	Debug.log(-1, "Cannot generate mouse event: "+e);
                }
            }
        }
		tidyUp();
	}

	/**
	 * Creates a screen capture of the remote screen.
	 *
	 * @param sr - region to capture
	 * @return ScreenImage of the remote screen
	 */
	@Override
	public ScreenImage captureScreen(Rectangle sr) {
		BufferedImage bimg = null;
		while (bimg == null) {
			bimg = con.getF(index).getBuffer();
		}
		return new ScreenImage(sr, bimg.getSubimage(sr.x, sr.y, sr.width, sr.height));
	}

	/**
	 * Returns a BufferedImage of the specified rectangle on the
	 * remote desktop
	 *
	 * @param rect
	 * @return
	 */
	public BufferedImage capture(Rectangle rect){
		return con.getF(index).getBuffer().getSubimage(
				rect.x, rect.y,
				rect.width, rect.height);
	}

	/**
	 * Waits until all events are processed.
	 */
	@Override
	public void waitForIdle() {
		try {
			new java.awt.Robot().waitForIdle();
		}
		catch (AWTException e) {
			Debug.log(-1, "Error-could non instantiate robot: "+e);
		}
	}

	/**
	 * Sleeps for the specified time.
	 */
	@Override
	public void delay(int ms) {
		if(ms < 0){
			ms = 0;
		}
		if(ms > MAX_DELAY){
			ms = MAX_DELAY;
		}
		try{
			Thread.sleep(ms);
		}
		catch(InterruptedException e){
			Debug.log(-1, "Thread Interrupted: "+e);
		}
	}

	/**
	 * Sets the number of milliseconds this Robot sleeps after generating an event.
	 */
	@Override
	public void setAutoDelay(int ms) {
		if(ms < 0){
			ms = 0;
		}
		if(ms > MAX_DELAY){
			ms = MAX_DELAY;
		}
		autodelay = ms;
	}

	/**
	 * drags and drops the mouse
	 */
	public void dragDrop(Location start,Location end,int steps,long ms,int buttons){
        mouseMove(start.x, start.y);
	    mouseDown(buttons);
	    delay((int)(Settings.DelayAfterDrag*1000));
	    waitForIdle();
	    smoothMove(start, end, ms);
	    delay((int)(Settings.DelayBeforeDrop*1000));
	    mouseUp(buttons);
	    waitForIdle();
	}

	/**
	 * Types the specified keyCodes based on a certain KeyMode
	 *
	 * @param mode     KeyMode.PRESS_ONLY
	 * 				   KeyMode.RELEASE_ONLY
	 * 				   KeyMode.PRESS_RELEASE
	 * @param keyCodes KeyEvent
	 */
	protected void doType(KeyMode mode, int... keyCodes) {
	      if(mode==KeyMode.PRESS_ONLY){
	         for(int i=0;i<keyCodes.length;i++){
	            keyDown(keyCodes[i]);
	         }
	      }
	      else if(mode==KeyMode.RELEASE_ONLY){
	         for(int i=0;i<keyCodes.length;i++){
	            keyUp(keyCodes[i]);
	         }
	      }
	      else{
	         for(int i=0;i<keyCodes.length;i++)
	            keyDown(keyCodes[i]);
	         for(int i=0;i<keyCodes.length;i++)
	            keyUp(keyCodes[i]);
	      }
	   }

	@Override
	   public void typeChar(char character, KeyMode mode) {
	      switch (character) {
	         case 'a'     : doType(mode,KeyEvent.VK_A); break;
	         case 'b'     : doType(mode,KeyEvent.VK_B); break;
	         case 'c'     : doType(mode,KeyEvent.VK_C); break;
	         case 'd'     : doType(mode,KeyEvent.VK_D); break;
	         case 'e'     : doType(mode,KeyEvent.VK_E); break;
	         case 'f'     : doType(mode,KeyEvent.VK_F); break;
	         case 'g'     : doType(mode,KeyEvent.VK_G); break;
	         case 'h'     : doType(mode,KeyEvent.VK_H); break;
	         case 'i'     : doType(mode,KeyEvent.VK_I); break;
	         case 'j'     : doType(mode,KeyEvent.VK_J); break;
	         case 'k'     : doType(mode,KeyEvent.VK_K); break;
	         case 'l'     : doType(mode,KeyEvent.VK_L); break;
	         case 'm'     : doType(mode,KeyEvent.VK_M); break;
	         case 'n'     : doType(mode,KeyEvent.VK_N); break;
	         case 'o'     : doType(mode,KeyEvent.VK_O); break;
	         case 'p'     : doType(mode,KeyEvent.VK_P); break;
	         case 'q'     : doType(mode,KeyEvent.VK_Q); break;
	         case 'r'     : doType(mode,KeyEvent.VK_R); break;
	         case 's'     : doType(mode,KeyEvent.VK_S); break;
	         case 't'     : doType(mode,KeyEvent.VK_T); break;
	         case 'u'     : doType(mode,KeyEvent.VK_U); break;
	         case 'v'     : doType(mode,KeyEvent.VK_V); break;
	         case 'w'     : doType(mode,KeyEvent.VK_W); break;
	         case 'x'     : doType(mode,KeyEvent.VK_X); break;
	         case 'y'     : doType(mode,KeyEvent.VK_Y); break;
	         case 'z'     : doType(mode,KeyEvent.VK_Z); break;
	         case 'A'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
	         case 'B'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
	         case 'C'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
	         case 'D'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
	         case 'E'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
	         case 'F'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
	         case 'G'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
	         case 'H'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
	         case 'I'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
	         case 'J'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
	         case 'K'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
	         case 'L'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
	         case 'M'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
	         case 'N'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
	         case 'O'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
	         case 'P'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
	         case 'Q'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
	         case 'R'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
	         case 'S'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
	         case 'T'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
	         case 'U'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
	         case 'V'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
	         case 'W'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
	         case 'X'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
	         case 'Y'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
	         case 'Z'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
	         case '`'     : doType(mode,KeyEvent.VK_BACK_QUOTE); break;
	         case '0'     : doType(mode,KeyEvent.VK_0); break;
	         case '1'     : doType(mode,KeyEvent.VK_1); break;
	         case '2'     : doType(mode,KeyEvent.VK_2); break;
	         case '3'     : doType(mode,KeyEvent.VK_3); break;
	         case '4'     : doType(mode,KeyEvent.VK_4); break;
	         case '5'     : doType(mode,KeyEvent.VK_5); break;
	         case '6'     : doType(mode,KeyEvent.VK_6); break;
	         case '7'     : doType(mode,KeyEvent.VK_7); break;
	         case '8'     : doType(mode,KeyEvent.VK_8); break;
	         case '9'     : doType(mode,KeyEvent.VK_9); break;
	         case '-'     : doType(mode,KeyEvent.VK_MINUS); break;
	         case '='     : doType(mode,KeyEvent.VK_EQUALS); break;
	         case '~'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
	         case '!'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_1); break;
	         case '@'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_2); break;
	         case '#'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_3); break;
	         case '$'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_4); break;
	         case '%'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
	         case '^'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_6); break;
	         case '&'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_7); break;
	         case '*'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_8); break;
	         case '('     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_9); break;
	         case ')'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_0); break;
	         case '_'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS); break;
	         case '+'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS); break;
	         case '\b'    : doType(mode,KeyEvent.VK_BACK_SPACE); break;
	         case '\t'    : doType(mode,KeyEvent.VK_TAB); break;
	         case '\r'    : doType(mode,KeyEvent.VK_ENTER); break;
	         case '\n'    : doType(mode,KeyEvent.VK_ENTER); break;
	         case '['     : doType(mode,KeyEvent.VK_OPEN_BRACKET); break;
	         case ']'     : doType(mode,KeyEvent.VK_CLOSE_BRACKET); break;
	         case '\\'    : doType(mode,KeyEvent.VK_BACK_SLASH); break;
	         case '{'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
	         case '}'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
	         case '|'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
	         case ';'     : doType(mode,KeyEvent.VK_SEMICOLON); break;
	         case ':'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON); break;
	         case '\''    : doType(mode,KeyEvent.VK_QUOTE); break;
	         case '"'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE); break;
	         case ','     : doType(mode,KeyEvent.VK_COMMA); break;
	         case '<'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
	         case '.'     : doType(mode,KeyEvent.VK_PERIOD); break;
	         case '>'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
	         case '/'     : doType(mode,KeyEvent.VK_SLASH); break;
	         case '?'     : doType(mode,KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
	         case ' '     : doType(mode,KeyEvent.VK_SPACE); break;
	         case '\u001b': doType(mode,KeyEvent.VK_ESCAPE); break;
	         case '\ue000': doType(mode,KeyEvent.VK_UP); break;
	         case '\ue001': doType(mode,KeyEvent.VK_RIGHT); break;
	         case '\ue002': doType(mode,KeyEvent.VK_DOWN); break;
	         case '\ue003': doType(mode,KeyEvent.VK_LEFT); break;
	         case '\ue004': doType(mode,KeyEvent.VK_PAGE_UP); break;
	         case '\ue005': doType(mode,KeyEvent.VK_PAGE_DOWN); break;
	         case '\ue006': doType(mode,KeyEvent.VK_DELETE); break;
	         case '\ue007': doType(mode,KeyEvent.VK_END); break;
	         case '\ue008': doType(mode,KeyEvent.VK_HOME); break;
	         case '\ue009': doType(mode,KeyEvent.VK_INSERT); break;
	         case '\ue011': doType(mode,KeyEvent.VK_F1); break;
	         case '\ue012': doType(mode,KeyEvent.VK_F2); break;
	         case '\ue013': doType(mode,KeyEvent.VK_F3); break;
	         case '\ue014': doType(mode,KeyEvent.VK_F4); break;
	         case '\ue015': doType(mode,KeyEvent.VK_F5); break;
	         case '\ue016': doType(mode,KeyEvent.VK_F6); break;
	         case '\ue017': doType(mode,KeyEvent.VK_F7); break;
	         case '\ue018': doType(mode,KeyEvent.VK_F8); break;
	         case '\ue019': doType(mode,KeyEvent.VK_F9); break;
	         case '\ue01A': doType(mode,KeyEvent.VK_F10); break;
	         case '\ue01B': doType(mode,KeyEvent.VK_F11); break;
	         case '\ue01C': doType(mode,KeyEvent.VK_F12); break;
	         case '\ue01D': doType(mode,KeyEvent.VK_F13); break;
	         case '\ue01E': doType(mode,KeyEvent.VK_F14); break;
	         case '\ue01F': doType(mode,KeyEvent.VK_F15); break;
	         case '\ue020': doType(mode,KeyEvent.VK_SHIFT); break;
	         case '\ue021': doType(mode,KeyEvent.VK_CONTROL); break;
	         case '\ue022': doType(mode,KeyEvent.VK_ALT); break;
	         case '\ue023': doType(mode,KeyEvent.VK_META); break;
	         case '\ue024': doType(mode,KeyEvent.VK_PRINTSCREEN); break;
	         case '\ue025': doType(mode,KeyEvent.VK_SCROLL_LOCK); break;
	         case '\ue026': doType(mode,KeyEvent.VK_PAUSE); break;
	         case '\ue027': doType(mode,KeyEvent.VK_CAPS_LOCK); break;
	         case '\ue030': doType(mode,KeyEvent.VK_NUMPAD0); break;
	         case '\ue031': doType(mode,KeyEvent.VK_NUMPAD1); break;
	         case '\ue032': doType(mode,KeyEvent.VK_NUMPAD2); break;
	         case '\ue033': doType(mode,KeyEvent.VK_NUMPAD3); break;
	         case '\ue034': doType(mode,KeyEvent.VK_NUMPAD4); break;
	         case '\ue035': doType(mode,KeyEvent.VK_NUMPAD5); break;
	         case '\ue036': doType(mode,KeyEvent.VK_NUMPAD6); break;
	         case '\ue037': doType(mode,KeyEvent.VK_NUMPAD7); break;
	         case '\ue038': doType(mode,KeyEvent.VK_NUMPAD8); break;
	         case '\ue039': doType(mode,KeyEvent.VK_NUMPAD9); break;
	         case '\ue03A': doType(mode,KeyEvent.VK_SEPARATOR); break;
	         case '\ue03B': doType(mode,KeyEvent.VK_NUM_LOCK); break;
	         case '\ue03C': doType(mode,KeyEvent.VK_ADD); break;
	         case '\ue03D': doType(mode,KeyEvent.VK_MINUS); break;
	         case '\ue03E': doType(mode,KeyEvent.VK_MULTIPLY); break;
	         case '\ue03F': doType(mode,KeyEvent.VK_DIVIDE); break;
	         default:
	            throw new IllegalArgumentException("Cannot type character " + character);
	      }
	   }

	/**
	 * Smooth moves the mouse
	 */
	@Override
	public void smoothMove(Location dest) {
		smoothMove(new Location(last_x,last_y),
				dest, (long)(Settings.MoveMouseDelay*1000L));
	}

	/**
	 * Uses the Animator class to smooth move the mouse
	 */
	@Override
	public void smoothMove(Location src, Location dest, long ms) {
		if(ms == 0){
			mouseMove(dest.x, dest.y);
	         return;
		}

		AnimatorTimeBased aniX = new AnimatorTimeBased(
	                        new AnimatorOutQuarticEase((float)src.x, (float)dest.x, ms));
		AnimatorTimeBased aniY = new AnimatorTimeBased(
	                        new AnimatorOutQuarticEase((float)src.y, (float)dest.y, ms));
		while(aniX.running()){
			float x = aniX.step();
			float y = aniY.step();
			mouseMove((int)x, (int)y);
			delay(50);
		}
	}

	//Other functions//////////////////////////////////////////////////////////

	/**
     * Helper function that converts from KeyEvent.VK_(key) to
     * X11 Keysyms for use in RFB Protocol Messages.
     *
     */
    private int getKeysym(int keycode){
        int key;
        if(shiftFlag){
        	switch(keycode){
        	case (KeyEvent.VK_0): key=0x0029; break; //right paren
            case (KeyEvent.VK_1): key=0x0021; break; //exclaimation
            case (KeyEvent.VK_2): key=0x0040; break; //at sign
            case (KeyEvent.VK_3): key=0x0023; break; //number sign
            case (KeyEvent.VK_4): key=0x0024; break; //dollar
            case (KeyEvent.VK_5): key=0x0025; break; //percent
            case (KeyEvent.VK_6): key=0x005e; break; //circumflex
            case (KeyEvent.VK_7): key=0x0026; break; //ampersand
            case (KeyEvent.VK_8): key=0x002a; break; //asterisk
            case (KeyEvent.VK_9): key=0x0028; break; //left paren
            case (KeyEvent.VK_BACK_SPACE): key=0xff08; break;
            case (KeyEvent.VK_TAB): key=0xfd05; break; //back tab
            case (KeyEvent.VK_ENTER): key=0xff0d; break;
            case (KeyEvent.VK_INSERT): key=0xfd1d; break; //printscreen
            case (KeyEvent.VK_DELETE): key=0xffff; break;
            case (KeyEvent.VK_HOME): key=0xff50; break;
            case (KeyEvent.VK_END): key=0xff57; break;
            case (KeyEvent.VK_PAGE_UP): key=0xff55; break;
            case (KeyEvent.VK_PAGE_DOWN): key=0xff56; break;
            case (KeyEvent.VK_LEFT): key=0xff51; break;
            case (KeyEvent.VK_UP): key=0xff52; break;
            case (KeyEvent.VK_RIGHT): key=0xff53; break;
            case (KeyEvent.VK_DOWN): key=0xff54; break;
            case (KeyEvent.VK_F1): key=0xffbe; break;
            case (KeyEvent.VK_F2): key=0xffbf; break;
            case (KeyEvent.VK_F3): key=0xffc0; break;
            case (KeyEvent.VK_F4): key=0xffc1; break;
            case (KeyEvent.VK_F5): key=0xffc2; break;
            case (KeyEvent.VK_F6): key=0xffc3; break;
            case (KeyEvent.VK_F7): key=0xffc4; break;
            case (KeyEvent.VK_F8): key=0xffc5; break;
            case (KeyEvent.VK_F9): key=0xffc6; break;
            case (KeyEvent.VK_F10): key=0xffc7; break;
            case (KeyEvent.VK_F11): key=0xffc8; break;
            case (KeyEvent.VK_F12): key=0xffc9; break;
            case (KeyEvent.VK_CONTROL): key=0xffe3; break;
            case (KeyEvent.VK_META): key=0xffe7; break;
            case (KeyEvent.VK_ALT): key=0xffe9; break;
            case (KeyEvent.VK_A): key=0x0041; break;//A
            case (KeyEvent.VK_B): key=0x0042; break;//B
            case (KeyEvent.VK_C): key=0x0043; break;//C
            case (KeyEvent.VK_D): key=0x0044; break;//D
            case (KeyEvent.VK_E): key=0x0045; break;//E
            case (KeyEvent.VK_F): key=0x0046; break;//F
            case (KeyEvent.VK_G): key=0x0047; break;//G
            case (KeyEvent.VK_H): key=0x0048; break;//H
            case (KeyEvent.VK_I): key=0x0049; break;//I
            case (KeyEvent.VK_J): key=0x004a; break;//J
            case (KeyEvent.VK_K): key=0x004b; break;//K
            case (KeyEvent.VK_L): key=0x004c; break;//L
            case (KeyEvent.VK_M): key=0x004d; break;//M
            case (KeyEvent.VK_N): key=0x004e; break;//N
            case (KeyEvent.VK_O): key=0x004f; break;//O
            case (KeyEvent.VK_P): key=0x0050; break;//P
            case (KeyEvent.VK_Q): key=0x0051; break;//Q
            case (KeyEvent.VK_R): key=0x0052; break;//R
            case (KeyEvent.VK_S): key=0x0053; break;//S
            case (KeyEvent.VK_T): key=0x0054; break;//T
            case (KeyEvent.VK_U): key=0x0055; break;//U
            case (KeyEvent.VK_V): key=0x0056; break;//V
            case (KeyEvent.VK_W): key=0x0057; break;//W
            case (KeyEvent.VK_X): key=0x0058; break;//X
            case (KeyEvent.VK_Y): key=0x0059; break;//Y
            case (KeyEvent.VK_Z): key=0x005a; break;//Z
            case (KeyEvent.VK_SPACE): key=0x0020; break;
            case (KeyEvent.VK_BACK_QUOTE): key=0x007e; break; //~
            case (KeyEvent.VK_MINUS): key=0x005f; break;//_
            case (KeyEvent.VK_EQUALS): key=0x002b; break;//+
            case (KeyEvent.VK_QUOTE): key=0x0022; break;//"
            case (KeyEvent.VK_SEMICOLON): key=0x003a; break;//:
            case (KeyEvent.VK_BACK_SLASH): key=0x007c; break;//|
            case (KeyEvent.VK_BRACELEFT): key=0x007b; break;//{
            case (KeyEvent.VK_BRACERIGHT): key=0x007d; break;//}
            case (KeyEvent.VK_PERIOD): key=0x003e; break;//>
            case (KeyEvent.VK_COMMA): key=0x003c; break;//<
            case (KeyEvent.VK_SLASH): key=0x003f; break;//?
            case (KeyEvent.VK_PLUS): key=0x002b; break;
            case (KeyEvent.VK_OPEN_BRACKET): key=0x007b; break;//{
            case (KeyEvent.VK_CLOSE_BRACKET): key=0x007d; break;//}
            case (KeyEvent.VK_ESCAPE): key=0xff1b; break;
            case (KeyEvent.VK_F13): key=0xffca; break;
            case (KeyEvent.VK_F14): key=0xffcb; break;
            case (KeyEvent.VK_F15): key=0xffcb; break;
            case (KeyEvent.VK_PRINTSCREEN): key=0xfd1d; break;
            case (KeyEvent.VK_SCROLL_LOCK): key=0xff14; break;
            case (KeyEvent.VK_PAUSE): key=0xff13; break;
            case (KeyEvent.VK_CAPS_LOCK): key=0xffe5; break;
            case (KeyEvent.VK_NUMPAD0): key=0x0030; break;
            case (KeyEvent.VK_NUMPAD1): key=0x0031; break;
            case (KeyEvent.VK_NUMPAD2): key=0x0032; break;
            case (KeyEvent.VK_NUMPAD3): key=0x0033; break;
            case (KeyEvent.VK_NUMPAD4): key=0x0034; break;
            case (KeyEvent.VK_NUMPAD5): key=0x0035; break;
            case (KeyEvent.VK_NUMPAD6): key=0x0036; break;
            case (KeyEvent.VK_NUMPAD7): key=0x0037; break;
            case (KeyEvent.VK_NUMPAD8): key=0x0038; break;
            case (KeyEvent.VK_NUMPAD9): key=0x0039; break;
            case (KeyEvent.VK_SEPARATOR): key=0xffac; break;
            case (KeyEvent.VK_NUM_LOCK): key=0xff7f; break;
            case (KeyEvent.VK_MULTIPLY): key=0x002a; break;
            case (KeyEvent.VK_ADD): key=0x002b; break;
            case (KeyEvent.VK_DIVIDE): key=0x002f; break;
            default: key=0xffffff; break;
        	}
        }
        else{
        	switch(keycode){
            case (KeyEvent.VK_0): key=0x0030; break;//0
            case (KeyEvent.VK_1): key=0x0031; break;//1
            case (KeyEvent.VK_2): key=0x0032; break;//2
            case (KeyEvent.VK_3): key=0x0033; break;//3
            case (KeyEvent.VK_4): key=0x0034; break;//4
            case (KeyEvent.VK_5): key=0x0035; break;//5
            case (KeyEvent.VK_6): key=0x0036; break;//6
            case (KeyEvent.VK_7): key=0x0037; break;//7
            case (KeyEvent.VK_8): key=0x0038; break;//8
            case (KeyEvent.VK_9): key=0x0039; break;//9
            case (KeyEvent.VK_BACK_SPACE): key=0xff08; break;
            case (KeyEvent.VK_TAB): key=0xff09; break;
            case (KeyEvent.VK_ENTER): key=0xff0d; break;
            case (KeyEvent.VK_INSERT): key=0xff63; break;
            case (KeyEvent.VK_DELETE): key=0xffff; break;
            case (KeyEvent.VK_HOME): key=0xff50; break;
            case (KeyEvent.VK_END): key=0xff57; break;
            case (KeyEvent.VK_PAGE_UP): key=0xff55; break;
            case (KeyEvent.VK_PAGE_DOWN): key=0xff56; break;
            case (KeyEvent.VK_LEFT): key=0xff51; break;
            case (KeyEvent.VK_UP): key=0xff52; break;
            case (KeyEvent.VK_RIGHT): key=0xff53; break;
            case (KeyEvent.VK_DOWN): key=0xff54; break;
            case (KeyEvent.VK_F1): key=0xffbe; break;
            case (KeyEvent.VK_F2): key=0xffbf; break;
            case (KeyEvent.VK_F3): key=0xffc0; break;
            case (KeyEvent.VK_F4): key=0xffc1; break;
            case (KeyEvent.VK_F5): key=0xffc2; break;
            case (KeyEvent.VK_F6): key=0xffc3; break;
            case (KeyEvent.VK_F7): key=0xffc4; break;
            case (KeyEvent.VK_F8): key=0xffc5; break;
            case (KeyEvent.VK_F9): key=0xffc6; break;
            case (KeyEvent.VK_F10): key=0xffc7; break;
            case (KeyEvent.VK_F11): key=0xffc8; break;
            case (KeyEvent.VK_F12): key=0xffc9; break;
            case (KeyEvent.VK_CONTROL): key=0xffe3; break;
            case (KeyEvent.VK_META): key=0xffe7; break;
            case (KeyEvent.VK_ALT): key=0xffe9; break;
            case (KeyEvent.VK_A): key=0x0061; break;//a
            case (KeyEvent.VK_B): key=0x0062; break;//b
            case (KeyEvent.VK_C): key=0x0063; break;//c
            case (KeyEvent.VK_D): key=0x0064; break;//d
            case (KeyEvent.VK_E): key=0x0065; break;//e
            case (KeyEvent.VK_F): key=0x0066; break;//f
            case (KeyEvent.VK_G): key=0x0067; break;//g
            case (KeyEvent.VK_H): key=0x0068; break;//h
            case (KeyEvent.VK_I): key=0x0069; break;//i
            case (KeyEvent.VK_J): key=0x006a; break;//j
            case (KeyEvent.VK_K): key=0x006b; break;//k
            case (KeyEvent.VK_L): key=0x006c; break;//l
            case (KeyEvent.VK_M): key=0x006d; break;//m
            case (KeyEvent.VK_N): key=0x006e; break;//n
            case (KeyEvent.VK_O): key=0x006f; break;//o
            case (KeyEvent.VK_P): key=0x0070; break;//p
            case (KeyEvent.VK_Q): key=0x0071; break;//q
            case (KeyEvent.VK_R): key=0x0072; break;//r
            case (KeyEvent.VK_S): key=0x0073; break;//s
            case (KeyEvent.VK_T): key=0x0074; break;//t
            case (KeyEvent.VK_U): key=0x0075; break;//u
            case (KeyEvent.VK_V): key=0x0076; break;//v
            case (KeyEvent.VK_W): key=0x0077; break;//w
            case (KeyEvent.VK_X): key=0x0078; break;//x
            case (KeyEvent.VK_Y): key=0x0079; break;//y
            case (KeyEvent.VK_Z): key=0x007a; break;//z
            case (KeyEvent.VK_SPACE): key=0x0020; break;
            case (KeyEvent.VK_BACK_QUOTE): key=0x0060; break;//`
            case (KeyEvent.VK_MINUS): key=0x002d; break;//-
            case (KeyEvent.VK_EQUALS): key=0x003d; break;//=
            case (KeyEvent.VK_QUOTE): key=0x0027; break;//'
            case (KeyEvent.VK_SEMICOLON): key=0x003b; break;//;
            case (KeyEvent.VK_BACK_SLASH): key=0x005c; break;//\
            case (KeyEvent.VK_BRACELEFT): key=0x005b; break;//{
            case (KeyEvent.VK_BRACERIGHT): key=0x005d; break;//}
            case (KeyEvent.VK_PERIOD): key=0x002e; break;//.
            case (KeyEvent.VK_COMMA): key=0x002c; break;//,
            case (KeyEvent.VK_SLASH): key=0x002f; break;///
            case (KeyEvent.VK_PLUS): key=0x002b; break;//+
            case (KeyEvent.VK_OPEN_BRACKET): key=0x005b; break;//[
            case (KeyEvent.VK_CLOSE_BRACKET): key=0x005d; break;//]
            case (KeyEvent.VK_ESCAPE): key=0xff1b; break;
            case (KeyEvent.VK_F13): key=0xffca; break;
            case (KeyEvent.VK_F14): key=0xffcb; break;
            case (KeyEvent.VK_F15): key=0xffcb; break;
            case (KeyEvent.VK_PRINTSCREEN): key=0xfd1d; break;
            case (KeyEvent.VK_SCROLL_LOCK): key=0xff14; break;
            case (KeyEvent.VK_PAUSE): key=0xff13; break;
            case (KeyEvent.VK_CAPS_LOCK): key=0xffe5; break;
            case (KeyEvent.VK_NUMPAD0): key=0x0030; break;//0
            case (KeyEvent.VK_NUMPAD1): key=0x0031; break;//1
            case (KeyEvent.VK_NUMPAD2): key=0x0032; break;//2
            case (KeyEvent.VK_NUMPAD3): key=0x0033; break;//3
            case (KeyEvent.VK_NUMPAD4): key=0x0034; break;//4
            case (KeyEvent.VK_NUMPAD5): key=0x0035; break;//5
            case (KeyEvent.VK_NUMPAD6): key=0x0036; break;//6
            case (KeyEvent.VK_NUMPAD7): key=0x0037; break;//7
            case (KeyEvent.VK_NUMPAD8): key=0x0038; break;//8
            case (KeyEvent.VK_NUMPAD9): key=0x0039; break;//9
            case (KeyEvent.VK_SEPARATOR): key=0xffac; break;
            case (KeyEvent.VK_NUM_LOCK): key=0xff7f; break;
            case (KeyEvent.VK_MULTIPLY): key=0x002a; break;
            case (KeyEvent.VK_ADD): key=0x002b; break;
            case (KeyEvent.VK_DIVIDE): key=0x002f; break;
            default: key=0xffffff; break;
        	}
        }
        Debug.log(3, "Keycode-"+keycode);
        Debug.log(3, "Shiftflag-"+shiftFlag);
        Debug.log(3, "Key-"+Integer.toHexString(key));
        return key;
    }

    /**
     * Executes after Robot performs action
     */
    private void tidyUp(){
    	if(waitForIdle){
    		waitForIdle();
    	}
    	delay(autodelay);
    }

//NEW SIKULI METHODS

	@Override
	public void keyDown(String keys) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyUp(String keys) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pressModifiers(int modifiers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseModifiers(int modifiers) {
		// TODO Auto-generated method stub

	}

	@Override
	public void typeKey(int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void typeStarts() {
		// TODO Auto-generated method stub

	}

	@Override
	public void typeEnds() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clickStarts() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clickEnds() {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getColorAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public IScreen getScreen() {
		return null;
	}
}
