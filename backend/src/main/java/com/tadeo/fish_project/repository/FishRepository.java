package com.tadeo.fish_project.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tadeo.fish_project.dto.FishNameMappingDto;
import com.tadeo.fish_project.entity.Fish;

public interface FishRepository extends CrudRepository<Fish, Long> {
    Optional<Fish> findById(Long id);
    Optional<Fish> findByScientificName(String scientificName);

    // NOTE: INSTR depends on mysql/mariadb!
    @Query(value = "SELECT id, scientific_name, common_names " +
                   "FROM fish f JOIN fish_common_names fc ON f.id = fc.fish_id " +
                   "WHERE fc.common_names LIKE %:name% " +
                   "ORDER BY INSTR(common_names, :name), common_names",
        nativeQuery = true)
    List<FishNameMappingDto> searchByCommonName(@Param("name") String commonName);
}
