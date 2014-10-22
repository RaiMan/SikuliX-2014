package com.sikulix.entities;

import org.sikuli.remoteinterfaces.entities.Command;

import java.util.List;

/**
 * Author: Sergey Kuts
 */
public class CommandLineBox implements Command {

    private String process;
    private List<String> args;
    private int timeout;

    public CommandLineBox(final String process, final List<String> args, final int timeout) {
        this.process = process;
        this.args = args;
        this.timeout = timeout;
    }

    public String getProcess() {
        return process;
    }

    public List<String> getArgs() {
        return args;
    }

    public int getTimeout() {
        return timeout;
    }

    public String toString() {
        return "[process = " + getProcess() +
                "; args = " + getArgs() +
                "; timeout = " + getTimeout() + "]";
    }
}
