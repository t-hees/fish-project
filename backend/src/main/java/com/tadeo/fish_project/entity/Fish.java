package com.tadeo.fish_project.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "fish")
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
        ENDEMIC,
    }

    public static enum Abbundance {
        COMMON,
        FAIRLY_COMMON,
        OCCASIONAL,
        SCARCE,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String scientificName;

    @NotNull
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Environment> environment = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private Occurence occurence;

    @NotNull
    @ElementCollection
    @CollectionTable(name = "fish_common_names", joinColumns = @JoinColumn(name = "fish_id"))
    private Set<String> commonNames = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Abbundance abundance;

    // This is currently a string because it contains mixed information
    // about female/male/unsexed fish.
    private String maxLength;

}
