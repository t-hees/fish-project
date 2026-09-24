package com.tadeo.fish_project;

import static com.tadeo.fish_project.util.TestUtils.encodedImage;
import static com.tadeo.fish_project.util.TestUtils.specialCatchParts;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.tadeo.fish_project.dto.AllCatchesDto;
import com.tadeo.fish_project.dto.EditCatchesDto;
import com.tadeo.fish_project.dto.SpecialCatchDto;
import com.tadeo.fish_project.dto.SpecialCatchWithIdDto;
import com.tadeo.fish_project.dto.TripDto;
import com.tadeo.fish_project.dto.TripReturnDto;
import com.tadeo.fish_project.entity.Trip;
import com.tadeo.fish_project.exception.ApiError;
import com.tadeo.fish_project.util.TestFishUtils;
import com.tadeo.fish_project.util.TestUserAuth;
import com.tadeo.fish_project.util.TestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SpecialCatchIT {

    @Autowired
    TestUserAuth testUserAuth;

    @Autowired
    TestUtils testUtils;

    @Autowired
    TestFishUtils testFishUtils;

    private Long someFishId;
    private Long tripId;

    private String specialCatchesUrl() {
        return "/api/trips/" + tripId + "/special-catches";
    }

    private ResponseEntity<SpecialCatchWithIdDto> postSpecialCatch(String username,
            MultiValueMap<String, Object> parts) {
        return testUserAuth.postMultipartAs(username, specialCatchesUrl(), parts,
            new ParameterizedTypeReference<SpecialCatchWithIdDto>() {});
    }

    private SpecialCatchWithIdDto createSpecialCatch(SpecialCatchDto dto, byte[] image) {
        ResponseEntity<SpecialCatchWithIdDto> response = postSpecialCatch(TestUserAuth.username,
            specialCatchParts(dto, image));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody();
    }

    private ApiError expectRejected(MultiValueMap<String, Object> parts, HttpStatus expectedStatus) {
        ResponseEntity<ApiError> response = testUserAuth.postMultipartAs(TestUserAuth.username,
            specialCatchesUrl(), parts, new ParameterizedTypeReference<ApiError>() {});
        assertEquals(expectedStatus, response.getStatusCode(), String.valueOf(response.getBody()));
        return response.getBody();
    }

    private ResponseEntity<byte[]> getImage(String username, String imageUrl) {
        return testUtils.exchange(imageUrl, HttpMethod.GET, new ParameterizedTypeReference<byte[]>() {}, null,
            testUserAuth.authHeadersFor(username));
    }

    private List<SpecialCatchWithIdDto> getSpecialCatches() {
        return testUserAuth.exchangeRestWithAuth("/api/trips/" + tripId + "/catches", HttpMethod.GET,
            new ParameterizedTypeReference<AllCatchesDto>() {}, null, HttpStatus.OK, "Failed to get catches")
            .specialCatches();
    }

    @BeforeEach
    void initTrip() {
        testUtils.cleanDatabase();
        someFishId = testFishUtils.initializeTestFish();
        tripId = testUserAuth.exchangeRestWithAuth("/api/trips", HttpMethod.POST,
            new ParameterizedTypeReference<TripReturnDto>() {},
            new TripDto("lake", Trip.Environment.LAKE, LocalDateTime.of(2026, 4, 5, 6, 30),
                null, null, null, Set.of(), null),
            HttpStatus.CREATED, "Failed to create trip").id();
    }

    @Test
    void testCreateSpecialCatchWithImage() {
        byte[] png = encodedImage("png");
        ResponseEntity<SpecialCatchWithIdDto> response = postSpecialCatch(TestUserAuth.username,
            specialCatchParts(new SpecialCatchDto(someFishId, 24l, 30l, "some notes"), png));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        SpecialCatchWithIdDto created = response.getBody();
        String catchUrl = specialCatchesUrl() + "/" + created.catchId();
        assertEquals(URI.create(catchUrl), response.getHeaders().getLocation());
        assertEquals(new SpecialCatchWithIdDto(created.catchId(), someFishId, catchUrl + "/image", 24l, 30l,
            "some notes", TestFishUtils.someFishScientificName), created);
        assertEquals(List.of(created), getSpecialCatches(), "Catches should contain the created catch");

        ResponseEntity<byte[]> image = getImage(TestUserAuth.username, created.imageUrl());
        assertEquals(HttpStatus.OK, image.getStatusCode());
        assertArrayEquals(png, image.getBody());
        assertEquals(MediaType.IMAGE_PNG, image.getHeaders().getContentType());
        assertTrue(image.getHeaders().getCacheControl().contains("private"),
            "Images must not be cached by shared caches");
    }

    @Test
    void testImageTypeIsDetectedFromData() {
        // Sent as application/octet-stream without extension, the type comes from the data only
        SpecialCatchWithIdDto created = createSpecialCatch(new SpecialCatchDto(someFishId, null, null, null),
            encodedImage("jpeg"));
        assertEquals(MediaType.IMAGE_JPEG, getImage(TestUserAuth.username, created.imageUrl())
            .getHeaders().getContentType());
    }

    @Test
    void testCreateSpecialCatchesWithoutImage() {
        SpecialCatchDto withoutImage = new SpecialCatchDto(someFishId, null, null, null);
        createSpecialCatch(withoutImage, null);
        createSpecialCatch(withoutImage, null);

        List<SpecialCatchWithIdDto> specialCatches = getSpecialCatches();
        assertEquals(2, specialCatches.size(), "Several special catches of the same fish should be possible");
        assertTrue(specialCatches.stream().allMatch(c -> c.imageUrl() == null));

        ResponseEntity<ApiError> noImage = testUtils.exchange(specialCatchesUrl() + "/"
            + specialCatches.getFirst().catchId() + "/image", HttpMethod.GET,
            new ParameterizedTypeReference<ApiError>() {}, null, testUserAuth.authHeadersFor(TestUserAuth.username));
        assertEquals(HttpStatus.NOT_FOUND, noImage.getStatusCode());
    }

    @Test
    void testRemoveSpecialCatch() {
        SpecialCatchWithIdDto created = createSpecialCatch(new SpecialCatchDto(someFishId, null, null, null),
            encodedImage("png"));

        testUserAuth.exchangeRestWithAuth("/api/trips/" + tripId + "/catches", HttpMethod.PUT,
            new ParameterizedTypeReference<AllCatchesDto>() {},
            new EditCatchesDto(List.of(), List.of(created.catchId())), HttpStatus.OK, "Failed to remove catch");

        assertTrue(getSpecialCatches().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND, getImage(TestUserAuth.username, created.imageUrl()).getStatusCode());
    }

    @Test
    void testRejectsUnsupportedImages() {
        for (byte[] data : List.of("<svg onload=\"alert(1)\"></svg>".getBytes(), encodedImage("gif"))) {
            ApiError error = expectRejected(specialCatchParts(new SpecialCatchDto(someFishId, null, null, null), data),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertEquals("UNSUPPORTED_IMAGE", error.code());
        }
        assertTrue(getSpecialCatches().isEmpty(), "Rejected catches must not be saved");
    }

    @Test
    void testRejectsTooLargeImages() {
        byte[] tooLarge = new byte[10 * 1024 * 1024 + 1];
        ApiError error = expectRejected(specialCatchParts(new SpecialCatchDto(someFishId, null, null, null), tooLarge),
            HttpStatus.PAYLOAD_TOO_LARGE);
        assertEquals("PAYLOAD_TOO_LARGE", error.code());
        assertTrue(getSpecialCatches().isEmpty(), "Rejected catches must not be saved");
    }

    @Test
    void testRejectsInvalidCatches() {
        ApiError invalid = expectRejected(specialCatchParts(new SpecialCatchDto(someFishId, -1l, null, null), null),
            HttpStatus.BAD_REQUEST);
        assertEquals("VALIDATION_FAILED", invalid.code());

        ApiError unknownFish = expectRejected(specialCatchParts(new SpecialCatchDto(-1l, null, null, null), null),
            HttpStatus.NOT_FOUND);
        assertEquals("ENTITY_NOT_FOUND", unknownFish.code());

        MultiValueMap<String, Object> withoutCatch = new LinkedMultiValueMap<>();
        withoutCatch.add("image", specialCatchParts(new SpecialCatchDto(someFishId, null, null, null),
            encodedImage("png")).getFirst("image"));
        expectRejected(withoutCatch, HttpStatus.BAD_REQUEST);
        assertTrue(getSpecialCatches().isEmpty(), "Rejected catches must not be saved");
    }

    @Test
    void testOtherUserCannotAccessSpecialCatches() {
        SpecialCatchWithIdDto created = createSpecialCatch(new SpecialCatchDto(someFishId, null, null, null),
            encodedImage("png"));
        String other = TestUserAuth.otherUsername;

        assertEquals(HttpStatus.NOT_FOUND, getImage(other, created.imageUrl()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, postSpecialCatch(other,
            specialCatchParts(new SpecialCatchDto(someFishId, null, null, null), null)).getStatusCode());
        assertEquals(1, getSpecialCatches().size());
    }

    @Test
    void testImagesRequireAuth() {
        SpecialCatchWithIdDto created = createSpecialCatch(new SpecialCatchDto(someFishId, null, null, null),
            encodedImage("png"));
        assertEquals(HttpStatus.UNAUTHORIZED, testUtils.exchange(created.imageUrl(), HttpMethod.GET,
            new ParameterizedTypeReference<byte[]>() {}, null, new HttpHeaders()).getStatusCode());
    }
}
