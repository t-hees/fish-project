package com.tadeo.fish_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;

public interface TripRepository extends CrudRepository<Trip, Long> {
    Optional<Trip> findById(Long id);

    List<Trip> findByUserOrderByTimeDesc(User user);
}
