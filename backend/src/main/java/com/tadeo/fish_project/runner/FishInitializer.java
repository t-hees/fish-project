package com.tadeo.fish_project.runner;

import java.io.BufferedReader;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tadeo.fish_project.repository.FishRepository;

@Component
public class FishInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(FishInitializer.class);

    @Autowired
    FishRepository fishRepository;

    @Override
    public void run(String... args) throws Exception {
        if (fishRepository.count() == 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/output.csv"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // TODO: implement
                }
            }
        }
    }
}
