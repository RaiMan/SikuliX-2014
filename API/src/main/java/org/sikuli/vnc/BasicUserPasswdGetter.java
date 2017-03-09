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

import com.tigervnc.rfb.UserPasswdGetter;

/**
 * Simple implementation of UserPasswdGetter that returns a fixed password.
 */
class BasicUserPasswdGetter implements UserPasswdGetter
{
    private final String password;

    public BasicUserPasswdGetter(String password)
    {
        this.password = password;
    }

    @Override
    public boolean getUserPasswd(StringBuffer user, StringBuffer passwd)
    {
        if (user != null) {
            user.setLength(0);
        }

        if (password != null) {
            passwd.setLength(0);
            passwd.append(password);

            return true;
        } else {
            return false;
        }
    }
}
