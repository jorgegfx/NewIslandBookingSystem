package com.newisland.user.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.newisland.user.repository")
@PropertySource("h2.properties")
@EnableTransactionManagement
public class H2JpaConfig {
}
