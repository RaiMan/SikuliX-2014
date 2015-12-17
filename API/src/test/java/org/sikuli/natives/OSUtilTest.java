package org.sikuli.natives;

import junit.framework.TestCase;

/**
 * @author tschneck
 *         Date: 12/15/15
 */
public class OSUtilTest extends TestCase {


    public static final String[] HEX_VALUES = new String[]{"fc", "a7", "22"};
    public static final String[] NUMERIC_VALUES = new String[]{"0252", "0167", "0034"};
    private OSUtil osutil = SysUtil.getOSUtil() ;

    public void testUtf8ToHex() throws Exception {
        assertEquals(osutil.getUnicodeKeyInputString('ü'), getValue(0));
        assertEquals(osutil.getUnicodeKeyInputString('§'), getValue(1));
        assertEquals(osutil.getUnicodeKeyInputString('\"'),getValue(2));
    }

    private String getValue(int i) {
        if (osutil instanceof WinUtil) {
            return osutil.isUtf8InputSupported() ? HEX_VALUES[i] : NUMERIC_VALUES[i];
        } else {
            return HEX_VALUES[i];
        }

    }


}
