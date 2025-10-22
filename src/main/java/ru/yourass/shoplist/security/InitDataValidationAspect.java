package ru.yourass.shoplist.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import ru.yourass.shoplist.services.TelegramService;

@Aspect
@Component
public class InitDataValidationAspect {
    private final TelegramService telegramService;

    public InitDataValidationAspect(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Before("@within(ru.yourass.shoplist.security.RequireInitData) || " +
            "@annotation(ru.yourass.shoplist.security.RequireInitData)")
    public void validateInitDataHeader() {
        HttpServletRequest req = currentRequest();
        String auth = req.getHeader("Authorization");
        try {
            telegramService.validateInitData(auth);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad initData", e);
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No request context");
        }
        return sra.getRequest();
    }
}
