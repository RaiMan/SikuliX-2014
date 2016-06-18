/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.remoteinterfaces.entities;

import java.util.List;

/**
 * Author: Sergey Kuts
 */
public interface Command {

    String getProcess();

    List<String> getArgs();

    int getTimeout();
}
