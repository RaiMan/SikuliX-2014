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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

import org.sikuli.basics.Debug;

/**
 * The ConnectionController class manages all of the VNC connections as well
 *  as the local copies of the remote Framebuffers.  A thread (VNCThread) is
 *  created to manage the data from each connection.  Connection
 *  Controller also extends GraphicsEnvironment so that it can be used
 *  with the Java2D API
 *
 *  @author Mike Johnson
 */
public class ConnectionController extends GraphicsEnvironment {

	protected ArrayList<VNCThread> threads = new ArrayList<VNCThread>();

	private static ArrayList<ConnectionController> cons = new ArrayList<ConnectionController>();

	/**
	 * Constructor
	 *
	 * @param sockets for connections
	 */
	public ConnectionController(Socket... sockets){

		for(int i = 0; i < sockets.length; i++){
			threads.add(new VNCThread(sockets[i]));
		}

		cons.add(this);
	}

//Thread wrapper methods******************************************************/

	/**
	 * Gets a protocol message object from the array at the given index
	 *
	 * @param index
	 * @return VNC protocol message object
	 */
	protected VNCClient getC(int index){
		return threads.get(index).getClient();
	}

	/**
	 * Gets a local framebuffer object from the array at the given index
	 *
	 * @param index
	 * @return local framebuffer object
	 */
	protected Framebuffer getF(int index){
		return threads.get(index).getScreen();
	}

	/**
	 * Tells one of the VNC Client Threads to connect to the VNC server
	 */
	public void openConnection(int index){
		//since other security types are not yet supported
		threads.get(index).openConnection(1, 1);
	}

	/**
	 * Changes the rate at which one of the VNC Client Threads polls
	 * the server for changes
	 */
	public void changeUpdateInterval(int index, int milliseconds){
		threads.get(index).changePollInterval(milliseconds);
	}

	/**
	 * Closes one of the VNC Client Threads and removes it from
	 * the ArrayList
	 */
	public void closeConnection(int index){
		threads.get(index).terminate();
		threads.remove(index);
		Debug.log(4, "VNC.ConnectionController: closed connection: %d", index);
	}

	/**
	 * Sets the pixel format assoicated with one of the VNC
	 * Client Threads
	 */
	public void setPixelFormat(int index, String format,
			int bitsPerPixel, int bigEndianFlag) {
		try {
			Debug.log(4, "Setting Pixel format for thread: "+index);
			threads.get(index).setPixelFormat(format,
					bitsPerPixel, bigEndianFlag);
		}
		catch (IOException io) {
			Debug.log(-1, "Error: IO Exception "+io);
		}
	}

	/**
	 * Adds a new VNCThread to the list of connections
	 */
	public int newConnection(Socket s){
		threads.add(new VNCThread(s));
		return threads.size() -1;
	}

	public void start(int index){
		threads.get(index).start();
	}

	public int getNumberOfConnections(){
		return threads.size();
	}

//GraphicsEnvironment extension***********************************************/

	/**
	 * Returns an active connection controller
	 *
	 * @param index the index in the array
	 * @return active connectioncontroller
	 */
	public static ConnectionController getActiveController(int index){
		if(cons.isEmpty()){
			return null;
		}
		return cons.get(index);
	}

	/**
	 * Returns an array of all of the screen devices
	 *
	 * @return Array of screen devices
	 */
	@Override
	public GraphicsDevice[] getScreenDevices() throws HeadlessException {
		if(isHeadless()){
			throw new HeadlessException();
		}
		Framebuffer[] fbs = new Framebuffer[threads.size()];

		for(int i = 0; i < fbs.length; i++){
			fbs[i] = threads.get(i).getScreen();
		}

		return fbs;
	}

	/**
	 * Returns the default screen device
	 *
	 * @return The default screen device
	 */
	@Override
	public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
		if(isHeadless()){
			throw new HeadlessException();
		}
		return threads.get(0).getScreen();
	}

	/**
	 * Returns a Graphics2D object used to render on the BufferedImage.
	 *
	 * @param img BufferedImage
	 * @return the Grpahics2D
	 */
	@Override
	public Graphics2D createGraphics(BufferedImage img) {
		return img.createGraphics();
	}

	/**
	 * Returns an array of the fonts for the local graphics environement.
	 *
	 * @return An array of fonts
	 */
	@Override
	public Font[] getAllFonts() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	}

	/**
	 * Returns an array of the available font family names in the local
	 * GraphicsEnvironment.
	 *
	 * @return The array of names
	 */
	@Override
	public String[] getAvailableFontFamilyNames() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment(
				).getAvailableFontFamilyNames();
	}

	/**
	 * Returns an array of the available font family names based on
	 * a specific locale.
	 *
	 * @return The array of names
	 */
	@Override
	public String[] getAvailableFontFamilyNames(Locale l) {
		return GraphicsEnvironment.getLocalGraphicsEnvironment(
				).getAvailableFontFamilyNames(l);
	}
}
