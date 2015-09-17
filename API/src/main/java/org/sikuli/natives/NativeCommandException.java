package org.sikuli.natives;

/**
 * Wrapper for all Exception which occurs during native command execution.
 *
 * @author tschneck
 *         Date: 9/15/15
 */
public class NativeCommandException extends RuntimeException {
    public NativeCommandException(String s) {
        super(s);
    }
}
