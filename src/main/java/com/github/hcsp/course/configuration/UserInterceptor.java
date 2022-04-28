package com.github.hcsp.course.configuration;

import com.github.hcsp.course.dao.SessionDao;
import com.github.hcsp.course.model.Session;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    public static final String COOKIE_NAME = "COURSE_APP_SESSION_ID";
    SessionDao sessionDao;

    public UserInterceptor(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Config.getCookie(request)
                .flatMap(sessionDao::findByCookie)
                .map(Session::getUser)
                .ifPresent(Config.UserContext::setCurrentUser);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Config.UserContext.setCurrentUser(null);
    }
}
