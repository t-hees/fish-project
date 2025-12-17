package com.tadeo.fish_project.entity;

import jakarta.persistence.Table;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Table(name = "image")
@Entity
public class Image {

    public static enum ImageType {
        PNG,
        UNKNOWN,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] data;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    public static ImageType getImageType(byte[] imageData) {
        record ImageSignaturePair(ImageType type, byte[] signature){};
        ImageSignaturePair[] signatureMapping = {
            new ImageSignaturePair(ImageType.PNG, HexFormat.of().parseHex("89504E470D0A1A0A")),
        };

        for (ImageSignaturePair imageSignaturePair : signatureMapping) {
            boolean matches = true;
            for (int i = 0; i < imageSignaturePair.signature().length; i++) {
                if (imageSignaturePair.signature()[i] != imageData[i]) {
                    matches = false;
                    break;
                }
            }
            if (matches) return imageSignaturePair.type();
        }
        return ImageType.UNKNOWN;
    }
}
