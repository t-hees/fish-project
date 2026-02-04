package com.tadeo.fish_project.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;

import com.tadeo.fish_project.service.FishService;

@Component
@Profile("!test")
public class FishInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(FishInitializer.class);

    @Autowired
    private FishService fishService;

    @Override
    public void run(String... args) {
        try {
            ThrowingSupplier<BufferedReader> readerSupplier = () ->
                new BufferedReader(new InputStreamReader((new ClassPathResource("output.csv")).getInputStream()));
            fishService.initializeFromReader(readerSupplier);
        } catch (Exception e) {
            logger.error("Failed to initialize empty fish table from csv", e);
        }
    }
}
