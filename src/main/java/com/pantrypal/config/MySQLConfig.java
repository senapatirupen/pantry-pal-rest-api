package com.pantrypal.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class MySQLConfig {

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        adapter.setShowSql(false);
        adapter.setGenerateDdl(true);

        // Additional Hibernate properties
        adapter.getJpaPropertyMap().put("hibernate.physical_naming_strategy",
                CamelCaseToUnderscoresNamingStrategy.class.getName());
        adapter.getJpaPropertyMap().put("hibernate.implicit_naming_strategy",
                SpringImplicitNamingStrategy.class.getName());
        adapter.getJpaPropertyMap().put("hibernate.jdbc.time_zone", "UTC");

        return adapter;
    }
}
