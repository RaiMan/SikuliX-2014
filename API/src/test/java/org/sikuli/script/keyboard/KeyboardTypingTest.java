package org.sikuli.script.keyboard;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.sikuli.basics.Settings;
import org.sikuli.natives.CommandExecutorHelper;
import org.sikuli.script.App;
import org.sikuli.script.Key;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import java.util.Arrays;
import java.util.List;

/**
 * @author tschneck
 *         Date: 12/14/15
 */
@Ignore
public class KeyboardTypingTest extends TestCase {
    private App gedit;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if(Settings.isWindows()){
            notepad();
        }else {
            gedit();
        }
    }

    private void gedit() {
        try {
            CommandExecutorHelper.execute("killall gedit", 0);
        } catch (Exception e) {
        }
        gedit = new App("gedit").openAndWait(2);
    }

    private void notepad() {
        try {
            CommandExecutorHelper.execute("Taskkill /IM notepad.exe /F", 0);
        } catch (Exception e) {
        }
        gedit = new App("notepad.exe").openAndWait(2);
    }

    public void testTyping() throws Exception {
        List<String> rows = Arrays.asList(
                "1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0",
                "a-b-c-d-e-f-g-h-i-j-a-b-c-d-e-f-g-h-i-j-a-b-c-d-e-f-g-h-i-j",
                "[{zzzzz}]",
                "haloo \"sikuli\"",
                "haloo @consol! ᣑ",
                "++++",
                "ÍÍÍÍ",
                "ϧϧϧϧϧ",
                "/////",
                "*111****",
                "---\b-",
                "zzzzz",
                "yyyyy",
                "\"\'\\_{}[];:;",
                "öÖ",
                "!\"$%&/()=?{}[]\\",
                "!\"$%&/()=?{}[]\\",
                "!\"§$%&/()=?{}[]\\",
                "@€Üü+*~",
                "ÖöÄä'#",
                "><|µ,;:.-_",
                ""
        );

        Region r = new Screen();
        for (String row : rows) {
            r.type("" + row);
            r.type(Key.ENTER);
        }
        Character uml = 'ö';

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
//        CommandExecutorHelper.execute("killall gedit", 0);
    }
}