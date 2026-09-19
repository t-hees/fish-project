package com.tadeo.fish_project.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.entity.User;

public interface TripRepository extends CrudRepository<Trip, Long> {
    Optional<Trip> findByIdAndUser(Long id, User user);

    @Query("SELECT t FROM Trip t WHERE t.user = :user "
        + "AND (:location IS NULL OR LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))) "
        + "AND (:environment IS NULL OR t.environment = :environment) "
        + "AND (:from IS NULL OR t.time >= :from) "
        + "AND (:to IS NULL OR t.time <= :to) "
        + "ORDER BY t.time DESC")
    Page<Trip> search(@Param("user") User user, @Param("location") String location,
        @Param("environment") Trip.Environment environment,
        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
        Pageable pageable);
}
