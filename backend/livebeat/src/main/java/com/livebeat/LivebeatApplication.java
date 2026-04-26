package com.livebeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LivebeatApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivebeatApplication.class, args);
    }
}
