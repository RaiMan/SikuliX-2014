package com.sikulix.remoteserver.wrapper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.LogOutputStream;

/**
 * Author: Sergey Kuts
 */
public class ExecutionLogger extends LogOutputStream {

    private Logger logger;

    public ExecutionLogger(final Logger logger, final Level logLevel) {
        super(logLevel.intValue());
        this.logger = logger;
    }

    protected void processLine(final String line, final int level) {
        logger.log(logger.getLevel(), line);
    }
}
