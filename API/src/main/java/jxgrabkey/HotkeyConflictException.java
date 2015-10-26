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

/**
 * This Exception is thrown when another application already registered a hotkey
 * which JXGrabKey tried to register for itself.
 * X11 hotkeys can only be registered by one application at a time.
 * Because JXGrabKey registers the same hotkey with different combinations of
 * offending masks like scrolllock, numlock and capslock,
 * any of those registrations can be the cause of the conflict.
 * It is best to unregister the hotkey after receiving this exception.
 * Otherwise the hotkey may not work at all, or may not work with all mask combinations.
 *
 * @author subes
 */
public class HotkeyConflictException extends Exception {

    public HotkeyConflictException() {
        super();
    }

    public HotkeyConflictException(String message) {
        super(message);
    }

    public HotkeyConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public HotkeyConflictException(Throwable cause) {
        super(cause);
    }
}
