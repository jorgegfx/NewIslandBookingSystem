package com.newisland.gateway;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gateway Application
 */
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * Routes are build programmatically to support changes on the profile (local, kubernetes ...)
     * but is possible as well to have all routes in a application.yml
     */
    @Bean
    public RouteLocator newIslandRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        return builder.routes()
                .route(p -> p
                        .path("/user/**")
                        .uri(uriConfiguration.getUserServiceUrl()))
                .route(p -> p
                        .path("/campsite/**")
                        .uri(uriConfiguration.getCampsiteCatalogServiceUrl()))
                .route(p -> p
                        .path("/reservation/**")
                        .uri(uriConfiguration.getReservationServiceUrl()))
                .build();
    }

}

@Data
@ConfigurationProperties
class UriConfiguration {

    @Value("${user-service-url}")
    private String userServiceUrl;

    @Value("${reservation-service-url}")
    private String reservationServiceUrl;

    @Value("${campsite-catalog-service}")
    private String campsiteCatalogServiceUrl;

}