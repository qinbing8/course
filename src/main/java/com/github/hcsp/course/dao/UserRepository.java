package com.github.hcsp.course.dao;

import com.github.hcsp.course.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "select * from users where id <> 2", nativeQuery = true)
    List<User> findUserWhoseIdNotEqual2();
}
