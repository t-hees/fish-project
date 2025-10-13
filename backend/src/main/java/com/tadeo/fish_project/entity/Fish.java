package com.tadeo.fish_project.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Fish {

    public static enum Environment {
        FRESHWATER,
        BRAKISH,
        MARINE,
    }

    public static enum Occurence {
        NATIVE,
        INTRODUCED,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String scientificName;

    @NotEmpty
    @ElementCollection
    private Set<String> commonNames;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Environment environment;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Occurence occurence;

    private String abundance;

    private String maxLength;

}
