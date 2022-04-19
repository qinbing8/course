package com.github.hcsp.course.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.hcsp.course.configuration.Config;
import com.github.hcsp.course.dao.UserRepository;
import com.github.hcsp.course.model.HttpException;
import com.github.hcsp.course.model.Session;
import com.github.hcsp.course.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class AuthController {
    @Autowired
    UserRepository userRepository;
    /**
     * @api {get} /api/v1/session 检查登录状态
     * @apiName 检查登录状态
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/auth
     *
     * @apiSuccess {User} user 用户信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "user": {
     *           "id": 123,
     *           "username": "Alice"
     *       }
     *     }
     * @apiError 401 Unauthorized 若用户未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "Unauthorized"
     *     }
     */
    /**
     * @return 已登录的用户
     */
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

    @PostMapping("/user")
    public User register(@RequestParam("username") String username,
                         @RequestParam("passwoord") String password) {
        if (ObjectUtils.isEmpty(username) || username.length() > 20 || username.length() < 5) {
            throw new HttpException(400, "用户名必须在6到20之间");
        }
        if (ObjectUtils.isEmpty(password)) {
            throw new HttpException(400, "密码不能为空");
        }
        User user = new User();
        user.setUsername(username);
        user.setEncryptedPassword(BCrypt.withDefaults()
                .hashToString(12, password.toCharArray()));

        try {
            userRepository.save(user);
        } catch (Throwable e) {
            throw new HttpException(409, "用户们已经被注册")
        }
        return user;
    }
}
