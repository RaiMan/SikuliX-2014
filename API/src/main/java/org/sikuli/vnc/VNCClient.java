/* Copyright (c) 2017, Sikuli.org, sikulix.com
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package org.sikuli.vnc;

import com.tigervnc.network.TcpSocket;
import com.tigervnc.rdr.FdInStreamBlockCallback;
import com.tigervnc.rfb.*;
import com.tigervnc.rfb.Exception;
import com.tigervnc.rfb.Point;
import com.tigervnc.vncviewer.CConn;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

class VNCClient extends CConnection implements FdInStreamBlockCallback, Closeable
{
    static ThreadLocal<UserPasswdGetter> UPG = new ThreadLocal<>();

    static {
        CConn.upg = new UserPasswdGetter()
        {
            @Override
            public boolean getUserPasswd(StringBuffer stringBuffer, StringBuffer stringBuffer1)
            {
                UserPasswdGetter upg = UPG.get();
                return false;
            }
        };
    }

    private final TcpSocket sock;
    private boolean shuttingDown = false;
    private PixelFormat serverPF;
    private int currentEncoding;
    private VNCFrameBuffer frameBuffer;

    public static VNCClient connect(String address, int port, String password, boolean shareConnection) throws IOException
    {
        VNCClient client = new VNCClient(address, port, password, shareConnection);
        while (client.state() != VNCClient.RFBSTATE_NORMAL) {
            client.processMsg();
        }
        return client;
    }

    private VNCClient(String address, int port, final String password, boolean shareConnection) throws IOException
    {
        this.security = new ThreadLocalSecurityClient(new BasicUserPasswdGetter(password));

        this.currentEncoding = Encodings.encodingTight;
        this.setShared(shareConnection);

        setServerName(address);
        setServerPort(port);
        this.sock = new TcpSocket(this.getServerName(), this.getServerPort());
        this.sock.inStream().setBlockCallback(this);
        this.setStreams(this.sock.inStream(), this.sock.outStream());
        this.initialiseProtocol();
    }

    @Override
    public PixelFormat getPreferredPF()
    {
        return new PixelFormat();
    }

    @Override
    public void serverInit()
    {
        super.serverInit();

        this.serverPF = this.cp.pf();
        this.frameBuffer = new VNCFrameBuffer(this.cp.width, this.cp.height, this.serverPF);

        this.writer().writeSetEncodings(this.currentEncoding, true);
    }

    public void setDesktopSize(int var1, int var2)
    {
        super.setDesktopSize(var1, var2);
        this.resizeFramebuffer();
    }

    public void setColourMapEntries(int offset, int nbColors, int[] rgb) {
        frameBuffer.setColourMapEntries(offset, nbColors, rgb);
    }

    private void resizeFramebuffer()
    {
        if (this.frameBuffer != null) {
            if (this.cp.width != 0 || this.cp.height != 0) {
                if (this.frameBuffer.width() != this.cp.width || this.frameBuffer.height() != this.cp.height) {
                    this.frameBuffer.resize(cp.width, cp.height);
                }
            }
        }
    }

    public void refreshFramebuffer()
    {
        refreshFramebuffer(0, 0, cp.width, cp.height, false);
    }

    /**
     * Sends FramebufferUpdateRequest message to server.
     *
     * @param x           X coordinate of desired region
     * @param y           Y coordinate of desired region
     * @param w           Width of desired region
     * @param h           Height of desired region
     * @param incremental Zero sends entire desktop, One sends changes only.
     * @throws IOException If there is a socket error
     */
    public void refreshFramebuffer(int x, int y, int w, int h, boolean incremental)
    {
        writer().writeFramebufferUpdateRequest(new Rect(x, y, w, h), incremental);
    }

    @Override
    public void framebufferUpdateStart()
    {
        refreshFramebuffer(0, 0, cp.width, cp.height, true);
    }

    @Override
    public void framebufferUpdateEnd()
    {
    }

    public void fillRect(Rect r, int p)
    {
        this.frameBuffer.fillRect(r.tl.x, r.tl.y, r.width(), r.height(), p);
    }

    public void imageRect(Rect r, Object p)
    {
        this.frameBuffer.imageRect(r.tl.x, r.tl.y, r.width(), r.height(), p);
    }

    public void copyRect(Rect r, int sx, int sy)
    {
        this.frameBuffer.copyRect(r.tl.x, r.tl.y, r.width(), r.height(), sx, sy);
    }

    /**
     * Tells VNC server to depress key.
     *
     * @param key X Window System Keysym for key.
     * @throws IOException If there is a socket error.
     */
    protected void keyDown(int key) throws IOException
    {
        writer().writeKeyEvent(key, true);
    }

    /**
     * Tells VNC server to release key.
     *
     * @param key X Window System Keysym for key.
     * @throws IOException If there is a socket error.
     */
    protected void keyUp(int key) throws IOException
    {
        writer().writeKeyEvent(key, false);
    }

    /**
     * Tells VNC server to perform a mouse event. bOne through bEight are mouse
     * buttons one through eight respectively.  A zero means release that
     * button, and a one means depress that button.
     *
     * @param buttonState logical or of BUTTON_N_DOWN
     * @param x           X coordinate of action
     * @param y           Y coordinate of action
     * @throws IOException If there is a socket error.
     */
    protected void mouseEvent(int buttonState, int x, int y) throws IOException
    {
        writer().writePointerEvent(new Point(x, y), buttonState);
    }

    /**
     * Closes the connection
     *
     * @throws IOException
     */
    public void close() throws IOException
    {
        this.shuttingDown = true;

        if (this.sock != null) {
            this.sock.shutdown();
        }
    }

    /**
     * Returns the VNCClient Object as a string
     */
    public String toString()
    {
        return "VNCClient: " + getServerName() + ":" + getServerPort();
    }

    public Rectangle getBounds()
    {
        return new Rectangle(0, 0, this.cp.width, this.cp.height);
    }

    public BufferedImage getFrameBuffer(int x, int y, int w, int h)
    {
        return frameBuffer.getImage(x, y, w, h);
    }

    @Override
    public void blockCallback()
    {
        try {
            synchronized (this) {
                this.wait(1L);
            }
        } catch (InterruptedException var4) {
            throw new Exception(var4.getMessage());
        }
    }

    public void processMessages()
    {
        while (!shuttingDown) {
            processMsg();
        }
    }

}
