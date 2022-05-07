package com.github.hcsp.course.dao;

import com.github.hcsp.course.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<Role,Integer> {
}
