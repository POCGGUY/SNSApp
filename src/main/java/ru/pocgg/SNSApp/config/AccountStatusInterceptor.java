package ru.pocgg.SNSApp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.pocgg.SNSApp.model.User;
import ru.pocgg.SNSApp.model.UserDetailsImpl;
import ru.pocgg.SNSApp.services.UserService;

@Component
@RequiredArgsConstructor
public class AccountStatusInterceptor implements HandlerInterceptor {
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/auth/")
                || path.contains("/users/register")
                || path.matches("/users/me/deleted.*")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            int userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            User user = userService.getUserById(userId);
            if (user.getBanned()) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Account is banned");
                return false;
            }
            if (user.getDeleted()) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Account is deleted");
                return false;
            }
        }
        return true;
    }
}

