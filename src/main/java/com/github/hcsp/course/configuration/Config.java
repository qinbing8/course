package com.github.hcsp.course.configuration;

import com.github.hcsp.course.dao.SessionDao;
import com.github.hcsp.course.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.hcsp.course.configuration.UserInterceptor.COOKIE_NAME;

@Configuration
public class Config {
    public static class UserContext {
        private static ThreadLocal<User> currentUser = new ThreadLocal<>();

        @Autowired
        SessionDao sessionDao;

        // 获取当前线程上下文的用户，null代表没有登录
        public static User getCurrentUser() {
            return currentUser.get();
        }

        // 为当前线程上下文设置用户，null代表清空当前用户
        public static void setCurrentUser(User currentUser) {
            UserContext.currentUser.set(currentUser);
        }
    }

    public static Optional<String> getCookie(HttpServletRequest request) {
        // 从数据库根据cookie取出用户信息，并放到当前的 线程上下文
        Cookie[] cookies = request.getCookies();

        return Stream.of(cookies).filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst();
    }

}
