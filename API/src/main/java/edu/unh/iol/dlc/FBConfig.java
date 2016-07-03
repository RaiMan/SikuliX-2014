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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.sikuli.basics.Debug;

/**
 * The FBConfig class stores configuration information about the
 * framebuffer
 *
 * @author Mike Johnson
 *
 */
public class FBConfig extends GraphicsConfiguration {

	private int width = 0;
	private int height = 0;
	private int bitsPerPixel = 0;
	private int depth = 0;
	private int bigEndianFlag = 0;
	private int trueColorFlag = 0;
	private int redMax = 0;
	private int greenMax = 0;
	private int blueMax = 0;
	private int redShift = 0;
	private int greenShift = 0;
	private int blueShift = 0;
	private Rectangle bounds;
	private String desktopName = null;
	private int idnum;
	private ColorModel cm;
	private DataBuffer db;

	/**
	 * Constructor
	 *
	 * @param config the configuration information
	 * @param name the remote desktop name
	 */
	public FBConfig(int[] config, String name){
		width = config[0];
		height = config[1];
		bitsPerPixel = config[2];
		depth = config[3];
		bigEndianFlag = config[4];
		trueColorFlag = config[5];
		redMax = config[6];
		greenMax = config[7];
		blueMax = config[8];
		redShift = config[9];
		greenShift = config[10];
		blueShift = config[11];
		desktopName = name;
		bounds = new Rectangle(0,0,width,height);

		Debug.log(3, "VNC Server found: %s (%dx%d)", desktopName, width, height);
		for(int i : config){
			Debug.log(4, ""+i);
		}
		Debug.log(4, "Name: "+desktopName);

		switch(bitsPerPixel){
			case 8:
				if(trueColorFlag!=0){
					idnum=3;
					Debug.log(-1, "Unsupported bits per pixel (8), setPixelFormatRequired");
				}
				else{
					idnum=0;
					Debug.log(-1, "Unsupported bits per pixel (8), setPixelFormatRequired");
				}
				break;
			case 16:
				if(trueColorFlag!=0){
					idnum=4;
					Debug.log(-1, "Unsupported bits per pixel (16), setPixelFormatRequired");
				}
				else{
					idnum=1;
					Debug.log(-1, "Unsupported bits per pixel (16), setPixelFormatRequired");
				}
				break;
			case 32:
				if(trueColorFlag!=0){
					idnum=5;
					int[] bitMasks = new int[3];
	    			bitMasks[0] = 0x00ff0000; //red mask-8 bits
	    			bitMasks[1] = 0x0000ff00; //green mask-8 bits
	    			bitMasks[2] = 0x000000ff; //blue mask-8 bits
	    			cm = new DirectColorModel(32,bitMasks[0],bitMasks[1],bitMasks[2]);
	    			db = new DataBufferInt(width*height);
				}
				else{
					idnum=2;
					Debug.log(-1, "Unsupported bits per pixel (32 noTC), setPixelFormatRequired");
				}
				break;
			default:
				Debug.log(-1, "Unsupported bits per pixel (??), setPixelFormatRequired");
				break;
		}
	}

	/**
	 * Creates a compatible raster basted on this configuration
	 *
	 * @return raster
	 */
	public Raster createCompatibleRaster(){
		switch(idnum){
			case 0:
				return null;
			case 1:
				return null;
			case 2:
				return null;
			case 3:
				return null;
			case 4:
				return null;
			case 5:
				int[] bitMasks = new int[3];
    			bitMasks[0] = 0x00ff0000; //red mask-8 bits
    			bitMasks[1] = 0x0000ff00; //green mask-8 bits
    			bitMasks[2] = 0x000000ff; //blue mask-8 bits
				return WritableRaster.createPackedRaster(
	    				db, width, height, width, bitMasks, null);
			default:
				return null;
		}
	}

	/**
	 * Gets the id number of the configuration
	 *
	 * @return num 0 colormap 8 bit
	 * 			   1 colormap 16 bit
	 * 			   2 colormap 32 bit
	 * 			   3 truecolor 8 bit
	 * 			   4 truecolor 16 bit
	 * 			   5 truecolor 32 bit
	 */
	public int getIdNum(){
		return idnum;
	}

	/**
	 * Gets the name of the remote desktop
	 *
	 * @return name
	 */
	public String getName(){
		return desktopName;
	}

	@Override
	public GraphicsDevice getDevice() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Creates a compatible image with this configuration based off
	 * a width and height
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		switch(idnum){
			case 0:
				return null;
			case 1:
				return null;
				//return new BufferedImage(width, height, BufferedImage.TYPE_CUSTOM);
			case 2:
				return null;
				//return new BufferedImage(width, height, BufferedImage.TYPE_CUSTOM);
			case 3:
				return null;
				//return new BufferedImage(width, height, BufferedImage.TYPE_CUSTOM);
			case 4:
				return new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
			case 5:
				return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			default:
				return new BufferedImage(width, height, BufferedImage.TYPE_CUSTOM);
		}
	}

	/**
	 * Gets the colormodel of this configuration
	 */
	@Override
	public ColorModel getColorModel() {
		return cm;
	}

	/**
	 * Gets the colormodel of this configuration
	 */
	@Override
	public ColorModel getColorModel(int transparency) {
		return cm;
	}

	@Override
	public AffineTransform getDefaultTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AffineTransform getNormalizingTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the bounds of the configuration
	 */
	@Override
	public Rectangle getBounds() {
		return bounds;
	}

}
