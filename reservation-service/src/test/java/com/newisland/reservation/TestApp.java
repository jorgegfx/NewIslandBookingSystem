package com.newisland.reservation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.newisland.reservation.model","com.newisland.reservation.service","com.newisland.reservation.controller"})
@SpringBootApplication(scanBasePackages = "com.newisland.reservation.infrastructure.test")
public class TestApp {

}
