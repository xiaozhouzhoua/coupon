package com.gateway;

import org.springframework.boot.Banner.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class GatewayApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(GatewayApplication.class)
				.bannerMode(Mode.OFF)
				.run(args);
	}
}
