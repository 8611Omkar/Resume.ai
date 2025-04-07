package com.example.Resume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        
        // Mock successful response
        ResponseEntity<String> successResponse = new ResponseEntity<>(
            "{\"choices\":[{\"text\":\"Generated Resume Content\"}]}",
            HttpStatus.OK
        );
        
        // Mock error response
        ResponseEntity<String> errorResponse = new ResponseEntity<>(
            "{\"error\":\"Test error\"}",
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        
        // Configure mock behavior
        Mockito.when(restTemplate.postForEntity(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.eq(String.class)
        )).thenReturn(successResponse);
        
        return restTemplate;
    }
}