package com.github.hcsp.course.service;

import com.github.hcsp.course.annotation.Admin;
import com.github.hcsp.course.dao.RoleDao;
import com.github.hcsp.course.dao.UserDao;
import com.github.hcsp.course.model.HttpException;
import com.github.hcsp.course.model.PageResponse;
import com.github.hcsp.course.model.Role;
import com.github.hcsp.course.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    RoleDao roleDao;

    @Admin
    public PageResponse<User> getAllUsers(String search,
                                          Integer pageSize,
                                          Integer pageNum,
                                          String orderBy,
                                          String orderType) {
        if (orderBy != null && orderType == null) {
            orderType = "Asc";
        }
        Pageable pageable = orderBy == null
                ? PageRequest.of(pageNum - 1, pageSize)
                : PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.fromString(orderType),
                        orderBy));

        Page<User> page = ObjectUtils.isEmpty(search) ? userDao.findAll(pageable)
                : userDao.finBySearch(search, pageable);
        return new PageResponse<>(page.getTotalPages(), pageNum, pageSize, page.toList());
    }

    @Admin
    public User getUser(Integer id) {
        return userDao
                .findById(id)
                .orElseThrow(() -> new HttpException(404, "用户不存在"));
    }

    @Admin
    public User updateUser(Integer id, User user) {
        Map<String, Role> nameToRoleMap = roleDao.findAll().stream().collect(
                Collectors.toMap(Role::getName, r -> r)
        );

        user.getRoles().forEach(role -> role.setId(
                nameToRoleMap.get(role.getName()).getId()
        ));

        User userInDatabase = getUser(id);
        userInDatabase.setRoles(user.getRoles());
        userDao.save(userInDatabase);
        return userInDatabase;
    }
}
