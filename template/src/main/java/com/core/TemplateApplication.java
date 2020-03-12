package com.core;

import org.springframework.boot.Banner.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class TemplateApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(TemplateApplication.class)
				.bannerMode(Mode.OFF)
				.run(args);
	}
}
