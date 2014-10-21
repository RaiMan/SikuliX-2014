package com.sikulix.remoteserver.interfaces.common;

import com.sikulix.remoteserver.interfaces.entities.Image;

/**
 * Author: Sergey Kuts
 */
public interface Sikulix extends CommandLineExecutor, FileTransporter {

    void click(final Image image, final int timeout);

    void setText(final Image image, final String text, final int timeout);

    boolean exists(final Image image, final int timeout);

    // Override it for closing client connections, or just skip
    default void close() {
    }
}
