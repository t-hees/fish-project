package com.tadeo.fish_project.service;

import static com.tadeo.fish_project.util.TestUtils.encodedImage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tadeo.fish_project.exception.InvalidImageException;

class ImageServiceTest {

    @Test
    void testDetectsSupportedFormats() {
        assertEquals("image/png", ImageService.detectMimeType(encodedImage("png")));
        assertEquals("image/jpeg", ImageService.detectMimeType(encodedImage("jpeg")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gif", "bmp"})
    void testRejectsOtherImageFormats(String format) {
        assertThrows(InvalidImageException.class, () -> ImageService.detectMimeType(encodedImage(format)));
    }

    @Test
    void testRejectsNonImageData() {
        assertThrows(InvalidImageException.class, () -> ImageService.detectMimeType("<svg></svg>".getBytes()));
        assertThrows(InvalidImageException.class, () -> ImageService.detectMimeType(new byte[0]));
    }
}
