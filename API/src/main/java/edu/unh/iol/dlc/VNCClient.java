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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.sikuli.basics.Debug;

/**
 * The VNCClient class controls all of the messages between
 * the client and the server.
 *
 * @author Mike Johnson
 */
public class VNCClient {

  /*
     * Below are the fields and objects associated with the handshaking phase
     */
  private BufferedWriter out = null;
  private BufferedReader in = null;
  private DataOutputStream dataOut = null;
  private DataInputStream dataIn = null;
  private int version = 0;
  private Socket socket = null;
  private int bef;

  /**
   * Constructor
   *
   * @param soc Socket to be used in VNC connection
   */
  public VNCClient(Socket soc) {
    socket = soc;
    try {
      socket.setTcpNoDelay(true);
      out = new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), "US-ASCII"));
      in = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), "US-ASCII"));
      dataOut = new DataOutputStream(
              new BufferedOutputStream(socket.getOutputStream()));
      dataIn = new DataInputStream(
              new BufferedInputStream(socket.getInputStream()));
    } catch (IOException e) {
      Debug.log(-1, "Error: IO Exception" + e);
    }
  }

//Handshaking Phase*****************************************************************************/

    /* Below are all of the methods associated with the establishing the VNC
     * Connection.
     */

  /**
   * This method reads the Protocol Version message from the server and then
   * selects the highest protocol version supported by the server.  If an
   * unrecognized protocol version is sent, the client closes the connection.
   * The encoding of the Protocol Version message is 7-bit ASCII.
   */
  protected void protocolHandshake() throws IOException {
    String protocolVersion = in.readLine();
    VersionParser parser = new VersionParser(protocolVersion);
    final ProtocolVersion parse = parser.parse();
    final String replyCode = parse.getReplyCode();
    out.write(replyCode + "\n");
    out.flush();
    version = Character.getNumericValue(replyCode.charAt(10));
  }

  /**
   * This method handles the initial security message exchanges between the
   * client and the server.  If RFB 3.3 is used the server will select the
   * security type and tell the client.  Otherwise the server will list the
   * security types that it supports and the client will select the one it
   * wants to use. If there is an error the server tells the client and the
   * client prints the reason to the standard error.
   *
   * @param desiredSecurityType The desired security type as defined by
   *                            the standard.
   * @return selectedType If there is an IOerror the method returns a -1,
   * otherwise it returns the security type selected
   * for use.
   */
  protected int securityInit(int desiredSecurityType) throws IOException {
    int selectedType = 0;
    byte[] securityTypes = new byte[10];
    if (version >= 7) {
      int numSecurityTypes = (int) dataIn.readByte();
      if (numSecurityTypes == 0) {
        Debug.log(-1, "Error: Server reported" +
                " an error, closing connection");
        socket.close();
      }
      dataIn.read(securityTypes, 0, numSecurityTypes);
      boolean flag = false;
      for (int i = 0; i < securityTypes.length; i++) {
        if (securityTypes[i] == desiredSecurityType) {
          flag = true;
          dataOut.write((byte) desiredSecurityType);
          dataOut.flush();
          break;
        }
      }
      if (flag) {
        selectedType = desiredSecurityType;
        return selectedType;
      }
      Debug.log(-1, "Error: Desired Security Type" +
              " Not supported by Server, closing connection");
      socket.close();
    } else {
      selectedType = dataIn.readInt();
      return selectedType;
    }
    return selectedType;
  }

  /**
   * Method that takes the selected security type and calls the necessary
   * methods involved with that security type.
   *
   * @param type The security type to be used as defined in the standard
   */
  protected void securityMethod(int type) {
    switch (type) {
      case 0:
        try {
          Debug.log(-1, "Error: Server" +
                  " reported an error, closing connection");
          socket.close();
        } catch (IOException e) {
          Debug.log(-1, "Error: IO Exception" + e);
        }
        break;
      case 1:
        if (version == 8) {
          securityResult();
        }
        break;
      case 2:
                /* TODO: Add VNC Authentication.
                 * From the 3.8 standard:
                 * "VNC authentication is to be used and protocol data is to be
                 * sent unencrypted. The server sends a random 16-byte
                 * challenge,the client encrypts the challenge with DES using
                 * a password supplied by the user as the key, and sends the
                 * resulting 16-byte response.
                 * The protocol continues with the SecurityResult message."
                 */
        //securityResult();
        break;
      default:
        try {
          Debug.log(-1, "Error: Desired Security" +
                  " Type Not supported, closing connection");
          socket.close();
        } catch (IOException e) {
          Debug.log(-1, "Error: IO Exception" + e);
        }
        break;
    }
  }

  /**
   * Handles the server's response to how the security handshaking went.
   */
  protected void securityResult() {
    try {
      int securityResult = dataIn.readInt();
      if (securityResult == 1) {
        Debug.log(3,
                "Error: Server reported an error, closing connection");
        socket.close();
      }
    } catch (IOException e) {
      Debug.log(-1, "Error: IO Exception" + e);
    }
  }

  /**
   * Method that tells the server whether to share
   * the connection with others.
   *
   * @param share 1 Share the connection with other clients
   *              0 Do not share the connection
   */
  protected void clientInit(int share) throws IOException,
          InterruptedException {
    dataOut.writeByte(share);
    dataOut.flush();
  }

  /**
   * Method that listens to the ServerInit message and records information
   * about the framebuffer.
   *
   * @return framebuffer information
   */
  protected synchronized int[] listenServerInit() throws IOException,
          InterruptedException {
    int[] data = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    data[0] = dataIn.readUnsignedShort();  //width
    data[1] = dataIn.readUnsignedShort();  //height
    data[2] = dataIn.readUnsignedByte();   //bitsPerPixel
    data[3] = dataIn.readUnsignedByte();   //depth
    data[4] = dataIn.readUnsignedByte();   //bigEndianFlag
    bef = data[4]; //for use later
    data[5] = dataIn.readUnsignedByte();   //trueColorFlag
    data[6] = dataIn.readUnsignedShort();  //redMax
    data[7] = dataIn.readUnsignedShort();  //greenMax
    data[8] = dataIn.readUnsignedShort();  //blueMax
    data[9] = dataIn.readUnsignedByte();   //redShift
    data[10] = dataIn.readUnsignedByte();   //greenShift
    data[11] = dataIn.readUnsignedByte();   //blueShift
    dataIn.readUnsignedByte();
    dataIn.readUnsignedByte(); //padding
    dataIn.readUnsignedByte();
    return data;
  }

  /**
   * Method that reads the desktop name of the remote host.
   *
   * @return The name of the desktop
   * @throws IOException
   */
  protected synchronized String readDesktopName() throws IOException {
    int nameLength = dataIn.readInt();
    byte[] stringBytes = new byte[nameLength];
    dataIn.read(stringBytes); //desktopName
    return new String(stringBytes, "UTF-8");
  }

//VNC Messages****************************************************************/

    /* Below are all of the methods the VNC Client can perform once connected.
     */

  /**
   * The SetPixelFormat message sets the format of the raw pixel data sent
   * across the network by the VNC Server. See RFB 3.8 standard for
   * SelPixelFormat message.
   *
   * @param bpp   The number of bits per pixel
   * @param Depth The number of bits used for data
   * @param be    The big endian flag
   * @param tcf   The true color flag
   * @param rm    The maximum red value
   * @param gm    The maximum blue value
   * @param bm    The maximum green value
   * @param rs    The red shift value
   * @param gs    The green shift value
   * @param bs    The blue shift value
   * @throws IOException If there is a socket error.
   */
  protected void setPixelFormat(int bpp, int Depth, int be, int tcf, int rm,
                                int gm, int bm, int rs, int gs, int bs) throws IOException {
    dataOut.writeByte(0); //message identifier
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(bpp);
    dataOut.writeByte(Depth);
    dataOut.writeByte(be);
    dataOut.writeByte(tcf);
    dataOut.writeShort(rm);
    dataOut.writeShort(gm);
    dataOut.writeShort(bm);
    dataOut.writeByte(rs);
    dataOut.writeByte(gs);
    dataOut.writeByte(bs);
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    bef = be;
    dataOut.flush();
  }

  /**
   * Sets the encoding of the pixel data sent by the server. See standard
   * for details of encoding type.
   *
   * @param numberOfEncodings The number of encodings the server supports
   * @param encoding          The value representing an encoding type.
   * @throws IOException If there is a socket error.
   */
  protected void setEncodings(short numberOfEncodings,
                              int... encoding) throws IOException {
    dataOut.writeByte(2); //message identifier
    dataOut.writeByte(0); //padding
    dataOut.writeShort(numberOfEncodings);
    for (int index : encoding) {
      dataOut.writeInt(index);
    }
    dataOut.flush();
  }

  /**
   * Sends FramebufferUpdateRequest message to server.
   *
   * @param incremental Zero sends entire desktop, One sends changes only.
   * @param x           X coordinate of desired region
   * @param y           Y coordinate of desired region
   * @param w           Width of desired region
   * @param h           Height of desired region
   * @throws IOException If there is a socket error
   */
  protected void framebufferUpdateRequest(boolean flag,
                                          int incremental, short x, short y,
                                          short w, short h) throws IOException {
    if (flag == true) {
      Debug.log(-1, "Error: SetPixelFormat Required.");
      return;
    }
    dataOut.writeByte(3); //message identifier
    dataOut.writeByte(incremental);
    dataOut.writeShort(x);
    dataOut.writeShort(y);
    dataOut.writeShort(w);
    dataOut.writeShort(h);
    dataOut.flush();
  }

  /**
   * Tells VNC server to depress key.
   *
   * @param key X Window System Keysym for key.
   * @throws IOException If there is a socket error.
   */
  protected void keyDown(int key) throws IOException {
    dataOut.writeByte(4); //message identifier
    dataOut.writeByte(1); //key down flag
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeInt(key);
    dataOut.flush();
    Debug.log(4, "Writing key down-" + Integer.toHexString(key));
  }

  /**
   * Tells VNC server to release key.
   *
   * @param key X Window System Keysym for key.
   * @throws IOException If there is a socket error.
   */
  protected void keyUp(int key) throws IOException {
    dataOut.writeByte(4); //message identifier
    dataOut.writeByte(0); //key up flag
    dataOut.writeByte(0); //padding
    dataOut.writeByte(0); //padding
    dataOut.writeInt(key);
    dataOut.flush();
    Debug.log(4, "Writing key up-" + Integer.toHexString(key));
  }

  /**
   * Tells VNC server to perform a mouse event. bOne through bEight are mouse
   * buttons one through eight respectively.  A zero means release that
   * button, and a one means depress that button.
   *
   * @param bOne   Button One
   * @param bTwo   Button Two
   * @param bThree Button Three
   * @param bFour  Button Four
   * @param bFive  Button Five
   * @param bSix   Button Six
   * @param bSeven Button Seven
   * @param bEight Button Eight
   * @param x      X coordinate of action
   * @param y      Y coordinate of action
   * @throws IOException If there is a socket error.
   */
  protected void mouseEvent(int bOne, int bTwo, int bThree,
                            int bFour, int bFive, int bSix, int bSeven,
                            int bEight, int x, int y) throws IOException {
    int[] buttons = {bOne, bTwo, bThree, bFour,
            bFive, bSix, bSeven, bEight};
    byte flag = 0;
    for (int i = 0; i < 8; i++) {
      flag += buttons[i] << (i);
    }
    dataOut.writeByte(5);
    dataOut.writeByte(flag);
    dataOut.writeShort(x);
    dataOut.writeShort(y);
    dataOut.flush();
    Debug.log(4, "MouseEvent-" + Byte.toString(flag));
  }

  /**
   * TODO: Method to cut and paste text over VNC connection with
   *
   * @param text
   * @throws IOException
   */
  protected void clientPasteText(String text) throws IOException {
    byte[] b = text.getBytes("ISO-8859-1");
    dataOut.writeByte(6);
    dataOut.writeByte(0);
    dataOut.writeByte(0);
    dataOut.writeByte(0);
    dataOut.writeInt(b.length);
    for (int i = 0; i < b.length; i++) {
      dataOut.writeByte(b[i]);
    }
    dataOut.flush();
  }

  /**
   * Reads a single unsigned byte off of the wire
   *
   * @return int the unsigned byte
   * @throws IOException
   */
  protected synchronized int readByte() throws IOException {
    return dataIn.readUnsignedByte();
  }

  /**
   * Reads an unsigned short off of the wire
   *
   * @return int the unsigned short
   * @throws IOException
   */
  protected synchronized int readShort() throws IOException {
    return dataIn.readUnsignedShort();
  }

  /**
   * Reads an int off of the wire
   *
   * @return int the int
   * @throws IOException
   */
  protected synchronized int readInt() throws IOException {
    return dataIn.readInt();
  }

  /**
   * Reads truecolor 32 bit data off of the wire
   *
   * @param length of the data
   * @return int[] the data
   * @throws IOException
   * @throws InterruptedException
   */
  protected synchronized int[] readTC32Data(int length) throws IOException, InterruptedException {
    int[] input = new int[length];
    if (bef > 0) { //big endian
      for (int j = 0; j < length; j += 3) {
        dataIn.readUnsignedByte();
        input[j] = dataIn.readUnsignedByte();
        input[j + 1] = dataIn.readUnsignedByte();
        input[j + 2] = dataIn.readUnsignedByte();
      }
    } else { //little endian
      for (int j = 0; j < length; j += 3) {
        input[j + 2] = dataIn.readUnsignedByte();
        input[j + 1] = dataIn.readUnsignedByte();
        input[j] = dataIn.readUnsignedByte();
        dataIn.readUnsignedByte();
      }
    }
    return input;
  }

  /**
   * Closes the connection
   *
   * @throws IOException
   */
  protected void close() throws IOException {
    socket.close();
  }

  /**
   * Returns true if there is data waiting to be read from the socket
   *
   * @return
   * @throws IOException
   */
  protected boolean available() throws IOException {
    if (dataIn.available() > 0) {
      return true;
    }
    return false;
  }

  /**
   * Returns the VNCClient Object as a string
   */
  public String toString() {
    return "VNCClient: Socket: " + socket.toString();
  }
}
