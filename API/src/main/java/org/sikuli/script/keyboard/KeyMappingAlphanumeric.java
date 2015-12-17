package org.sikuli.script.keyboard;

import org.sikuli.script.Key;

import java.awt.event.KeyEvent;

/**
 * @author tschneck
 *         Date: 12/14/15
 */
public class KeyMappingAlphanumeric implements KeyMapping {
    @Override
    public KeyPress lookupKeePress(char key) {
        switch (key) {
//Lowercase
            case 'a':
                return new KeyPress(KeyEvent.VK_A);
            case 'b':
                return new KeyPress(KeyEvent.VK_B);
            case 'c':
                return new KeyPress(KeyEvent.VK_C);
            case 'd':
                return new KeyPress(KeyEvent.VK_D);
            case 'e':
                return new KeyPress(KeyEvent.VK_E);
            case 'f':
                return new KeyPress(KeyEvent.VK_F);
            case 'g':
                return new KeyPress(KeyEvent.VK_G);
            case 'h':
                return new KeyPress(KeyEvent.VK_H);
            case 'i':
                return new KeyPress(KeyEvent.VK_I);
            case 'j':
                return new KeyPress(KeyEvent.VK_J);
            case 'k':
                return new KeyPress(KeyEvent.VK_K);
            case 'l':
                return new KeyPress(KeyEvent.VK_L);
            case 'm':
                return new KeyPress(KeyEvent.VK_M);
            case 'n':
                return new KeyPress(KeyEvent.VK_N);
            case 'o':
                return new KeyPress(KeyEvent.VK_O);
            case 'p':
                return new KeyPress(KeyEvent.VK_P);
            case 'q':
                return new KeyPress(KeyEvent.VK_Q);
            case 'r':
                return new KeyPress(KeyEvent.VK_R);
            case 's':
                return new KeyPress(KeyEvent.VK_S);
            case 't':
                return new KeyPress(KeyEvent.VK_T);
            case 'u':
                return new KeyPress(KeyEvent.VK_U);
            case 'v':
                return new KeyPress(KeyEvent.VK_V);
            case 'w':
                return new KeyPress(KeyEvent.VK_W);
            case 'x':
                return new KeyPress(KeyEvent.VK_X);
            case 'y':
                return new KeyPress(KeyEvent.VK_Y);
            case 'z':
                return new KeyPress(KeyEvent.VK_Z);
//Uppercase
            case 'A':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_A);
            case 'B':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_B);
            case 'C':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_C);
            case 'D':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_D);
            case 'E':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_E);
            case 'F':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_F);
            case 'G':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_G);
            case 'H':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_H);
            case 'I':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_I);
            case 'J':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_J);
            case 'K':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_K);
            case 'L':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_L);
            case 'M':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_M);
            case 'N':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_N);
            case 'O':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_O);
            case 'P':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_P);
            case 'Q':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_Q);
            case 'R':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_R);
            case 'S':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_S);
            case 'T':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_T);
            case 'U':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_U);
            case 'V':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_V);
            case 'W':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_W);
            case 'X':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_X);
            case 'Y':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_Y);
            case 'Z':
                return new KeyPress(KeyEvent.VK_SHIFT, KeyEvent.VK_Z);
//Row 3 (below function keys)
//      case 'Press': return new Key (192}; //not producab)e
            case '1':
                return new KeyPress(KeyEvent.VK_1);
            case '2':
                return new KeyPress(KeyEvent.VK_2);
            case '3':
                return new KeyPress(KeyEvent.VK_3);
            case '4':
                return new KeyPress(KeyEvent.VK_4);
            case '5':
                return new KeyPress(KeyEvent.VK_5);
            case '6':
                return new KeyPress(KeyEvent.VK_6);
            case '7':
                return new KeyPress(KeyEvent.VK_7);
            case '8':
                return new KeyPress(KeyEvent.VK_8);
            case '9':
                return new KeyPress(KeyEvent.VK_9);
            case '0':
                return new KeyPress(KeyEvent.VK_0);
//Numpad keys
            case '-':
                return new KeyPress(KeyEvent.VK_SUBTRACT);
            case '+':
                return new KeyPress(KeyEvent.VK_ADD);
            case '/':
                return new KeyPress(KeyEvent.VK_DIVIDE);
            case '*':
                return new KeyPress(KeyEvent.VK_MULTIPLY);
//Special
            case '\b':
                return new KeyPress(KeyEvent.VK_BACK_SPACE);
            case '\t':
                return new KeyPress(KeyEvent.VK_TAB);
            case '\r':
                return new KeyPress(KeyEvent.VK_ENTER);
            case '\n':
                return new KeyPress(KeyEvent.VK_ENTER);
            case ' ':
                return new KeyPress(KeyEvent.VK_SPACE);
//Modifier
            case Key.C_SHIFT:
                return new KeyPress(KeyEvent.VK_SHIFT);
            case Key.C_CTRL:
                return new KeyPress(KeyEvent.VK_CONTROL);
            case Key.C_ALT:
                return new KeyPress(KeyEvent.VK_ALT);
            case Key.C_META:
                return new KeyPress(KeyEvent.VK_META);
//Cursor movement
            case Key.C_UP:
                return new KeyPress(KeyEvent.VK_UP);
            case Key.C_RIGHT:
                return new KeyPress(KeyEvent.VK_RIGHT);
            case Key.C_DOWN:
                return new KeyPress(KeyEvent.VK_DOWN);
            case Key.C_LEFT:
                return new KeyPress(KeyEvent.VK_LEFT);
            case Key.C_PAGE_UP:
                return new KeyPress(KeyEvent.VK_PAGE_UP);
            case Key.C_PAGE_DOWN:
                return new KeyPress(KeyEvent.VK_PAGE_DOWN);
            case Key.C_END:
                return new KeyPress(KeyEvent.VK_END);
            case Key.C_HOME:
                return new KeyPress(KeyEvent.VK_HOME);
            case Key.C_DELETE:
                return new KeyPress(KeyEvent.VK_DELETE);
//Function keys
            case Key.C_ESC:
                return new KeyPress(KeyEvent.VK_ESCAPE);
            case Key.C_F1:
                return new KeyPress(KeyEvent.VK_F1);
            case Key.C_F2:
                return new KeyPress(KeyEvent.VK_F2);
            case Key.C_F3:
                return new KeyPress(KeyEvent.VK_F3);
            case Key.C_F4:
                return new KeyPress(KeyEvent.VK_F4);
            case Key.C_F5:
                return new KeyPress(KeyEvent.VK_F5);
            case Key.C_F6:
                return new KeyPress(KeyEvent.VK_F6);
            case Key.C_F7:
                return new KeyPress(KeyEvent.VK_F7);
            case Key.C_F8:
                return new KeyPress(KeyEvent.VK_F8);
            case Key.C_F9:
                return new KeyPress(KeyEvent.VK_F9);
            case Key.C_F10:
                return new KeyPress(KeyEvent.VK_F10);
            case Key.C_F11:
                return new KeyPress(KeyEvent.VK_F11);
            case Key.C_F12:
                return new KeyPress(KeyEvent.VK_F12);
            case Key.C_F13:
                return new KeyPress(KeyEvent.VK_F13);
            case Key.C_F14:
                return new KeyPress(KeyEvent.VK_F14);
            case Key.C_F15:
                return new KeyPress(KeyEvent.VK_F15);
//Toggling kezs
            case Key.C_SCROLL_LOCK:
                return new KeyPress(KeyEvent.VK_SCROLL_LOCK);
            case Key.C_NUM_LOCK:
                return new KeyPress(KeyEvent.VK_NUM_LOCK);
            case Key.C_CAPS_LOCK:
                return new KeyPress(KeyEvent.VK_CAPS_LOCK);
            case Key.C_INSERT:
                return new KeyPress(KeyEvent.VK_INSERT);
//Windows special
            case Key.C_PAUSE:
                return new KeyPress(KeyEvent.VK_PAUSE);
            case Key.C_PRINTSCREEN:
                return new KeyPress(KeyEvent.VK_PRINTSCREEN);
//Num pad
            case Key.C_NUM0:
                return new KeyPress(KeyEvent.VK_NUMPAD0);
            case Key.C_NUM1:
                return new KeyPress(KeyEvent.VK_NUMPAD1);
            case Key.C_NUM2:
                return new KeyPress(KeyEvent.VK_NUMPAD2);
            case Key.C_NUM3:
                return new KeyPress(KeyEvent.VK_NUMPAD3);
            case Key.C_NUM4:
                return new KeyPress(KeyEvent.VK_NUMPAD4);
            case Key.C_NUM5:
                return new KeyPress(KeyEvent.VK_NUMPAD5);
            case Key.C_NUM6:
                return new KeyPress(KeyEvent.VK_NUMPAD6);
            case Key.C_NUM7:
                return new KeyPress(KeyEvent.VK_NUMPAD7);
            case Key.C_NUM8:
                return new KeyPress(KeyEvent.VK_NUMPAD8);
            case Key.C_NUM9:
                return new KeyPress(KeyEvent.VK_NUMPAD9);
//Num pad special
            case Key.C_SEPARATOR:
                return new KeyPress(KeyEvent.VK_SEPARATOR);
            case Key.C_ADD:
                return new KeyPress(KeyEvent.VK_ADD);
            case Key.C_MINUS:
                return new KeyPress(KeyEvent.VK_SUBTRACT);
            case Key.C_MULTIPLY:
                return new KeyPress(KeyEvent.VK_MULTIPLY);
            case Key.C_DIVIDE:
                return new KeyPress(KeyEvent.VK_DIVIDE);
            case Key.C_DECIMAL:
                return new KeyPress(KeyEvent.VK_DECIMAL);
            case Key.C_CONTEXT:
                return new KeyPress(KeyEvent.VK_CONTEXT_MENU);
            case Key.C_WIN:
                return new KeyPress(KeyEvent.VK_WINDOWS);
//hack: alternative tab in GUI
            case Key.C_NEXT:
                return new KeyPress(-KeyEvent.VK_TAB);

            default:
                throw new IllegalArgumentException("Cannot convert character " + key);
        }

    }
}
