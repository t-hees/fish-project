package com.tadeo.fish_project.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.tadeo.fish_project.entity.Fish;

public interface FishRepository extends CrudRepository<Fish, Long> {
    Optional<Fish> findById(Long id);
    Optional<Fish> findByScientificName(String scientificName);
}
