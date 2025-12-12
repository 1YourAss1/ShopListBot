package ru.yourass.shoplist.action;

import ru.yourass.shoplist.model.Update;

public interface Action {
    /**
     *
     * @return telegram action key (start with /)
     */
    String getKey();

    /**
     *
     * @param update telegram update
     * @return true if waiting callback
     */
    boolean handle(Update update);

    /**
     *
     * @param update telegram update
     * @return true if success
     */
    boolean callback(Update update);
}
