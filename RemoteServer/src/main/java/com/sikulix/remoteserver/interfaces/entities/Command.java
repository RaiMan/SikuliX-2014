package com.sikulix.remoteserver.interfaces.entities;

import java.util.List;

/**
 * Author: Sergey Kuts
 */
public interface Command {

    String getProcess();

    List<String> getArgs();

    int getTimeout();

    default String getValues() {
        return "[process = " + getProcess() +
                "; args = " + getArgs() +
                "; timeout = " + getTimeout() + "]";
    }
}
