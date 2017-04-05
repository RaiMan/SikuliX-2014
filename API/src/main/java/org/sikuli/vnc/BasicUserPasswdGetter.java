/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
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
