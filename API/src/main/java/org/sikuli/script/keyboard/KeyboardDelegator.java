package org.sikuli.script.keyboard;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.sikuli.natives.OSUtil;
import org.sikuli.natives.SysUtil;
import org.sikuli.script.Key;

/**
 * Singleton implementation to get correct keyboard mapping for each supported language.
 * See therefor all Implementations of {@link KeyMapping}.
 *
 * @author tschneck
 *         Date: 12/14/15
 */
public class KeyboardDelegator {

    private static final OSUtil _osUtil = SysUtil.getOSUtil();
    private static KeyMapping keyMapping = null;

    KeyboardDelegator() {
        //protected constructor: use getKeyMappingInstance()
    }

    static KeyMapping getKeyMappingInstance() {
        if (keyMapping == null) {
            if (Settings.isMac() && !_osUtil.isUtf8InputSupported()) {
                //macs doesn't support unicode input by default so choose the US-keyboard mapping.
                //To active: see https://en.wikipedia.org/wiki/Unicode_input#In_Mac_OS
                keyMapping = new KeyMappingMac();
            } else {
                keyMapping = new KeyMappingAlphanumeric();
            }
        }
        return keyMapping;
    }

    /**
     * Convert Sikuli Key to Java virtual key code
     *
     * @param key as Character
     * @return the Java KeyCodes
     */
    public static KeyPress toJavaKeyCode(char key) {
        try {
            return getKeyMappingInstance().lookupKeePress(key);
        } catch (IllegalArgumentException e) {
            //if default Mac OS X key layout is active => UTF-8 wouldn't be supported
            if (getKeyMappingInstance() instanceof KeyMappingMac){
                throw e;
            }
            KeyPress result = getUnicodeKeyCombination(key);
            Debug.log("Can't find native key mapping => use unicode input: " + result);
            return result;
        }
    }

    static KeyPress getUnicodeKeyCombination(char key) {
        String keyCombo = _osUtil.getUnicodeKeyInputString(key);
        //create base keyPress object with OS based modifier matching
        KeyPress keyPress = new KeyPress(new int[]{}, _osUtil.getUnicodeModifier());
        if (keyCombo != null) {
            for (char c : keyCombo.toCharArray()) {
                //use Numpad keys in case of an windows system
                c = lookupNumpadKey(c);
                keyPress.addKeyPress(getKeyMappingInstance().lookupKeePress(c));
            }
        }
        return keyPress;
    }

    private static char lookupNumpadKey(char c) {
        switch (c) {
            case '1':
                return Key.C_NUM1;
            case '2':
                return Key.C_NUM2;
            case '3':
                return Key.C_NUM3;
            case '4':
                return Key.C_NUM4;
            case '5':
                return Key.C_NUM5;
            case '6':
                return Key.C_NUM6;
            case '7':
                return Key.C_NUM7;
            case '8':
                return Key.C_NUM8;
            case '9':
                return Key.C_NUM9;
            case '0':
                return Key.C_NUM0;
            default:
                return c;
        }
    }

}
