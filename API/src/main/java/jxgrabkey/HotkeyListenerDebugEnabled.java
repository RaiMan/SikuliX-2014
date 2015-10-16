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
 * This listener handles debug messages aswell as hotkey events.
 * It can be used for custom logging needs.
 *
 * @author subes
 */
public interface HotkeyListenerDebugEnabled extends HotkeyListener {
    
    /**
     * This method is used to handle debug messages from JXGrabKey.
     * You need to enable debug to receive those.
     * 
     * @param debugMessage
     */
    void debugCallback(String debugMessage);
}
