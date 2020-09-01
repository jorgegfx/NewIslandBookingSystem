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

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class GatewayApplication {


    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator newIslandRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        String apiPath = uriConfiguration.getApiPath();
        return builder.routes()
                .route(p -> p
                        .path(apiPath+"/user")
                        .uri(uriConfiguration.getUserServiceUrl()+"/user"))
                .route(p -> p
                        .path(apiPath+"/campsite")
                        .uri(uriConfiguration.getCampsiteCatalogServiceUrl()+"/campsite"))
                .route(p -> p
                        .path(apiPath+"/reservation")
                        .uri(uriConfiguration.getReservationServiceUrl()+"/reservation"))
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

    @Value("${api-path}")
    private String apiPath;

}