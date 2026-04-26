package com.livebeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * [app] LiveBeat 應用程式進入點
 *
 * 負責：啟動 Spring Boot 應用程式，啟用 @ConfigurationProperties 掃描
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LivebeatApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivebeatApplication.class, args);
    }
}
