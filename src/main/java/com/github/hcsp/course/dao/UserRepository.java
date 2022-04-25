package com.github.hcsp.course.dao;

import com.github.hcsp.course.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    //@Query("SELECT u FROM User u where u.username = ?1 and u.encryptedPassword = ?2")
    User findUsersByUsername(String username);
}
