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

import java.io.IOException;
import java.net.Socket;

import org.sikuli.basics.Debug;

/**
 * The VNC thread object handles connecting to the server and keeping the
 * framebuffer up to date by polling the server for changes.
 *
 * @author Mike Johnson
 */
public class VNCThread extends Thread{

	//keep the connection open or not
	private boolean listen = true;

	//interval to poll VNC server for changes (milliseconds)
	private int pollInterval = 50;

	//protocol message object
	private VNCClient client;
	//local copy of remote framebuffer
	private Framebuffer screen;

	//flag of whether the connection is configured correctly based on what
	//the VNC stack currectly supports
	private boolean pixel_flag = false;

	private boolean connected = false;

	public VNCThread(Socket s){
		client = new VNCClient(s);
		screen = new Framebuffer();
	}

    /**
     * terminates the thread
     */
	public void terminate(){
    	listen = false;
    }

    public VNCClient getClient(){
    	return client;
    }

    public Framebuffer getScreen(){
    	return screen;
    }

    /**
     * Changes the interval at which the VNC Client
     * polls the server for updates.
     *
     * @param milliseconds
     */
    public void changePollInterval(int milliseconds){
    	pollInterval = milliseconds;
    }

    /**
     * This method connects the client to the server and invokes the necessary
     * methods so that the user does not need to worry about the underlying VNC
     * messages.
     *
     * @param connectOthers 1  The client will share the connection with others
     *                      0  The client will not share the connection
     * @param securityType  0 Invalid
     *                      1 None
     *                      2 VNC Authentication
     */
    public void openConnection(int connectOthers, int securityType){

    	try{
    		client.protocolHandshake();
    		client.securityMethod(client.securityInit(securityType));
    		client.clientInit(connectOthers);
    		int[] firstInput = client.listenServerInit();
    		String secondInput = client.readDesktopName();
    		pixel_flag = screen.setPF(firstInput, secondInput);
    		connected = true;
    	}
    	catch(IOException e){
    		Debug.log(3, ""+e);
    	}
    	catch(InterruptedException ie){
    		Debug.log(3, ""+ie);
    	}
    }

    /**
     * The SetPixelFormat method sets the format of the raw pixel data sent
     * across the network by the VNC Server.
     *
     * @param format Can be either "Truecolor" or "Colormap"
     * @param bits The number of bits per pixel
     * @param bef The BigEndianFlag (0 is little endian)
     * @throws IOException Thrown if there is a socket error.
     */
    public void setPixelFormat(String format, int bits, int bef)
    		throws IOException{

        if(format.equals("Truecolor")){

        	Debug.log(3, "Format: "+format);
        	Debug.log(3, "Bits: "+bits);

            switch(bits){
                case 8:
                	int[] a = {8,8,bef,1,7,7,3,5,2,0};
                	client.setPixelFormat(a[0],a[1],a[2],a[3],a[4],
                			a[5],a[6],a[7],a[8],a[9]);
                    screen.resetPF(a);
                    pixel_flag = true;
                    break;
                case 16:
                	int[] b = {16,15,bef,1,31,31,31,10,5,0};
                	client.setPixelFormat(b[0],b[1],b[2],b[3],b[4],
                			b[5],b[6],b[7],b[8],b[9]);
                    screen.resetPF(b);
                	pixel_flag = true;
                    break;
                case 32:
                	int[] c = {32,24,bef,1,255,255,255,16,8,0};
                	client.setPixelFormat(c[0],c[1],c[2],c[3],c[4],
                			c[5],c[6],c[7],c[8],c[9]);
                    screen.resetPF(c);
                	pixel_flag = false;
                    break;
                default:
                	Debug.log(3, "Error: Number of Bits unsupported");
                    pixel_flag = true;
                    break;
            }
        }
        else if(format.equals("Colormap")){
            switch(bits){
                case 8:
                	int[] a = {8,8,bef,1,7,7,3,5,2,0};
                	client.setPixelFormat(a[0],a[1],a[2],a[3],a[4],
                			a[5],a[6],a[7],a[8],a[9]);
                    screen.resetPF(a);
                	pixel_flag = true;
                    break;
                case 16:
                	int[] b = {8,8,bef,1,7,7,3,5,2,0};
                	client.setPixelFormat(b[0],b[1],b[2],b[3],b[4],
                			b[5],b[6],b[7],b[8],b[9]);
                    screen.resetPF(b);
                	pixel_flag = true;
                    break;
                case 32:
                	int[] c = {8,8,bef,1,7,7,3,5,2,0};
                	client.setPixelFormat(c[0],c[1],c[2],c[3],c[4],
                			c[5],c[6],c[7],c[8],c[9]);
                    screen.resetPF(c);
                	pixel_flag = true;
                    break;
                default:
                	Debug.log(3, "Error: Number of Bits unsupported");
                    pixel_flag = true;
                    break;
            }
        }
        else{
        	Debug.log(3, "Error: Format not supported.");
            pixel_flag = true;
        }
    }

    @Override
    public void run(){

    	//if not connected sleep until it is
    	while(!connected){
    		try {
				Thread.sleep(100);
			}
    		catch (InterruptedException e) {
    			Debug.log(3, "Error: Thread Interrupted");
			}
    	}

        try{
        	client.framebufferUpdateRequest( //non-incremental update request
                    pixel_flag,0, (short)0, (short)0,
                    (short)screen.getWidth(), (short)screen.getHeight());

            while (listen){
            	if(client.available()){
            		int message = client.readByte();
                    switch (message){
                        case 0:
                          	client.readByte(); //padding
                          	listenRemoteFramebufferUpdate();
                            break;
                        case 1:
                            //client.listenSetColorMap();
                            break;
                        case 2:
                          	//listenBell();
                            break;
                        case 3:
                         	//client.listenServerCutText();
                            break;
                        default:
                        	Debug.log(3, "Error: Unsupported " +
                            		"Message Type: "+message);
                            break;
                    }
            	}
              	Thread.sleep(pollInterval);

                client.framebufferUpdateRequest( //incremental update request
                        pixel_flag,1, (short)0, (short)0,
                        (short)screen.getWidth(), (short)screen.getHeight());
            }
            client.close();
        }
        catch(IOException io){
        	Debug.log(3, "Error: IO Exception"+io);
        }
        catch (InterruptedException e) {
        	Debug.log(3, "Error: Thread Interrupted");
		}
    }

    /**
     * Listens to the framebuffer update messages from the server
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void listenRemoteFramebufferUpdate()
    		throws IOException, InterruptedException{

    	int numRect,x,y,w,h,encType;

        numRect = client.readShort();

        for(int i = 0; i < numRect; i++){
        	x = client.readShort();
            y = client.readShort();
            w = client.readShort();
            h = client.readShort();
            encType = client.readInt();
            switch(encType){
            	case 0://raw encoding
                    int[] pixels = client.readTC32Data(w*h*3);
                    screen.raw(x, y, w, h, pixels);
                    break;
            	case 1://copy rect encoding
            		int srcX = client.readShort();
            		int srcY = client.readShort();
            		screen.copyRect(x,y,w,h,srcX,srcY);
            		break;
            	default:
            		Debug.log(3, "Error: Encoding type not recognized " +
            				"or supported: "+Integer.toString(encType));
            		break;
            }
        }
        screen.convertToBufferedImage();
    }

/*Unimplemented Methods------------------------------------------------------*/

    /*
    private void listenBell(){
        Debug.log(3, "Ring");
    }

    private void listenSetColorMap()throws IOException{
        dataIn.readUnsignedByte();
        int first=dataIn.readUnsignedShort();
        int num = dataIn.readUnsignedShort();
        Debug.log(3, first+" first");
        Debug.log(3, num+" num");
        for(int i=0;i<num;i++){
            int r = dataIn.readUnsignedShort();
            int g = dataIn.readUnsignedShort();
            int b = dataIn.readUnsignedShort();
        }
    }

    private String listenServerCutText() throws IOException{
        dataIn.readUnsignedByte();
        dataIn.readUnsignedByte(); //padding
        dataIn.readUnsignedByte();
        int length = dataIn.readInt();
        byte[] b = new byte[length];
        dataIn.read(b);
        return new String(b, "ISO-8859-1");
    }*/
}
