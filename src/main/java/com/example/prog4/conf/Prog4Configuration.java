package com.example.prog4.conf;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
@EnableJpaRepositories(
        transactionManagerRef = "postgres1TransactionManager",
        entityManagerFactoryRef = "postgres1EntityManagerFactory",
        basePackages = {"com.example.prog4.repository.postgres1"}
)
public class Prog4Configuration {
    private final Environment env;

    @Bean(initMethod = "migrate")
    @ConfigurationProperties(prefix = "spring.flyway.postgres1")
    public Flyway flywayPostgres1() {
        return new Flyway(
                Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(
                        env.getRequiredProperty("spring.datasource.url"),
                        env.getRequiredProperty("spring.datasource.username"),
                        env.getRequiredProperty("spring.datasource.password")
                )
        );
    }

    @Bean(name = "postgres1Datasource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource postgres1DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "postgres1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgres1EntityManagerFactory(
            final EntityManagerFactoryBuilder builder,
            @Qualifier("postgres1Datasource") final DataSource dataSource
            ) {
        return  builder
                .dataSource(dataSource)
                .packages("com.example.prog4.repository.postgres1.entity")
                .build();
    }

    @Bean(name = "postgres1TransactionManager")
    public PlatformTransactionManager postgres1PlatformTransactionManager(
            @Qualifier("postgres1EntityManagerFactory") final EntityManagerFactory postgres1EntityManagerFactory
            ) {
            return new JpaTransactionManager(postgres1EntityManagerFactory);
    }
}