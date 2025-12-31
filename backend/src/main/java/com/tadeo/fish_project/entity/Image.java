package com.tadeo.fish_project.entity;

import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Base64;
import java.util.HexFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
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
    @NotNull
    private byte[] data;

    @NotNull
    private String mimeType;

    public void setBase64Image(String imageData) {
        String[] splitImage = imageData.split(",");
        if (splitImage.length != 2) throw new IllegalArgumentException("Image has no mime type!");
        this.mimeType = splitImage[0];
        this.data = Base64.getDecoder().decode(splitImage[1]);
    }

    public String getBase64Image() {
        return mimeType + "," + Base64.getEncoder().encodeToString(this.data);
    }

    // Deprecated
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
