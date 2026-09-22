package com.tadeo.fish_project.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tadeo.fish_project.entity.Fish;
import com.tadeo.fish_project.repository.FishRepository;

@ExtendWith(MockitoExtension.class)
class FishServiceTest {

    @Mock
    FishRepository fishRepository;

    @InjectMocks
    FishService fishService;

    private static final String congerRow =
        "\"Conger conger\",\"['brakish', 'marine']\",\"native\",\"['Meeraal', 'Conger']\",\"scarce\",\"300 cm TL male/unsexed\"";

    @Test
    void testParseCsvLine() {
        Fish fish = FishService.parseCsvLine(congerRow);
        assertAll(
            () -> assertEquals("Conger conger", fish.getScientificName()),
            () -> assertEquals(Set.of(Fish.Environment.BRAKISH, Fish.Environment.MARINE), fish.getEnvironment()),
            () -> assertEquals(Fish.Occurence.NATIVE, fish.getOccurence()),
            () -> assertEquals(Set.of("Meeraal", "Conger"), fish.getCommonNames()),
            () -> assertEquals(Fish.Abbundance.SCARCE, fish.getAbundance()),
            () -> assertEquals("300 cm TL male/unsexed", fish.getMaxLength())
        );
    }

    @Test
    void testParseCsvLineWithMultiWordAbundance() {
        Fish fish = FishService.parseCsvLine(
            "\"Salmo trutta\",\"['freshwater']\",\"native\",\"['Forelle']\",\"fairly common\",\"140 cm\"");
        assertEquals(Fish.Abbundance.FAIRLY_COMMON, fish.getAbundance());
    }

    @Test
    void testParseCsvLineWithEmptyOptionalColumns() {
        Fish fish = FishService.parseCsvLine(
            "\"Salmo trutta\",\"['freshwater']\",\"introduced\",\"[]\",\"\",\"\"");
        assertAll(
            () -> assertEquals(Fish.Occurence.INTRODUCED, fish.getOccurence()),
            () -> assertTrue(fish.getCommonNames().isEmpty()),
            () -> assertNull(fish.getAbundance()),
            () -> assertNull(fish.getMaxLength())
        );
    }

    @Test
    void testParseCsvLineRejectsInvalidRows() {
        assertThrows(IllegalArgumentException.class, () -> FishService.parseCsvLine(
            "\"\",\"['marine']\",\"native\",\"['Aal']\",\"\",\"\""), "Missing scientific name");
        assertThrows(IllegalArgumentException.class, () -> FishService.parseCsvLine(
            "\"Conger conger\",\"['marine']\",\"unknown\",\"['Aal']\",\"\",\"\""), "Unknown occurence");
        assertThrows(IndexOutOfBoundsException.class, () -> FishService.parseCsvLine(
            "\"Conger conger\",\"['marine']\""), "Incomplete row");
    }

    @Test
    void testInitializeFromReaderSavesEveryRow() throws Exception {
        when(fishRepository.count()).thenReturn(0l);
        fishService.initializeFromReader(() -> new BufferedReader(new StringReader(congerRow + "\n" + congerRow)));
        verify(fishRepository, times(2)).save(any(Fish.class));
    }

    @Test
    void testInitializeFromReaderSkipsFilledTable() throws Exception {
        when(fishRepository.count()).thenReturn(1l);
        fishService.initializeFromReader(() -> {
            throw new AssertionError("Reader must not be opened for a filled table");
        });
        verify(fishRepository, never()).save(any());
    }

    @Test
    void testInitializeFromReaderReportsBrokenRow() {
        when(fishRepository.count()).thenReturn(0l);
        String brokenRow = "\"Conger conger\",\"['marine']\"";
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> fishService.initializeFromReader(() -> new BufferedReader(new StringReader(brokenRow))));
        assertTrue(exception.getMessage().contains(brokenRow), "Error should name the broken row");
    }

    @Test
    void testSearchByCommonNameEscapesLikeWildcards() {
        fishService.searchByCommonName("100%_\\");
        verify(fishRepository).searchByCommonName("100\\%\\_\\\\");
    }
}
