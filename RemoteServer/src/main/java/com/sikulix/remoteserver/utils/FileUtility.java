package com.sikulix.remoteserver.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Sergey Kuts
 */
public final class FileUtility {

    private static final Logger IO_LOGGER = Logger.getLogger(FileUtility.class.getName());

    private FileUtility() {
    }

    public static boolean createFolder(final String path) {
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            IO_LOGGER.severe(e.getMessage());
        }

        final boolean exists = exists(path, false);
        IO_LOGGER.info(path + " created = " + exists);

        return exists;
    }

    public static boolean delete(final String path) {
        try {
            FileUtils.forceDelete(new File(path));
        } catch (IOException e) {
            IO_LOGGER.severe(e.getMessage());
        }

        final boolean exists = exists(path, false);
        IO_LOGGER.info(path + " deleted = " + !exists);

        return !exists;
    }

    public static boolean cleanFolder(final String path) {
        boolean cleared;

        try {
            FileUtils.cleanDirectory(new File(path));
            cleared = true;
        } catch (IOException e) {
            IO_LOGGER.severe(e.getMessage());
            cleared = false;
        }

        IO_LOGGER.info(path + " folder cleared = " + cleared);

        return cleared;
    }

    public static boolean copyFolder(final String copyFrom, final String copyTo) {
        boolean copied;

        try {
            FileUtils.copyDirectory(new File(copyFrom), new File(copyTo));
            copied = true;
        } catch (IOException e) {
            IO_LOGGER.severe(e.getMessage());
            copied = false;
        }

        IO_LOGGER.info(copyFrom + " copied to " + copyTo + " = " + copied);

        return copied;
    }

    public static boolean exists(final List<String> paths) {
        boolean exists = true;

        for (String path : paths) {
            if (!exists(path, true)) {
                exists = false;
                break;
            }
        }

        return exists;
    }

    private static boolean exists(final String path, final boolean showLog) {
        final boolean exists = new File(path).exists();

        if (showLog) {
            IO_LOGGER.info(path + " exists = " + exists);
        }

        return exists;
    }
}
