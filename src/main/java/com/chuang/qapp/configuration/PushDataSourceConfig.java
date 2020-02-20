package com.chuang.qapp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fandy.lin
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.chuang.qapp.repository.mysql.*",
        entityManagerFactoryRef = "pushEntityManagerFactory",
        transactionManagerRef = "pushTransactionManager")
public class PushDataSourceConfig {

    @Autowired
    @Qualifier(value = "pushOriginalDataSource")
    private DataSource pushOriginalDataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Bean(name = "pushEntityManager")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return pushEntityManagerFactory(builder).getObject().createEntityManager();
    }

    @Bean(name = "pushEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pushEntityManagerFactory (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(pushOriginalDataSource)
                .properties(getVendorProperties())
                .packages("com.chuang.qapp.entity.mysql.*")
                .persistenceUnit("pushPersistenceUnit")
                .build();
    }

    private Map<String, String> getVendorProperties() {
        return jpaProperties.getProperties();
//        return jpaProperties.getHibernateProperties(new HibernateSettings());
    }

    @Bean(name = "pushTransactionManager")
    PlatformTransactionManager pushTransactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(pushEntityManagerFactory(builder).getObject());
    }
}
