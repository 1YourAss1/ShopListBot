package ru.yourass.shoplist.exceptions;

import java.text.MessageFormat;

public class GroupException extends Exception {
    public GroupException(String s) {
        super(s);
    }
    public GroupException(String s, Object... args) {
        super(MessageFormat.format(s, args));
    }
}
