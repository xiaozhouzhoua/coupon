package com.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@EnableAdminServer
@SpringBootApplication
public class AdminApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(AdminApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.run(args);
	}
}
