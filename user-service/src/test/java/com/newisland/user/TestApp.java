package com.newisland.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.newisland.user.model","com.newisland.user.service","com.newisland.user.controller"})
@SpringBootApplication(scanBasePackages = "com.newisland.user.infrastructure.test")
public class TestApp {

}
