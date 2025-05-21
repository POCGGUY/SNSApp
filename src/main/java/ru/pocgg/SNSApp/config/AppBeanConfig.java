package ru.pocgg.SNSApp.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class AppBeanConfig {
    private final Environment env;

    public AppBeanConfig(Environment env) {
        this.env = env;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.pocgg.SNSApp");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(loadHibernateProperties());
        return em;
    }
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public DataSource dataSource() {
        Properties hibernateProperties = loadHibernateProperties();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(hibernateProperties.getProperty("hibernate.connection.driver_class"));
        dataSource.setUrl(hibernateProperties.getProperty("hibernate.connection.url"));
        dataSource.setUsername(hibernateProperties.getProperty("hibernate.connection.username"));
        dataSource.setPassword(hibernateProperties.getProperty("hibernate.connection.password"));
        return dataSource;
    }

    private Properties loadHibernateProperties() {
        Properties props = new Properties();
        props.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        props.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        props.put("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
        props.put("hibernate.connection.driver_class", env.getProperty("hibernate.connection.driver_class"));
        props.put("hibernate.connection.url", env.getProperty("hibernate.connection.url"));
        props.put("hibernate.connection.username", env.getProperty("hibernate.connection.username"));
        props.put("hibernate.connection.password", env.getProperty("hibernate.connection.password"));
        props.put("hibernate.use_sql_comments", env.getProperty("hibernate.use_sql_comments"));
        props.put("hibernate.default_schema", env.getProperty("hibernate.default_schema"));
        return props;
    }
}
