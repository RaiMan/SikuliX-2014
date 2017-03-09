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

import com.tigervnc.rfb.PixelBuffer;
import com.tigervnc.rfb.PixelFormat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteOrder;

/**
 * An off-screen frame buffer that can be used to capture screen contents.
 */
class VNCFrameBuffer extends PixelBuffer
{
    private final Object imageLock = new Object();
    private BufferedImage image;
    private DataBuffer db;

    public VNCFrameBuffer(int width, int height, PixelFormat serverPF)
    {
        PixelFormat nativePF = this.getNativePF();
        if (nativePF.depth > serverPF.depth) {
            this.setPF(serverPF);
        } else {
            this.setPF(nativePF);
        }
        this.resize(width, height);
    }

    public void resize(int width, int height)
    {
        if (width != this.width() || height != this.height()) {
            this.width_ = width;
            this.height_ = height;
            this.createImage(width, height);
        }
    }

    private PixelFormat getNativePF()
    {
        return new PixelFormat(
                32,
                24,
                ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN,
                true,
                255,
                255,
                255,
                16,
                8,
                0
        );
    }

    public void setColourMapEntries(int offset, int nbColors, int[] rgb)
    {
        throw new RuntimeException("Not supported yet");
    }

    private void createImage(int width, int height)
    {
        synchronized (imageLock) {
            if (width != 0 && height != 0) {
                WritableRaster raster = this.cm.createCompatibleWritableRaster(width, height);
                this.image = new BufferedImage(this.cm, raster, false, null);
                this.db = raster.getDataBuffer();
            }
        }
    }

    public void fillRect(int x, int y, int w, int h, int pixelValue)
    {
        synchronized (imageLock) {
            Graphics2D g2d = this.image.createGraphics();
            switch (this.format.depth) {
                case 24:
                    g2d.setColor(new Color(pixelValue));
                    g2d.fillRect(x, y, w, h);
                    break;
                default:
                    g2d.setColor(new Color(0xff000000 | this.cm.getRed(pixelValue) << 16 | this.cm.getGreen(pixelValue) << 8 | this.cm.getBlue(pixelValue)));
                    g2d.fillRect(x, y, w, h);
            }

            g2d.dispose();
        }
    }

    public void imageRect(int x, int y, int w, int h, Object p)
    {
        if (p instanceof Image) {
            Image img = (Image) p;
            synchronized (imageLock) {
                Graphics2D g2d = this.image.createGraphics();
                g2d.drawImage(img, x, y, w, h, null);
                g2d.dispose();
            }
            img.flush();
        } else {
            synchronized (imageLock) {
                SampleModel sampleModel = this.image.getSampleModel();
                if (sampleModel.getTransferType() == DataBuffer.TYPE_BYTE) {
                    byte[] byteData = new byte[((int[]) p).length];

                    for (int i = 0; i < byteData.length; ++i) {
                        byteData[i] = (byte) ((int[]) p)[i];
                    }

                    p = byteData;
                }

                sampleModel.setDataElements(x, y, w, h, p, this.db);
            }
        }

    }

    public void copyRect(int dx, int dy, int w, int h, int sx, int sy)
    {
        synchronized (imageLock) {
            Graphics2D g2d = this.image.createGraphics();
            g2d.copyArea(sx, sy, w, h, dx - sx, dy - sy);
            g2d.dispose();
        }
    }

    public BufferedImage getImage(int x, int y, int w, int h)
    {
        BufferedImage i;
        synchronized (imageLock) {
            i = new BufferedImage(image.getColorModel(), image.getColorModel().createCompatibleWritableRaster(w, h), false, null);
            Graphics2D g2d = i.createGraphics();
            g2d.drawImage(image, 0, 0, w, h, x, y, w, h, null);
            g2d.dispose();
        }
        return i;
    }
}
