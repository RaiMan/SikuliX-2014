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
 * This listener is used to handle hotkey events from externally.
 *
 * @author subes
 */
public interface HotkeyListener {

    /**
     * This method receives the hotkey events which are received by the main listen loop.
     *
     * @param id
     */
    void onHotkey(int id);
}
