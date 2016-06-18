/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.remoteinterfaces.common;

import java.util.List;

/**
 * Author: Sergey Kuts
 */
public interface FileProcessor {

    void uploadFile(final List<String> filesPath, final String saveToPath);

    void downloadFile(final String downloadFilePath, final String saveToPath);

    void createFolder(final String path);

    void copyFolder(final String copyFrom, final String copyTo);

    void cleanFolder(final String path);

    void delete(final String path);

    boolean exists(final List<String> paths);
}
