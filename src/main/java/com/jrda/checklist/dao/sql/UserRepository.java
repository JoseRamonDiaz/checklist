package com.jrda.checklist.dao.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    List<AppUser> findByName(String name);

    List<AppUser> findByEmail(String email);
}
