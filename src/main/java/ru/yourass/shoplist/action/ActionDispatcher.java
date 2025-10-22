package ru.yourass.shoplist.action;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActionDispatcher {
    private final Map<String, Action> actionsCommandMap;
    private final Map<Long, Action> userActions = new ConcurrentHashMap<>();

    public ActionDispatcher(@Qualifier("actionsCommandMap") Map<String, Action> actionsCommandMap) {
        this.actionsCommandMap = actionsCommandMap;
    }

    public void dispatch(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (userId == null) return;

        Action userAction = userActions.get(userId);
        if (userAction != null) {
            if (userAction.callback(update)) {
                userActions.remove(userId);
            }
            return;
        }

        String command = this.extractCommand(update);
        if (command != null && actionsCommandMap.containsKey(command)) {
            Action action = actionsCommandMap.get(command);
            if (action != null && action.handle(update)) {
                userActions.put(userId, action);
            }
        } else {
            Action action = actionsCommandMap.get("default");
            if (action != null) {
                action.handle(update);
            }
        }
    }

    // TODO optional
    private String extractCommand(Update update) {
        String text = update.getMessage().getText();
        if (text.startsWith("/")) {
            return text.split(" ")[0];
        }
        return null;
    }
}
