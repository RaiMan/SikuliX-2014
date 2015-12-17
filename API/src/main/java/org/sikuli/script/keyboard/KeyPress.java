package org.sikuli.script.keyboard;

import java.awt.event.KeyEvent;

/**
 * Represents an keyboard event which sould be executed in one action, and with possible modifiers.
 *
 * @author tschneck
 *         Date: 12/16/15
 */
public class KeyPress {
    private int[] keys = new int[0];
    private int[] modifiers = new int[0];

    public KeyPress(int... keys) {
        this.keys = keys;
    }

    public KeyPress(int[] keys, int[] modifiers) {
        this(keys);
        this.modifiers = modifiers;
    }

    private static String printkeys(int[] keys) {
        StringBuilder sb = new StringBuilder();
        if (keys != null) {
            for (int key : keys) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(KeyEvent.getKeyText(key));
            }
        }
        return sb.toString();
    }

    public int[] getKeys() {
        return keys;
    }

    public int[] getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return "(keys: " + printkeys(keys)
                + ((modifiers.length > 0) ? ", modifiers: " + printkeys(modifiers) + ")" : "");
    }

    /**
     * add the keypresses to current KeyPress object
     *
     * @param additionalKeypress a instance of {@link KeyPress}
     */
    public void addKeyPress(KeyPress additionalKeypress) {
        if (additionalKeypress != null) {
            keys = addToArray(keys, additionalKeypress.getKeys());
            modifiers = addToArray(modifiers, additionalKeypress.getModifiers());
        }
    }

    private int[] addToArray(int[] first, int[] second) {
        if (first != null && second != null && second.length > 0) {
            int[] newValue = new int[first.length + second.length];
            System.arraycopy(first, 0, newValue, 0, first.length);
            System.arraycopy(second, 0, newValue, first.length, second.length);
            return newValue;
        } else {
            return first;
        }
    }
}
