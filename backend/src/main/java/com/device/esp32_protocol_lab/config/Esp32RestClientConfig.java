package com.device.esp32_protocol_lab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class Esp32RestClientConfig {

    @Bean
    public RestClient esp32RestClient(@Value("${esp32.base-url:http://192.168.0.236}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
