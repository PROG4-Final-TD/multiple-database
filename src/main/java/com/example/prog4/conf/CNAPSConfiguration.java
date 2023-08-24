package com.example.prog4.conf;

import com.example.prog4.repository.postgres2.entity.CNAPSEmployee;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(
        transactionManagerRef = "postgres2TransactionManager",
        entityManagerFactoryRef = "postgres2EntityManagerFactory",
        basePackages = {"com.example.prog4.repository.postgres2",
        }
)
public class CNAPSConfiguration {
    private final Environment env;

    @Bean(initMethod = "migrate")
    @ConfigurationProperties(prefix = "spring.flyway.postgres2")
    public Flyway flywayPostgres2() {
        return new Flyway(
                Flyway.configure()
                        .baselineOnMigrate(true)
                        .locations("classpath:/db/migration/postgres2")
                        .dataSource(
                                env.getRequiredProperty("spring.second-datasource.url"),
                                env.getRequiredProperty("spring.second-datasource.username"),
                                env.getRequiredProperty("spring.second-datasource.password")
                        )
        );
    }

    @Bean(name = "postgres2DatasourceProperties")
    @ConfigurationProperties(prefix = "spring.second-datasource")
    public DataSourceProperties postgres2DatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "postgres2Datasource")
    public DataSource postgres2DataSource() {
        return postgres2DatasourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "postgres2EntityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder postgres2EntityManagerFactoryBuilder(
            @Qualifier("postgres2Datasource") DataSource dataSource
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", env.getRequiredProperty("spring.second-datasource.url"));
        properties.put("javax.persistence.jdbc.user", env.getRequiredProperty("spring.second-datasource.username"));
        properties.put("javax.persistence.jdbc.password", env.getRequiredProperty("spring.second-datasource.password"));

        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(),
                properties,
                null
        );
    }

    @Bean(name = "postgres2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgres2EntityManagerFactory(
            @Qualifier("postgres2EntityManagerFactoryBuilder") final EntityManagerFactoryBuilder builder,
            @Qualifier("postgres2Datasource") final DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.prog4.repository.postgres2.entity")
                .build();
    }

    @Bean(name = "postgres2TransactionManager")
    public PlatformTransactionManager postgres2PlatformTransactionManager(
            @Qualifier("postgres2EntityManagerFactory") final EntityManagerFactory prostgres2EntityManagerFactory
            ) {
        return new JpaTransactionManager(prostgres2EntityManagerFactory);
    }
}
