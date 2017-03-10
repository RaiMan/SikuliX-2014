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

import com.tigervnc.rfb.CConnection;
import com.tigervnc.rfb.CSecurity;
import com.tigervnc.rfb.SecurityClient;
import com.tigervnc.rfb.UserPasswdGetter;
import com.tigervnc.vncviewer.CConn;

import java.lang.reflect.Field;

/**
 * Workaround for static state in TigerVNC.
 *
 * The UserPasswdGetter used by TigerVNC is a static field (CConn.upg).
 * In SikuliX need to be able to specify a password per connection without requesting user input.
 * To do that we set the global UserPasswdGetter to one that retrieves the actual UserPasswdGetter
 * instance from a thread-local variable.
 * This extension of SecurityClient sets the thread-local variable to the correct value just before
 * it is going to be requested by the TigerVNC code.
 */
class ThreadLocalSecurityClient extends SecurityClient
{
    private static final ThreadLocal<UserPasswdGetter> UPG = new ThreadLocal<>();
    static {
        CConn.upg = new UserPasswdGetter()
        {
            @Override
            public boolean getUserPasswd(StringBuffer user, StringBuffer pass)
            {
                UserPasswdGetter upg = UPG.get();
                if (upg != null) {
                    return upg.getUserPasswd(user, pass);
                } else {
                    if (user != null) {
                        user.setLength(0);
                    }
                    if (pass != null) {
                        pass.setLength(0);
                    }
                    return false;
                }
            }
        };
    }

    private final UserPasswdGetter upg;

    ThreadLocalSecurityClient(UserPasswdGetter userPasswdGetter)
    {
        upg = userPasswdGetter;
        try {
            // An assertion fails is the SecurityClient#msg is null
            // I couldn't find any other way to set it than via reflection.
            Field field = SecurityClient.class.getDeclaredField("msg");
            field.setAccessible(true);
            field.set(this, "");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CSecurity GetCSecurity(int securityType)
    {
        final CSecurity security = super.GetCSecurity(securityType);
        if (security != null) {
            return new CSecurity()
            {
                @Override
                public boolean processMsg(CConnection cConnection)
                {
                    UPG.set(upg);
                    return security.processMsg(cConnection);
                }

                @Override
                public int getType()
                {
                    return security.getType();
                }

                @Override
                public String description()
                {
                    return security.description();
                }
            };
        } else {
            return null;
        }
    }
}
