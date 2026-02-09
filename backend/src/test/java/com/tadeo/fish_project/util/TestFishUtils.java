package com.tadeo.fish_project.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.BufferedReader;
import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

import com.tadeo.fish_project.repository.FishRepository;
import com.tadeo.fish_project.service.FishService;

@Component
@Profile("test")
public class TestFishUtils {

    @Autowired
    FishRepository fishRepository;

    @Autowired
    FishService fishService;

    public Long initializeTestFish() {
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
        return fishRepository.findByScientificName("Zoarces viviparus").get().getId();
    }
}
