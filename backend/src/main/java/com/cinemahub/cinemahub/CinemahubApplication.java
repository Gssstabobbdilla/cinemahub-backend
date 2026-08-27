package com.cinemahub.cinemahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemahubApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemahubApplication.class, args);
	}

}
