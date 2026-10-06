package com.vocably;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VocablyApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocablyApplication.class, args);
    }
}
