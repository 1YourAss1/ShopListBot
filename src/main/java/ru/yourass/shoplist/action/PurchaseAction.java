package ru.yourass.shoplist.action;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.GroupMember;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.TelegramService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PurchaseAction implements Action {
    private static final Pattern PATTERN = Pattern.compile("^\\s*(.+?)(?:\\s+(\\d+(?:[.,]\\d+)?))??\\s*$");
    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public PurchaseAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "default";
    }

    @Override
    public boolean handle(User user, String data) {
        Matcher matcher = PATTERN.matcher(data);
        if (matcher.matches()) {
            String productName = matcher.group(1);
            Float quantity = matcher.group(2) !=  null ? Float.parseFloat(matcher.group(2).replace(',', '.')) : null;
            shopListService.savePurchase(user, productName, quantity);

            Optional<GroupMember> activeByUserId = shopListService.getActiveGroupMembersForUser(user);
            if (activeByUserId.isPresent()) {
                Group group = activeByUserId.get().getGroup();
                List<GroupMember> activeByGroup = shopListService.getActiveGroupMembersForGroup(group);
                activeByGroup.stream()
                        .filter(groupMember -> !groupMember.getUser().equals(user))
                        .forEach(groupMember -> {
                            User groupMemberUser = groupMember.getUser();
                            telegramService.sendMessage(groupMemberUser.getId(), messageSource.getMessage(
                                    "message.purchase.add",
                                    new Object[]{user.toString(), data},
                                    Locale.getDefault()));
                        });
            }
        }
        return false;
    }

    @Override
    public boolean callback(User user, String data) {
        return false;
    }
}
