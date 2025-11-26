package com.tadeo.fish_project.repository;

import com.tadeo.fish_project.entity.User;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
