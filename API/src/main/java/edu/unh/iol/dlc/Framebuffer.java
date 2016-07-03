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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.sikuli.basics.Debug;

/**
 * The framebuffer class is responsible for maintaining the local
 * copy of the remote framebuffer.  The class extends GraphicsDevice
 * so that it can be used with the java2D API.
 *
 * @author Mike Johnson
 */
public class Framebuffer extends GraphicsDevice {

	/*
     * Below are the fields associated with the
     * Server's PixelFormat and Framebuffer.
     */
	private FBConfig conf = null;
	private BufferedImage buffer;
	private int[][] rgbs = null;
	private boolean updated = false;
	private BufferedImage doubleBuffer;

//General methods*************************************************************/

	/**
	 * Method that sets the pixel format of the framebuffer.
	 *
	 * @param data sets the pixel format of the framebuffer for the connection
	 * @param name name of the remote desktop
	 * @return true if the pixel format is valid for what the VNC stack
	 * 		   	    currently supports
	 * 		   false if the pixel format is not valid
	 */
	protected boolean setPF(int[] data, String name){

		if(conf != null){
			int[] array = {getWidth(), getHeight(), data[0],data[1],data[2],
					data[3],data[4],data[5],data[6],data[7],data[8],data[9]};
			conf = new FBConfig(array, name);
		}
		else{
			conf = new FBConfig(data, name);
		}
		if(conf.getIdNum() == 5){
			return false;
		}
		return true;
	}

	/**
	 * Resets the pixel format to a different configuration
	 *
	 * @param data sets the pixel format of the framebuffer for the connection
	 * @return true if the pixel format is valid for what the VNC stack
	 * 		   	    currently supports
	 * 		   false if the pixel format is not valid
	 *
	 */
	protected boolean resetPF(int[] data){
		return setPF(data, conf.getName());
	}

	/**
	 * Gets the PixelFormat in the form of a GraphicsConfiguration
	 * object
	 *
	 * @return conf the GraphicsConfiguration
	 */
	protected GraphicsConfiguration getPF(){
		return conf;
	}

	/**
	 * Gets the width of the framebuffer
	 */
	protected int getWidth(){
		return conf.getBounds().width;
	}

	/**
	 * Gets the height of the framebuffer
	 */
	protected int getHeight(){
		return conf.getBounds().height;
	}

	/**
	 * Sets the buffer according to the specified raster
	 */
	private synchronized void setBuffer(WritableRaster raster){
		buffer = conf.createCompatibleImage(
        		conf.getBounds().width, conf.getBounds().height);
		buffer.setData(raster);
		updated = true;
		doubleBuffer = buffer;
	}

	/**
	 * Gets the buffer
	 */
	protected synchronized BufferedImage getBuffer(){
		//return doubleBuffer;
		if(updated){
			updated=false;
			return buffer;
		}
		return doubleBuffer;
	}

//Raw Encoding Methods********************************************************/

	/**
	 * Copies raw pixel array (which is actually a
	 * rectangle of pixel data) into the buffer.
	 *
	 * @param x x location of origin of pixel rectangle
	 * @param y y location of origin of pixel rectangle
	 * @param w width of pixel rectangle
	 * @param h height of pixel rectangle
	 * @param input array containing pixel data
	 */
	protected void raw(int x, int y, int w,
			int h, int[] input){
		if(rgbs==null){
			rgbs = new
			int[conf.getBounds().width*3][conf.getBounds().height];
		}
		int count = 0;
		label:
		for(int j=y;j<h+y;j++){
			for(int i=(x*3);i<((w*3)+(x*3));i++){
				//System.out.println(input[count]);
				rgbs[i][j] = input[count];
				count++;
				if(count==input.length){
					break label;
				}
			}
		}
	}

	/**
	 * Converts the pixel array into a buffered image
	 */
	protected void convertToBufferedImage(){
		switch(conf.getIdNum()){
		case 0:
			//8bit colormap
			Debug.log(3, "Error: Config not supported");
			break;
		case 1:
			//16bit colormap
			Debug.log(3, "Error: Config not supported");
			break;
		case 2:
			//32bit colormap
			Debug.log(3, "Error: Config not supported");
			break;
		case 3:
			//8bit truecolor
			Debug.log(3, "Error: Config not supported");
			break;
		case 4:
			//16bit truecolor
			Debug.log(3, "Error: Config not supported");
			break;
		case 5:
			//32bit truecolor
			int[] rgbSamples =
				new int[conf.getBounds().width*conf.getBounds().height*3];
			int count = 0;
			for(int j = 0; j < (conf.getBounds().height); j++){
				for(int i = 0; i < (conf.getBounds().width*3); i++){
					rgbSamples[count] = rgbs[i][j];
					count++;
				}
			}
			WritableRaster raster =
				(WritableRaster)conf.createCompatibleRaster();
			raster.setPixels(0, 0, conf.getBounds().width,
					conf.getBounds().height, rgbSamples);
	        setBuffer(raster);
			break;
		default:
			Debug.log(3, "Error: Config not supported");
			break;
		}
	}

//CopyRect Encoding Methods***************************************************/

	/**
	 * implements the copyrect encoding
	 */
	protected synchronized void copyRect(int x, int y, int w, int h,
			int srcx, int srcy){
    	BufferedImage sub = buffer.getSubimage(srcx, srcy, w, h);
    	buffer.setData(sub.getRaster().createTranslatedChild(x, y));
	}

//GraphicsDevice extension****************************************************/

	/**
	 * Returns the type of GraphicsDevice for the Framebuffer
	 */
	@Override
	public int getType() {
		return GraphicsDevice.TYPE_RASTER_SCREEN;
	}

	/**
	 * Returns the ID String for the Framebuffer
	 */
	@Override
	public String getIDstring() {
		return "RFB";
	}

	@Override
	public GraphicsConfiguration[] getConfigurations() {
		return new GraphicsConfiguration[] {conf};
	}

	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return conf;
	}

}
