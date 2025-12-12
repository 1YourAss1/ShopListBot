package ru.yourass.shoplist.action;

import ru.yourass.shoplist.model.User;

public interface Action {
    /**
     * @return telegram команда (start with /)
     */
    String getKey();

    /**
     * @param user пользователь
     * @param data текстовые данные
     * @return true, если ожидается callback
     */
    boolean handle(User user, String data);

    /**
     * @param user пользователь
     * @param data текстовые данные
     * @return true, если успешно отработан
     */
    boolean callback(User user, String data);
}
