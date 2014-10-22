package org.sikuli.remoteinterfaces.common;

/**
 * Author: Sergey Kuts
 */
public interface FileTransporter {

    void uploadFile(final String filePath, final String saveToPath);

    void downloadFile(final String downloadFilePath, final String saveToPath);
}
