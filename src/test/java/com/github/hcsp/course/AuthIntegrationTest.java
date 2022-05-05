package com.github.hcsp.course;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hcsp.course.model.Session;
import com.github.hcsp.course.model.User;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CourseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test.properties"})
public class AuthIntegrationTest {
    @Autowired
    Environment environment;
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    @Value("${spring.datasource.password}")
    private String databasePassword;

    private ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient client = HttpClient.newHttpClient();


    public String getPort() {
        return environment.getProperty("local.server.port");
    }

    @BeforeEach
    public void resetDatabase() {
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(conf);
        flyway.clean();
        flyway.migrate();
    }

    private HttpResponse<String> post(String path, // path是/user /session 这样的路径
                                      String accept,
                                      String contentType,
                                      String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", accept)
                .header("Content-type", contentType)
                .uri(URI.create("http://localhost:" + getPort() + "/api/v1" + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String cookie) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", APPLICATION_JSON_VALUE)
                .header("Cookie", cookie)
                .uri(URI.create("http://localhost:" + getPort() + "/api/v1" + path))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String cookie) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", APPLICATION_JSON_VALUE)
                .header("Cookie", cookie)
                .uri(URI.create("http://localhost:" + getPort() + "/api/v1" + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

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

    @Test
    public void onlyAdminCanSeeAllUsers() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/admin/users", "COURSE_APP_SESSION_ID=test_user_3");
        assertEquals(200, response.statusCode());
    }

    @Test
    public void notAdminCanNotSeeAllUsers() throws IOException, InterruptedException {
        HttpResponse<String> response = get("/admin/users", "COURSE_APP_SESSION_ID=test_user_1");
        assertEquals(403, response.statusCode());
    }
}
