/*
 * $Id: JXApplet.java 4175 2012-05-30 16:57:33Z kschaefe $
 *
 * Copyright 2010 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx;

import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * An applet that uses {@link JXRootPane} as its root container.
 *
 * @author kschaefer
 */
@SuppressWarnings("nls")
public class JXApplet extends JApplet {
    private static final long serialVersionUID = 2L;

    /**
     * Creates a the applet instance.
     * <p>
     * This constructor sets the component's locale property to the value returned by
     * <code>JComponent.getDefaultLocale</code>.
     *
     * @throws HeadlessException
     *             if GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     */
    public JXApplet() throws HeadlessException {
        super();
    }

    /**
     * Overridden to create a JXRootPane and to ensure that the root pane is always created on the
     * Event Dispatch Thread. Some applet containers do not start applets on the EDT; this method,
     * therefore, protects against that. Actual, root pane creation occurs in
     * {@link #createRootPaneSafely()}.
     *
     * @return the root pane for this applet
     * @see #createRootPaneSafely()
     */
    @Override
    protected final JXRootPane createRootPane() {
        if (SwingUtilities.isEventDispatchThread()) {
            return createRootPaneSafely();
        }

        try {
            return SwingXUtilities.invokeAndWait(new Callable<JXRootPane>() {
                @Override
                public JXRootPane call() throws Exception {
                    return createRootPaneSafely();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            IllegalComponentStateException thrown = new IllegalComponentStateException("cannot construct root pane");
            thrown.initCause(e);

            throw thrown;
        }

        return null;
    }

    /**
     * This method performs the actual creation of the root pane and is guaranteed to be performed on the Event Dispatch Thread.
     * <p>
     * Subclasses that need to configure the root pane or create a custom root pane should override this method.
     *
     * @return the root pane for this applet
     */
    protected JXRootPane createRootPaneSafely() {
        JXRootPane rp = new JXRootPane();
        rp.setOpaque(true);

        return rp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JXRootPane getRootPane() {
        return (JXRootPane) super.getRootPane();
    }

    /**
     * Returns the value of the status bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JXStatusBar} which is the current status bar
     * @see #setStatusBar(JXStatusBar)
     * @see JXRootPane#getStatusBar()
     */
    public JXStatusBar getStatusBar() {
        return getRootPane().getStatusBar();
    }

    /**
     * Sets the status bar property on the underlying {@code JXRootPane}.
     *
     * @param statusBar
     *            the {@code JXStatusBar} which is to be the status bar
     * @see #getStatusBar()
     * @see JXRootPane#setStatusBar(JXStatusBar)
     */
    public void setStatusBar(JXStatusBar statusBar) {
        getRootPane().setStatusBar(statusBar);
    }

    /**
     * Returns the value of the tool bar property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JToolBar} which is the current tool bar
     * @see #setToolBar(JToolBar)
     * @see JXRootPane#getToolBar()
     */
    public JToolBar getToolBar() {
        return getRootPane().getToolBar();
    }

    /**
     * Sets the tool bar property on the underlying {@code JXRootPane}.
     *
     * @param toolBar
     *            the {@code JToolBar} which is to be the tool bar
     * @see #getToolBar()
     * @see JXRootPane#setToolBar(JToolBar)
     */
    public void setToolBar(JToolBar toolBar) {
        getRootPane().setToolBar(toolBar);
    }

    /**
     * Returns the value of the default button property from the underlying
     * {@code JRootPane}.
     *
     * @return the {@code JButton} which is the default button
     * @see #setDefaultButton(JButton)
     * @see JRootPane#getDefaultButton()
     */
    public JButton getDefaultButton() {
        return getRootPane().getDefaultButton();
    }

    /**
     * Sets the default button property on the underlying {@code JRootPane}.
     *
     * @param button
     *            the {@code JButton} which is to be the default button
     * @see #getDefaultButton()
     * @see JRootPane#setDefaultButton(JButton)
     */
    public void setDefaultButton(JButton button) {
        getRootPane().setDefaultButton(button);
    }

    /**
     * Returns the value of the cancel button property from the underlying
     * {@code JXRootPane}.
     *
     * @return the {@code JButton} which is the cancel button
     * @see #setCancelButton(JButton)
     * @see JXRootPane#getCancelButton()
     */
    public JButton getCancelButton() {
        return getRootPane().getCancelButton();
    }

    /**
     * Sets the cancel button property on the underlying {@code JXRootPane}.
     *
     * @param button
     *            the {@code JButton} which is to be the cancel button
     * @see #getCancelButton()
     * @see JXRootPane#setCancelButton(JButton)
     */
    public void setCancelButton(JButton button) {
        getRootPane().setCancelButton(button);
    }
}
