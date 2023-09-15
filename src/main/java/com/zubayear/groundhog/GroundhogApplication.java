package com.zubayear.groundhog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GroundhogApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroundhogApplication.class, args);
	}

}
