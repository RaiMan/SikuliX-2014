package org.sikuli.script.keyboard;

import junit.framework.TestCase;
import org.sikuli.basics.Settings;
import org.sikuli.natives.SysUtil;

import java.awt.event.KeyEvent;

/**
 * @author tschneck
 *         Date: 12/17/15
 */
public class KeyboardDelegatorTest extends TestCase {

    public void testUtf8KeyCombination() throws Exception {
        KeyPress kp = KeyboardDelegator.getUnicodeKeyCombination('ü');
        if (Settings.isWindows()) {
            boolean utf8 = SysUtil.getOSUtil().isUtf8InputSupported();
            assertEquals(kp.getModifiers()[0], KeyEvent.VK_ALT);
            if (utf8) {
                assertEquals(kp.getModifiers()[1], KeyEvent.VK_ADD);
                assertEquals(kp.getKeys().length, 2);
                assertEquals(kp.getKeys()[0], KeyEvent.VK_F);
                assertEquals(kp.getKeys()[1], KeyEvent.VK_C);
            }else {
                assertEquals(kp.getKeys().length, 4);
                assertEquals(kp.getKeys()[0], KeyEvent.VK_NUMPAD0);
                assertEquals(kp.getKeys()[1], KeyEvent.VK_NUMPAD2);
                assertEquals(kp.getKeys()[2], KeyEvent.VK_NUMPAD5);
                assertEquals(kp.getKeys()[3], KeyEvent.VK_NUMPAD2);
            }
        } else if (Settings.isLinux()) {
            assertEquals(kp.getKeys().length, 2);
            assertEquals(kp.getKeys()[0], KeyEvent.VK_F);
            assertEquals(kp.getKeys()[1], KeyEvent.VK_C);
            assertEquals(kp.getModifiers().length, 3);
            assertEquals(kp.getModifiers()[0], KeyEvent.VK_CONTROL);
            assertEquals(kp.getModifiers()[1], KeyEvent.VK_SHIFT);
            assertEquals(kp.getModifiers()[2], KeyEvent.VK_U);
        } else {
            assertEquals(kp.getKeys().length, 2);
            assertEquals(kp.getKeys()[0], KeyEvent.VK_F);
            assertEquals(kp.getKeys()[1], KeyEvent.VK_C);
            assertEquals(kp.getModifiers().length, 1);
            assertEquals(kp.getModifiers()[0], KeyEvent.VK_ALT);
        }
    }
}