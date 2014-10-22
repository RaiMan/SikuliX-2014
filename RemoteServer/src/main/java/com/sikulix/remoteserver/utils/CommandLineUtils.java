package com.sikulix.remoteserver.utils;

import org.sikuli.remoteinterfaces.entities.Command;
import com.sikulix.remoteserver.wrapper.ExecutionLogger;
import org.apache.commons.exec.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.exec.util.StringUtils.quoteArgument;

/**
 * Author: Sergey Kuts
 */
public final class CommandLineUtils {

    private static final Logger CMD_LOGGER = Logger.getLogger(CommandLineUtils.class.getName());

    private CommandLineUtils() {
    }

    public static int executeCommandLine(final Command command) {

        CMD_LOGGER.info("Processing the following command: " + command.getProcess() + " " + command.getArgs());

        final long timeout = (command.getTimeout() > 0 ? command.getTimeout() : 0) * 1000;
        final CommandLine commandLine = new CommandLine(quoteArgument(command.getProcess()));

        for (String arg :  command.getArgs()) {
            commandLine.addArgument(quoteArgument(arg));
        }

        final ExecutionResultsHandler resultHandler = new ExecutionResultsHandler();
        final PumpStreamHandler streamHandler = new PumpStreamHandler(new ExecutionLogger(CMD_LOGGER, Level.INFO),
                new ExecutionLogger(CMD_LOGGER, Level.SEVERE));
        final DefaultExecutor executor = new DefaultExecutor();

        executor.setStreamHandler(streamHandler);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        try {
            executor.execute(commandLine, resultHandler);
            resultHandler.waitFor(timeout);
        } catch (InterruptedException | IOException e) {
            CMD_LOGGER.severe("Error occurred during command execution: " + e.getMessage());
            return -1;
        }

        return resultHandler.getExitValue();
    }

    private static class ExecutionResultsHandler extends DefaultExecuteResultHandler {

        private int exitValue;

        public void waitFor(final long timeout) throws InterruptedException {
            super.waitFor(timeout);

            if (!hasResult()) {
                exitValue = 1;
                CMD_LOGGER.log(Level.WARNING, "Main process finished after " + timeout + " waiting.");
            }
        }

        public int getExitValue() {
            return hasResult() ? super.getExitValue() : exitValue;
        }
    }
}
