package com.tadeo.fish_project.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.tadeo.fish_project.dto.FishNameMappingDto;
import com.tadeo.fish_project.entity.Fish;
import com.tadeo.fish_project.repository.FishRepository;

@Service
public class FishService {

    @Autowired
    private FishRepository fishRepository;

    public List<FishNameMappingDto> searchByCommonName(String commonName) {
        return fishRepository.searchByCommonName(commonName);
    }

    @Transactional
    public void initializeFishFromCsv() throws Exception {
        if (fishRepository.count() == 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/output.csv"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        Fish newFish = new Fish();
                        // Match elements enclosed in literal "
                        Pattern elementPattern = Pattern.compile("\"[^\"]*\"");
                        Pattern arrayItemPattern = Pattern.compile("'[^']*'");
                        Matcher matcher = elementPattern.matcher(line);
                        List<String> elements = new ArrayList<>();
                        while (matcher.find()) {
                            elements.add(matcher.group().replace("\"", ""));
                        }

                        if (elements.get(0) == "") throw new IllegalArgumentException("Scientific name must exist");
                        newFish.setScientificName(elements.get(0));

                        Set<Fish.Environment> environment = new HashSet<>();
                        matcher = arrayItemPattern.matcher(elements.get(1));
                        while (matcher.find()) {
                            environment.add(Fish.Environment.valueOf(matcher.group().replace("'", "").toUpperCase()));
                        }
                        newFish.setEnvironment(environment);

                        newFish.setOccurence(Fish.Occurence.valueOf(elements.get(2).toUpperCase()));

                        Set<String> commonNames = new HashSet<>();
                        matcher = arrayItemPattern.matcher(elements.get(3));
                        while (matcher.find()) {
                            commonNames.add(matcher.group().replace("'", ""));
                        }
                        newFish.setCommonNames(commonNames);

                        String abbundance = elements.get(4).toUpperCase().replace(" ", "_");
                        if (abbundance != "") {
                            newFish.setAbundance(Fish.Abbundance.valueOf(abbundance));
                        }

                        String maxLength = elements.get(5);
                        if (maxLength != "") {
                            newFish.setMaxLength(maxLength);
                        }

                        fishRepository.save(newFish);
                    } catch (Exception e) {
                        String message = String.format("Failed to persist csv row: %s\n%s", line, e);
                        throw new RuntimeException(message);
                    }
                }
            }
        }
    }
}
