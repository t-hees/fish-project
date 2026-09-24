package com.tadeo.fish_project.entity;

import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "image")
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @NotNull
    private byte[] data;

    // Detected from the data on upload, see ImageService
    @NotNull
    private String mimeType;

    public Image(byte[] data, String mimeType) {
        this(null, data, mimeType);
    }
}
