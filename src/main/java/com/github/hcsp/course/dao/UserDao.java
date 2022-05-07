package com.github.hcsp.course.dao;

import com.github.hcsp.course.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserDao extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u where u.username like %:search%")
    Page<User> finBySearch(String search, Pageable pageable);

    User findUsersByUsername(String username);
}
