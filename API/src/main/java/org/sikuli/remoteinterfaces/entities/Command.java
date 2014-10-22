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
