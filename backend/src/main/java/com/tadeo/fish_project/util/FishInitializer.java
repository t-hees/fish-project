package com.tadeo.fish_project.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tadeo.fish_project.service.FishService;

@Component
public class FishInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(FishInitializer.class);

    @Autowired
    private FishService fishService;

    @Override
    public void run(String... args) {
        try {
            fishService.initializeFishFromCsv();
        } catch (Exception e) {
            logger.error("Failed to initialize empty fish table from csv", e);
        }
    }
}
