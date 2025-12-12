package ru.yourass.shoplist.action;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yourass.shoplist.dao.GroupDAO;
import ru.yourass.shoplist.dao.GroupMemberDAO;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.GroupMember;
import ru.yourass.shoplist.model.Update;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ActionDispatcher {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(/\\w+)\\s*(.*)$");

    private final ShopListService shopListService;
    private final Map<String, Action> actionsCommandMap;
    private final Map<Long, Action> userActions = new ConcurrentHashMap<>();

    public ActionDispatcher(ShopListService shopListService,
                            @Qualifier("actionsCommandMap") Map<String, Action> actionsCommandMap) {
        this.shopListService = shopListService;
        this.actionsCommandMap = actionsCommandMap;
    }

    public void dispatch(Update update) {
        User user;
        String data;
        if (update.getMessage() != null) {
            user = update.getMessage().getFrom();
            data = update.getMessage().getText();
        } else if (update.getCallbackQuery() != null) {
            user = update.getCallbackQuery().getFrom();
            data = update.getCallbackQuery().getData();
        } else {
            throw new IllegalArgumentException("Invalid update data");
        }

        shopListService.createUsersDbIfNotExists(user);
        Long userId = user.getId();

        Action userAction = userActions.get(userId);
        if (userAction != null) {
            if (userAction.callback(user, data)) {
                userActions.remove(userId);
            }
            return;
        }

        Optional<String> optCommand = this.extractCommand(data);
        if (optCommand.isPresent()) {
            data = this.extractDataWithoutCommand(data).orElse("");
            Action action = actionsCommandMap.get(optCommand.get());
            if (action != null && action.handle(user, data)) {
                userActions.put(userId, action);
            }
        } else {
            Action action = actionsCommandMap.get("default");
            if (action != null) {
                action.handle(user, data);
            }
        }
    }

    private Optional<String> extractCommand(String data) {
        String command = null;
        Matcher matcher = COMMAND_PATTERN.matcher(data);
        if (matcher.matches()) {
            command = matcher.group(1);
        }
        return Optional.ofNullable(command);
    }

    private Optional<String> extractDataWithoutCommand(String data) {
        String dataWithoutCommand = null;
        Matcher matcher = COMMAND_PATTERN.matcher(data);
        if (matcher.matches()) {
            dataWithoutCommand = matcher.group(2);
        }
        return Optional.ofNullable(dataWithoutCommand);
    }
}
