package com.tadeo.fish_project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
class FishIntTest {

    @Autowired
    FishService fishService;

    @Autowired
    TestUtils testUtils;

    @Autowired
    TestFishUtils testFishUtils;

    @BeforeEach
    void initializeTestFish() {
        testFishUtils.initializeTestFish();
    }

    @Test
    void testSearchByCommonName() {
        List<FishNameMappingDto> fishList = testUtils.exchangeRest(
            "/api/fish/search_by_common_name?name=aal", HttpMethod.GET,
            new ParameterizedTypeReference<List<FishNameMappingDto>>() {},
            HttpStatus.OK, "Failed to search fish by name"
        );
        assertEquals(
            List.of("Aalmutter", "Meeraal", "Congeraal", "Kleiner Sandaal", "Gemeiner Meeraal"),
            fishList.stream().map((fish) -> {
                return fish.commonName();
            }).collect(Collectors.toList()),
            "Search result doesn't match expected values in correct order"
        );
    }

    @Test
    void testInitializeFishFromCsv() {
        ThrowingSupplier<BufferedReader> readerSupplier = () ->
            new BufferedReader(new InputStreamReader((new ClassPathResource("output.csv")).getInputStream()));
        assertDoesNotThrow(() -> fishService.initializeFromReader(readerSupplier), "Failed to initialze fish from csv");
    }


}
