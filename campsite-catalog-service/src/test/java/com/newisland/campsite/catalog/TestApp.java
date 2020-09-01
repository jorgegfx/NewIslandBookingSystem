package com.newisland.campsite.catalog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.newisland.campsite.catalog.model","com.newisland.campsite.catalog.service","com.newisland.campsite.catalog.controller"})
@SpringBootApplication(scanBasePackages = "com.newisland.campsite.catalog.infrastructure.test")
public class TestApp {

}
