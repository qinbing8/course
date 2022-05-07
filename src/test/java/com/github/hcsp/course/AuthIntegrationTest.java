package com.github.hcsp.course;

import com.github.hcsp.course.model.Session;
import com.github.hcsp.course.model.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AuthIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void registerLoginLogout() throws IOException, InterruptedException {
        // 注册用户
        String usernameAndPassword = "username=zhangsan&password=123456";

        HttpResponse<String> response = post("/user",
                APPLICATION_JSON_VALUE,
                APPLICATION_FORM_URLENCODED_VALUE,
                usernameAndPassword);
        User responseUser = objectMapper.readValue(response.body(), User.class);

        assertEquals(201, response.statusCode());
        assertEquals("zhangsan", responseUser.getUsername());
        assertNull(responseUser.getEncryptedPassword());

        // 用该用户登录
        response = post("/session",
                APPLICATION_JSON_VALUE,
                APPLICATION_FORM_URLENCODED_VALUE,
                usernameAndPassword);
        responseUser = objectMapper.readValue(response.body(), User.class);

        String cookie = response.headers()
                .firstValue("Set-Cookie")
                .get();

        assertNotNull(cookie);
        assertEquals(200, response.statusCode());
        assertEquals("zhangsan", responseUser.getUsername());
        assertNull(responseUser.getEncryptedPassword());

        // 确定该用户登录成功
        response = get("/session", cookie);
        assertEquals(200, response.statusCode());
        Session session = objectMapper.readValue(response.body(), Session.class);
        assertEquals("zhangsan", session.getUser().getUsername());
        // 调用注销接口
        response = delete("/session", cookie);
        assertEquals(204, response.statusCode());

        // 再次尝试访问用户的登录状态
        // 确定该用户登出
        response = get("/session", cookie);
        assertNotNull(cookie);
        assertEquals(401, response.statusCode());
    }

    @Test
    public void getErrorIfUsernameAlreadyRegister() throws IOException, InterruptedException {
        // 注册用户
        String usernameAndPassword = "username=zhangsan&password=123456";

        HttpResponse<String> response = post("/user",
                APPLICATION_JSON_VALUE,
                APPLICATION_FORM_URLENCODED_VALUE,
                usernameAndPassword);
        User responseUser = objectMapper.readValue(response.body(), User.class);
        // 成功
        assertEquals(201, response.statusCode());
        // 再次使用同名用户注册
        response = post("/user",
                APPLICATION_JSON_VALUE,
                APPLICATION_FORM_URLENCODED_VALUE,
                usernameAndPassword);
        // 失败
        assertEquals(409, response.statusCode());
    }
}
