package com.sikulix.remoteserver.wrapper;

import org.sikuli.remoteinterfaces.entities.Image;
import org.sikuli.script.*;

import java.awt.*;

/**
 * Author: Sergey Kuts
 */
public class RemoteDesktop {

    private enum SikuliAction {
        CLICK,
        TYPE,
    }

    private Region desktop;

    public RemoteDesktop() {
        this.desktop = new Region(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    public boolean click(final Image element, final int timeout) {
        return onAppear(createImage(element), SikuliAction.CLICK).observe(timeout);
    }

    public boolean setText(final Image element, final String text, final int timeout) {
        return onAppear(createImage(element), SikuliAction.TYPE, text).observe(timeout);
    }

    public boolean exists(final Image element, final int timeout) {
        return desktop.exists(createImage(element), timeout) != null;
    }

    private Pattern createImage(final Image element) {
        return new Pattern(element.getPath()).similar(element.getSimilarity());
    }

    private boolean observe(final int timeout) {
        return desktop.observe(timeout);
    }

    private RemoteDesktop onAppear(final Pattern element, final SikuliAction action) {
        return onAppear(element, action, "");
    }

    private RemoteDesktop onAppear(final Pattern element, final SikuliAction action, final String text) {
        desktop.onAppear(element, new ObserverCallBack() {
            public void appeared(ObserveEvent e) {
                switch (action) {
                    case CLICK:
                        e.getMatch().click();
                        break;
                    case TYPE:
                        e.getMatch().click();
                        e.getMatch().type(text);
                        break;
                }

                desktop.stopObserver();
            }
        });

        return this;
    }
}
