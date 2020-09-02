package com.newisland.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-k8s.properties")
@Profile("k8s")
public class KubernetesConfiguration {
}
