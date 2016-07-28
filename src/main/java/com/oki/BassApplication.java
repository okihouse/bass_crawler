package com.oki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@IntegrationComponentScan
@EnableScheduling
//@ImportResource(value = "classpath:integration.xml")
public class BassApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		String profile = System.getProperty("spring.profiles.active");
		if (profile == null) {
			System.setProperty("spring.profiles.active", "local");
		}
		SpringApplication.run(BassApplication.class, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    	return builder.sources(BassApplication.class);
    }
	
}
