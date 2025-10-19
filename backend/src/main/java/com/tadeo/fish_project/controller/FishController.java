package com.tadeo.fish_project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tadeo.fish_project.dto.FishNameMappingDto;
import com.tadeo.fish_project.service.FishService;

import java.util.List;

@RestController
@RequestMapping("/api/fish")
public class FishController {
    @Autowired
    private FishService fishService;

    @GetMapping("/search_by_common_name")
    public ResponseEntity<List<FishNameMappingDto>> searchByCommonName(@RequestParam String name) {
        return ResponseEntity.ok(fishService.searchByCommonName(name));
    }
}
