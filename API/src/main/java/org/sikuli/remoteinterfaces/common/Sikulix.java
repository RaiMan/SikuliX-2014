package org.sikuli.remoteinterfaces.common;

import org.sikuli.remoteinterfaces.entities.Image;

/**
 * Author: Sergey Kuts
 */
public interface Sikulix extends CommandLineExecutor, FileProcessor {

    void click(final Image image, final int timeout);

    void setText(final Image image, final String text, final int timeout);

    boolean exists(final Image image, final int timeout);

    void close();
}
