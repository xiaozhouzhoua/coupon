package com.core;

import org.springframework.boot.Banner.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SettlementApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SettlementApplication.class)
                .bannerMode(Mode.OFF)
                .run(args);
    }
}
