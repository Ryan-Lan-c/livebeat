package com.livebeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * [app] LiveBeat 應用程式進入點
 *
 * 負責：啟動 Spring Boot 應用程式，啟用 @ConfigurationProperties 掃描與排程任務
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class LivebeatApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivebeatApplication.class, args);
    }
}
