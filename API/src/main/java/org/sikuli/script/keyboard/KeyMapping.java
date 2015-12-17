package org.sikuli.script.keyboard;

/**
 * @author tschneck
 *         Date: 12/14/15
 */
public interface KeyMapping {

    /**
     * Convert Sikuli Key to Java virtual key code
     *
     * @param key as Character
     * @return the Java KeyCodes
     */
    KeyPress lookupKeePress(char key);
}
