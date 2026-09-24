CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `authorities` varbinary(255) DEFAULT NULL,
  `account_non_expired` bit(1) NOT NULL,
  `account_non_locked` bit(1) NOT NULL,
  `credentials_non_expired` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB;

CREATE TABLE `fish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `scientific_name` varchar(255) NOT NULL,
  `occurence` enum('ENDEMIC','INTRODUCED','NATIVE') NOT NULL,
  `abundance` enum('COMMON','FAIRLY_COMMON','OCCASIONAL','SCARCE') DEFAULT NULL,
  `max_length` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fish_scientific_name` (`scientific_name`)
) ENGINE=InnoDB;

CREATE TABLE `fish_common_names` (
  `fish_id` bigint NOT NULL,
  `common_names` varchar(255) DEFAULT NULL,
  CONSTRAINT `fk_fish_common_names_fish` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `fish_environment` (
  `fish_id` bigint NOT NULL,
  `environment` enum('BRAKISH','FRESHWATER','MARINE') DEFAULT NULL,
  CONSTRAINT `fk_fish_environment_fish` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mime_type` varchar(255) NOT NULL,
  `data` longblob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `simple_catch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fish_id` bigint DEFAULT NULL,
  `amount` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_simple_catch_fish` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `special_catch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fish_id` bigint DEFAULT NULL,
  `image_id` bigint DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `weight` bigint DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_special_catch_image` (`image_id`),
  CONSTRAINT `fk_special_catch_fish` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),
  CONSTRAINT `fk_special_catch_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `trip` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `environment` enum('LAKE','OCEAN','RIVER') DEFAULT NULL,
  `time` datetime(6) NOT NULL,
  -- java.time.Duration in nanoseconds
  `duration` decimal(21,0) DEFAULT NULL,
  `temperature` bigint DEFAULT NULL,
  `water_level` bigint DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  -- Trips go with their user, UserService.delete removes them through JPA as well so their catches cascade
  CONSTRAINT `fk_trip_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE `trip_weather` (
  `trip_id` bigint NOT NULL,
  `weather` enum('CLEAR_SKY','CLOUDY','DAWN','DUSK','FOGGY','FREEZING','HEAVY_RAIN','HUMID','LIGHT_RAIN',
    'LIGHT_WIND','OVERCAST','PARTLY_CLOUDY','SNOW','STORM','STRONG_WIND','THUNDERSTORM') DEFAULT NULL,
  CONSTRAINT `fk_trip_weather_trip` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `trip_simple_catches` (
  `trip_id` bigint NOT NULL,
  `simple_catches_id` bigint NOT NULL,
  PRIMARY KEY (`simple_catches_id`, `trip_id`),
  UNIQUE KEY `uk_trip_simple_catches_catch` (`simple_catches_id`),
  CONSTRAINT `fk_trip_simple_catches_trip` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`id`),
  CONSTRAINT `fk_trip_simple_catches_catch` FOREIGN KEY (`simple_catches_id`) REFERENCES `simple_catch` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `trip_special_catches` (
  `trip_id` bigint NOT NULL,
  `special_catches_id` bigint NOT NULL,
  PRIMARY KEY (`special_catches_id`, `trip_id`),
  UNIQUE KEY `uk_trip_special_catches_catch` (`special_catches_id`),
  CONSTRAINT `fk_trip_special_catches_trip` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`id`),
  CONSTRAINT `fk_trip_special_catches_catch` FOREIGN KEY (`special_catches_id`) REFERENCES `special_catch` (`id`)
) ENGINE=InnoDB;
