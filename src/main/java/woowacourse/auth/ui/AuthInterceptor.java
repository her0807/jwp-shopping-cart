package woowacourse.auth.ui;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import woowacourse.auth.application.AuthService;
import woowacourse.auth.exception.UnauthorizedException;

public class AuthInterceptor implements HandlerInterceptor {

    private AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(AUTHORIZATION);
        if (header == null) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다");
        }
        return authService.isValid(header);
    }
}
