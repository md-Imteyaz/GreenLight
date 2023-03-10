package com.gl.platform.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@Configuration
//@EnableJpaRepositories("com.gl.platform.repository")
//@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
//@EnableTransactionManagement
//@ConditionalOnClass({ JdbcTemplate.class })
public class DCCCDDatabaseConfiguration {

	private final Logger log = LoggerFactory.getLogger(DCCCDDatabaseConfiguration.class);

	private final Environment env;

	public DCCCDDatabaseConfiguration(Environment env) {
		this.env = env;
	}

	@Bean
	@ConfigurationProperties("dcccd.datasource")
	public DataSourceProperties dcccdDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("dcccd.datasource")
	public DataSource dcccdDataSource() {
		return dcccdDataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Bean(name = "dcccdEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

		LocalContainerEntityManagerFactoryBean emf = builder.dataSource(dcccdDataSource()).persistenceUnit("dcccd")
				.build();
		emf.setJpaProperties(properties);
		return emf;
	}

	@Bean(name = "dcccdTransactionManager")
	public JpaTransactionManager db2TransactionManager(
			@Qualifier("dcccdEntityManagerFactory") final EntityManagerFactory emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}

}
