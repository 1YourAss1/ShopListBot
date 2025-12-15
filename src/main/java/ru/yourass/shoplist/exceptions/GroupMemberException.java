package ru.yourass.shoplist.exceptions;

import java.text.MessageFormat;

public class GroupMemberException extends Exception {
    public GroupMemberException(String s) {
        super(s);
    }
    public GroupMemberException(String s, Object... args) {
        super(MessageFormat.format(s, args));
    }
}
