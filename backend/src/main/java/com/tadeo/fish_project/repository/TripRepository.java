package com.tadeo.fish_project.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;

public interface TripRepository extends CrudRepository<Trip, Long> {
    Optional<Trip> findByIdAndUser(Long id, User user);

    Page<Trip> findByUserOrderByTimeDesc(User user, Pageable pageable);
}
