/*
 * Copyright (c) 2010-2016, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 */

package org.sikuli.remoteinterfaces.common;

import org.sikuli.remoteinterfaces.entities.Image;

import java.util.List;

/**
 * Author: Sergey Kuts
 */
public interface Sikulix extends CommandLineExecutor, FileProcessor {

    void click(final Image image, final int timeout);

    void setText(final Image image, final String text, final int timeout);

    boolean exists(final Image image, final int timeout);

    void dragAndDrop(final List<Image> images, final int timeout);

    void close();
}
