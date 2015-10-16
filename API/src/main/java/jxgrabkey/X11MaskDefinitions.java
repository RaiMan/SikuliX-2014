/*  Copyright 2008  Edwin Stang (edwinstang@gmail.com),
 *
 *  This file is part of JXGrabKey.
 *
 *  JXGrabKey is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JXGrabKey is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JXGrabKey.  If not, see <http://www.gnu.org/licenses/>.
 */
package jxgrabkey;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * This class holds definitions for X11 masks. It can also convert AWT masks into X11 masks.
 *
 * @author subes
 */
public final class X11MaskDefinitions {

    public static final int X11_SHIFT_MASK = 1 << 0;
    public static final int X11_LOCK_MASK = 1 << 1;
    public static final int X11_CONTROL_MASK = 1 << 2;
    public static final int X11_MOD1_MASK = 1 << 3;
    public static final int X11_MOD2_MASK = 1 << 4;
    public static final int X11_MOD3_MASK = 1 << 5;
    public static final int X11_MOD4_MASK = 1 << 6;
    public static final int X11_MOD5_MASK = 1 << 7;

    private X11MaskDefinitions() {
    }

    /**
     * Converts an AWT mask into a X11 mask.
     *
     * @param awtMask
     * @return
     */
    public static int awtMaskToX11Mask(int awtMask) {

        int x11Mask = 0;

        if ((awtMask & InputEvent.SHIFT_MASK) != 0 || (awtMask & InputEvent.SHIFT_DOWN_MASK) != 0) {
            x11Mask |= X11MaskDefinitions.X11_SHIFT_MASK;
        }
        if ((awtMask & InputEvent.ALT_MASK) != 0 || (awtMask & InputEvent.ALT_DOWN_MASK) != 0) {
            x11Mask |= X11MaskDefinitions.X11_MOD1_MASK;
        }
        if ((awtMask & InputEvent.CTRL_MASK) != 0 || (awtMask & InputEvent.CTRL_DOWN_MASK) != 0) {
            x11Mask |= X11MaskDefinitions.X11_CONTROL_MASK;
        }
        if ((awtMask & InputEvent.META_MASK) != 0 || (awtMask & InputEvent.META_DOWN_MASK) != 0) {
            x11Mask |= X11MaskDefinitions.X11_MOD2_MASK;
        }
        if ((awtMask & InputEvent.ALT_GRAPH_MASK) != 0 || (awtMask & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            x11Mask |= X11MaskDefinitions.X11_MOD5_MASK;
        }

        return x11Mask;
    }
}
