//package com.chuang.qapp.configuration;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.persistence.EntityManager;
//import javax.sql.DataSource;
//import java.util.Map;
//
///**
// * @author fandy.lin
// */
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = "com.chuang.qapp.repository.mysql.common",
//        entityManagerFactoryRef = "commonEntityManagerFactory",
//        transactionManagerRef = "commonTransactionManager")
//public class CommonDataSourceConfig {
//
//    @Autowired
//    @Qualifier(value = "commonOriginalDataSource")
//    private DataSource commonOriginalDataSource;
//
//    @Autowired
//    private JpaProperties jpaProperties;
//
//    @Bean(name = "commonEntityManager")
//    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
//        return this.commonEntityManagerFactory(builder).getObject().createEntityManager();
//    }
//
//    @Bean(name = "commonEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean commonEntityManagerFactory (EntityManagerFactoryBuilder builder) {
//        return builder
//                .dataSource(commonOriginalDataSource)
//                .properties(getVendorProperties())
//                .packages("com.chuang.qapp.entity.mysql.common")
//                .persistenceUnit("commonPersistenceUnit")
//                .build();
//    }
//
//    private Map<String, String> getVendorProperties() {
//        return jpaProperties.getProperties();
////        return jpaProperties.getHibernateProperties(new HibernateSettings());
//    }
//
//    @Bean(name = "commonTransactionManager")
//    PlatformTransactionManager commonTransactionManager(EntityManagerFactoryBuilder builder) {
//        return new JpaTransactionManager(commonEntityManagerFactory(builder).getObject());
//    }
//}
