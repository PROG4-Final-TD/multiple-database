package com.example.prog4.conf;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
                .locations("classpath:/db/migration/postgres1")
                .dataSource(
                        env.getRequiredProperty("spring.datasource.url"),
                        env.getRequiredProperty("spring.datasource.username"),
                        env.getRequiredProperty("spring.datasource.password")
                )
        );
    }

    @Primary
    @Bean(name = "postgres1DatasourceProperties")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties postgres1DatasourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "postgres1Datasource")
    public DataSource postgres1DataSource() {
        return postgres1DatasourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "postgres1EntityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder postgres1EntityManagerFactoryBuilder(
            @Qualifier("postgres1Datasource") DataSource dataSource
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", env.getRequiredProperty("spring.datasource.url"));
        properties.put("javax.persistence.jdbc.user", env.getRequiredProperty("spring.datasource.username"));
        properties.put("javax.persistence.jdbc.password", env.getRequiredProperty("spring.datasource.password"));

        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(),
                properties,
                null
        );
    }

    @Primary
    @Bean(name = "postgres1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgres1EntityManagerFactory(
            @Qualifier("postgres1EntityManagerFactoryBuilder") final EntityManagerFactoryBuilder builder,
            @Qualifier("postgres1Datasource") final DataSource dataSource
            ) {
        return  builder
                .dataSource(dataSource)
                .packages("com.example.prog4.repository.postgres1.entity")
                .build();
    }

    // Added this Primary annotation here and the employee creation worked fine
    // But for some reason beyond my understanding, this error occurs when I try to go to /employee/show/employee_id
    // 500 failed to lazily initialize a collection of role: com.example.prog4.repository.postgres1.entity.Employee.phones: could not initialize proxy - no Session
    @Primary
    @Bean(name = "postgres1TransactionManager")
    public PlatformTransactionManager postgres1PlatformTransactionManager(
            @Qualifier("postgres1EntityManagerFactory") final EntityManagerFactory postgres1EntityManagerFactory
            ) {
            return new JpaTransactionManager(postgres1EntityManagerFactory);
    }
}
