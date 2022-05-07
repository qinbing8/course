package com.github.hcsp.course;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.hcsp.course.model.PageResponse;
import com.github.hcsp.course.model.Role;
import com.github.hcsp.course.model.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void get401IfNotLogIn() throws IOException, InterruptedException {
        assertEquals(401, get("/user").statusCode());
        assertEquals(401, get("/user?search=aaa&pageSize=10").statusCode());
        assertEquals(401, get("/user/10").statusCode());
    }

    @Test
    public void get403IfNotAdmin() throws IOException, InterruptedException {
        assertEquals(403, get("/user", studentUserCookie).statusCode());
        assertEquals(403, get("/user", teacherUserCookie).statusCode());
        assertEquals(403, get("/user/123", studentUserCookie).statusCode());
        assertEquals(403, get("/user/123", teacherUserCookie).statusCode());
        assertEquals(403, patch("/user/123", "{}", Map.of("Cookie", studentUserCookie)).statusCode());
        assertEquals(403, patch("/user/123", "{}", Map.of("Cookie", teacherUserCookie)).statusCode());
    }

    @Test
    public void adminCanGetAllUsers() throws IOException, InterruptedException {
        String body = get("/user?pageSize=1&pageNum=2&orderBy=id&orderType=Asc", adminUserCookie).body();
        PageResponse<User> pageResponse = objectMapper.readValue(body, new TypeReference<PageResponse<User>>() {
        });

        assertEquals(1, pageResponse.getPageSize().intValue());
        assertEquals(2, pageResponse.getPageNum().intValue());
        assertEquals(3, pageResponse.getTotalPage().intValue());
        assertEquals(1, pageResponse.getData().size());
        assertEquals("Teacher2", pageResponse.getData().get(0).getUsername());
    }

    @Test
    public void adminCanSearchUser() throws IOException, InterruptedException {
        String body = get("/user?pageSize=100&pageNum=1&search=e&orderBy=id&orderType=Desc", adminUserCookie).body();
        PageResponse<User> pageResponse = objectMapper.readValue(body, new TypeReference<>() {
        });

        assertEquals(100, pageResponse.getPageSize().intValue());
        assertEquals(1, pageResponse.getPageNum().intValue());
        assertEquals(1, pageResponse.getTotalPage().intValue());
        assertEquals(2, pageResponse.getData().size());
        assertEquals(Arrays.asList("Teacher2", "Student1"),
                pageResponse.getData().stream().map(User::getUsername).collect(toList()));
    }

    @Test
    public void adminCanGetOneUser() throws IOException, InterruptedException {
        String studentJson = get("/user/2", adminUserCookie).body();
        User user = objectMapper.readValue(studentJson, User.class);

        assertEquals("Teacher2",user.getUsername());
        assertEquals(List.of("老师"),user.getRoles().stream().map(Role::getName).collect(toList()));
    }

    @Test
    public void get404IfUserNotExits() throws IOException, InterruptedException {
        assertEquals(404,get("/user/0",adminUserCookie).statusCode());

    }

    @Test
    public void adminCanUpdateUserRole() throws IOException, InterruptedException {
        String studentJson = get("/user/1", adminUserCookie).body();
        User student = objectMapper.readValue(studentJson, User.class);
        Role role = new Role();
        role.setName("管理员");
        student.getRoles().add(role);

        int statusCode = patch("/user/1",
                objectMapper.writeValueAsString(student),
                Map.of("Cookie", adminUserCookie)).statusCode();

        assertEquals(200, statusCode);

        // 再次获取，现在用户已经是管理员了
        studentJson = get("/user/1", adminUserCookie).body();
        student = objectMapper.readValue(studentJson, User.class);

        assertTrue(student.getRoles().stream().anyMatch(r -> "管理员".equals(r.getName())));
    }
}
