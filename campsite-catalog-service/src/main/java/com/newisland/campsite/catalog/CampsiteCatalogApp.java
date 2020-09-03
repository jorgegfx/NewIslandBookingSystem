package com.newisland.campsite.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CampsiteCatalogApp {
    public static void main(String[] args) {
        SpringApplication.run(CampsiteCatalogApp.class, args);
    }
}
