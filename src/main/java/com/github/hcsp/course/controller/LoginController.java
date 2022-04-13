package com.github.hcsp.course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class LoginController {
    private Map<String, String> userPasswords = new ConcurrentHashMap<>();
    private Map<String, String> cookieToUsername = new ConcurrentHashMap<>();

    {
        userPasswords.put("zhangsan", "123");
        userPasswords.put("lisi", "1234");
    }

    public static class UsernameAndPassword {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody UsernameAndPassword usernameAndPassword, HttpServletResponse response) {
        String username = usernameAndPassword.getUsername();
        String password = usernameAndPassword.getPassword();
        if (password.equals(userPasswords.get(username))) {
            // 登录成功，向用户发送一个Cookie
            String sessionId = UUID.randomUUID().toString();
            String cookieName = "onlineCourseSessionId";

            response.addCookie(new Cookie(cookieName, sessionId));
            cookieToUsername.put(sessionId, username);
            return "Login successfully";
        } else {
            return "Login failed";
        }
    }
}
