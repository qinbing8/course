package com.github.hcsp.course.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.hcsp.course.configuration.Config;
import com.github.hcsp.course.dao.UserRepository;
import com.github.hcsp.course.model.HttpException;
import com.github.hcsp.course.model.Session;
import com.github.hcsp.course.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.github.hcsp.course.configuration.UserInterceptor.COOKIE_NAME;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private BCrypt.Verifyer verifyer = BCrypt.verifyer();
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

    /**
     * @api {post} /api/v1/user 注册
     * @apiName 注册
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/x-www-form-urlencoded
     *
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample Request-Example:
     *          username: Alice
     *          password: MySecretPassword
     *
     * @apiSuccess {Integer} id 用户id
     * @apiSuccess {String} username 用户名
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 Created
     *     {
     *         "id": 123,
     *         "username": "Alice"
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     *
     * @apiError 409 Conflict 若用户名已经被注册
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 409 Conflict
     *     {
     *       "message": "用户名已经被注册"
     *     }
     */
    /**
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/user")
    public User register(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         HttpServletResponse response) {
        if (ObjectUtils.isEmpty(username) || username.length() > 20 || username.length() < 5) {
            throw new HttpException(400, "用户名必须在6到20之间");
        }
        if (ObjectUtils.isEmpty(password)) {
            throw new HttpException(400, "密码不能为空");
        }

        User user = new User();
        user.setUsername(username);
        user.setEncryptedPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        try {
            userRepository.save(user);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
            //throw new HttpException(409, "用户们已经被注册");
        }
        response.setStatus(201);
        return user;
    }

    @PostMapping("/session")
    public User login(@RequestParam("username") String username,
                      @RequestParam("password") String password,
                      HttpServletResponse response) {
        User user = userRepository.findUsersByUsername(username);
        if (user == null) {
            throw new HttpException(401, "登录失败！");
        } else {
            if (verifyer.verify(password.toCharArray(), user.getEncryptedPassword()).verified) {
                String cookie = UUID.randomUUID().toString();
                response.addCookie(new Cookie(COOKIE_NAME, cookie));
                return user;
            } else {
                throw new HttpException(401, "登录失败！");
            }
        }
    }
}
