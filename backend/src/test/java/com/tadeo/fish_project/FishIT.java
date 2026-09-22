package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.function.ThrowingSupplier;

import com.tadeo.fish_project.config.SecurityNoAuthTestConfig;
import com.tadeo.fish_project.dto.FishNameMappingDto;
import com.tadeo.fish_project.repository.FishRepository;
import com.tadeo.fish_project.service.FishService;
import com.tadeo.fish_project.util.TestFishUtils;
import com.tadeo.fish_project.util.TestUtils;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SecurityNoAuthTestConfig.class)
@ActiveProfiles({"test", "no_auth"})
class FishIT {

    @Autowired
    FishService fishService;

    @Autowired
    FishRepository fishRepository;

    @Autowired
    TestUtils testUtils;

    @Autowired
    TestFishUtils testFishUtils;

    @BeforeEach
    void initializeTestFish() {
        testUtils.cleanDatabase();
        testFishUtils.initializeTestFish();
    }

    private List<String> searchCommonNames(String name) {
        List<FishNameMappingDto> fishList = testUtils.exchangeRest(
            "/api/fish/search_by_common_name?name=" + name, HttpMethod.GET,
            new ParameterizedTypeReference<List<FishNameMappingDto>>() {},
            HttpStatus.OK, "Failed to search fish by name"
        );
        return fishList.stream().map(FishNameMappingDto::commonName).toList();
    }

    @Test
    void testSearchByCommonName() {
        assertEquals(
            List.of("Aalmutter", "Meeraal", "Congeraal", "Kleiner Sandaal", "Gemeiner Meeraal"),
            searchCommonNames("aal"),
            "Search result doesn't match expected values in correct order"
        );
    }

    @Test
    void testSearchByCommonNameIsCaseInsensitive() {
        assertEquals(searchCommonNames("aal"), searchCommonNames("AAL"));
    }

    @Test
    void testSearchByCommonNameWithoutMatch() {
        assertTrue(searchCommonNames("nonexistent").isEmpty());
    }

    @Test
    void testSearchByCommonNameTreatsWildcardsLiterally() {
        assertTrue(searchCommonNames("%").isEmpty(), "'%' must not match every fish");
        assertTrue(searchCommonNames("_").isEmpty(), "'_' must not match every fish");
    }

    @Test
    void testInitializeFishFromCsv() {
        fishRepository.deleteAll();
        ThrowingSupplier<BufferedReader> readerSupplier = () ->
            new BufferedReader(new InputStreamReader((new ClassPathResource("output.csv")).getInputStream()));
        assertDoesNotThrow(() -> fishService.initializeFromReader(readerSupplier), "Failed to initialze fish from csv");

        assertTrue(fishRepository.count() > 100, "Expected the full fish list to be imported");
        List<FishNameMappingDto> eel = fishService.searchByCommonName("Aalpricken");
        assertEquals(1, eel.size());
        assertEquals("Anguilla anguilla", eel.getFirst().scientificName());
    }
}
