package com.tadeo.fish_project;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SecurityNoAuthTestConfig.class)
@ActiveProfiles({"test", "no_auth"})
class FishIntTest {

    @Autowired
    FishService fishService;

    @Autowired
    FishRepository fishRepository;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void initializeTestFish() {
        fishRepository.deleteAll();
        String fishData = """
"Trisopterus esmarkii","['marine']","native","['Stintdorsch', 'Sparling']","scarce","35.00 cm TL male/unsexed"
"Conger conger","['brakish', 'marine']","native","['Meeraal', 'Conger', 'Congeraal', 'Gemeiner Meeraal']","scarce","300 cm TL male/unsexed"
"Zoarces viviparus","['brakish', 'marine']","native","['Aalmutter']","common","52.00 cm TL male/unsexed"
"Ammodytes marinus","['brakish', 'marine']","native","['Kleiner Sandaal', 'Tobiasfisch', 'Tobis']","scarce","25.00 cm TL male/unsexed"
        """;

        ThrowingSupplier<BufferedReader> readerSupplier = () ->
            new BufferedReader(new StringReader(fishData));
        assertDoesNotThrow(() -> fishService.initializeFromReader(readerSupplier));
    }

    @Test
    void testSearchByCommonName() {
        ParameterizedTypeReference<List<FishNameMappingDto>> resultType = new ParameterizedTypeReference<List<FishNameMappingDto>>() {};
        ResponseEntity<List<FishNameMappingDto>> response = restTemplate.exchange("/api/fish/search_by_common_name?name=aal",
            HttpMethod.GET,
            null,
            resultType);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody(), "Response has no body");
        assertEquals(
            "Search result doesn't match expected values in correct order",
            List.of("Aalmutter", "Meeraal", "Congeraal", "Kleiner Sandaal", "Gemeiner Meeraal"),
            response.getBody().stream().map((fish) -> {
                return fish.commonName();
            }).collect(Collectors.toList())
        );
    }

    @Test
    void testInitializeFishFromCsv() {
        ThrowingSupplier<BufferedReader> readerSupplier = () ->
            new BufferedReader(new InputStreamReader((new ClassPathResource("output.csv")).getInputStream()));
        assertDoesNotThrow(() -> fishService.initializeFromReader(readerSupplier), "Failed to initialze fish from csv");
    }


}
