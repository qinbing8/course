package com.github.hcsp.course.controller;

import com.github.hcsp.course.configuration.Config;
import com.github.hcsp.course.model.HttpException;
import com.github.hcsp.course.model.Session;
import com.github.hcsp.course.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class AuthController {
    @GetMapping("/session")
    public Session authStatus() {
        User currentUser = Config.UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new HttpException(401, "Unauthorized");
        } else {
            Session session = new Session();
            session.setUser(currentUser);
            return session;
        }
    }
}
