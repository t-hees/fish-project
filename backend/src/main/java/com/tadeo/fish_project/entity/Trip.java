package com.tadeo.fish_project.entity;

import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ForeignKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

/*
Describes a single fishing trip
*/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "trip")
@Entity
public class Trip {

    public static enum Environment {
        RIVER,
        LAKE,
        OCEAN,
    }

    public static enum Weather {
        CLEAR_SKY,
        PARTLY_CLOUDY,
        CLOUDY,
        OVERCAST,
        FOGGY,
        HUMID,
        THUNDERSTORM,
        LIGHT_RAIN,
        HEAVY_RAIN,
        LIGHT_WIND,
        STRONG_WIND,
        STORM,
        FREEZING,
        SNOW,
        DUSK,
        DAWN,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SimpleCatch> simpleCatches;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SpecialCatch> specialCatches;

    @ManyToOne
    @JoinColumn(
        name = "user_id",
        foreignKey = @ForeignKey(
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE"
        )
    )
    private User user;

    // TODO: Use entity for location
    private String location;

    private Environment environment;

    private LocalDateTime time;

    private Duration duration;

    private Long temperature;

    private Long waterLevel;

    private Set<Weather> weather;

    private String notes;

    // TODO: Fishing gear (possibly new entity)
}
