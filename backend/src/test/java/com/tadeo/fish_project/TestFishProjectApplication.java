package com.tadeo.fish_project;

import org.springframework.boot.SpringApplication;

public class TestFishProjectApplication {

	public static void main(String[] args) {
		SpringApplication.from(FishProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
