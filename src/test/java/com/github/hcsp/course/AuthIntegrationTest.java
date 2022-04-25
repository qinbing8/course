package com.github.hcsp.course;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    public void registerLoginLogout() throws IOException, InterruptedException {
        // 注册用户
        HttpClient client = HttpClient.newHttpClient();

        // username==aaa&password=bbb
        String body = "username=zhangsan&password=123456";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-type", "application/x-www-form-urlencoded")
                .uri(URI.create("http://localhost:" + getPort() + "/api/v1/user"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        User responseUser = objectMapper.readValue(response.body(), User.class);

        assertEquals(201, response.statusCode());
        assertEquals("zhangsan", responseUser.getUsername());
        assertNull(responseUser.getEncryptedPassword());

        // 用该用户登录
        request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-type", "application/x-www-form-urlencoded")
                .uri(URI.create("http://localhost:" + getPort() + "/api/v1/session"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        responseUser = objectMapper.readValue(response.body(), User.class);

        String setCookie = response.headers().firstValue("Set-Cookie").get();

        assertNotNull(setCookie);
        assertEquals(200, response.statusCode());
        assertEquals("zhangsan", responseUser.getUsername());
        assertNull(responseUser.getEncryptedPassword());

        // 确定该用户登录成功
        // 调用注销接口
        // 确定该用户登出
    }

    public void getErrorIfUsernameAlreadyRegister() {
        // 注册用户
        // 成功
        // 再次使用同名用户注册
        // 失败
    }

    public void get401IfNoPermission() {

    }
}
