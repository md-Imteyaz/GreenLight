package com.gl.platform.config;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.liquibase.AsyncSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
@EnableJpaRepositories("com.gl.platform.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
@ConditionalOnClass({ JdbcTemplate.class })
public class DatabaseConfiguration {

	private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

	private final Environment env;

	public DatabaseConfiguration(Environment env) {
		this.env = env;
	}

	@Bean
	public SpringLiquibase liquibase(@Qualifier("taskExecutor") TaskExecutor taskExecutor, DataSource dataSource,
			LiquibaseProperties liquibaseProperties) {

		// Use liquibase.integration.spring.SpringLiquibase if you don't want Liquibase
		// to start asynchronously
		SpringLiquibase liquibase = new AsyncSpringLiquibase(taskExecutor, env);
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("classpath:config/liquibase/master.xml");
		liquibase.setContexts(liquibaseProperties.getContexts());
		liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
		liquibase.setDropFirst(liquibaseProperties.isDropFirst());
		if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE)) {
			liquibase.setShouldRun(false);
		} else {
			liquibase.setShouldRun(liquibaseProperties.isEnabled());
			log.debug("Configuring Liquibase");
		}
		return liquibase;
	}

	@Bean(name = "namedJdbcTemplate")
	@ConditionalOnMissingBean(NamedParameterJdbcOperations.class)
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
	}

	private Properties getProperties(String queriesXmlPath) throws IOException, InvalidPropertiesFormatException {
		Properties props = new Properties();
		// Create a ClassPathResource, based on the given location within the resource
		// folder
		ClassPathResource res = new ClassPathResource(queriesXmlPath);
		// Load the properties using InputStream provided by the ClassPathResource
		props.loadFromXML(res.getInputStream());

		return props;
	}

	@Bean(name = "statisticsQueries")
	public Properties statisticsQueries() {
		try {
			return getProperties("sql/statistics.sql.xml");

		} catch (IOException e) {
			return new Properties();
		}

	}
	
	@Bean(name = "marketPlaceQueries")
	public Properties marketPlaceQueries() {
		try {
			return getProperties("sql/marketplace.sql.xml");

		} catch (IOException e) {
			return new Properties();
		}

	}

	@Bean(name = "universityQueries")
	public Properties universityQueries() {
		try {
			return getProperties("sql/university.sql.xml");

		} catch (IOException e) {
			return new Properties();
		}

	}
	
	@Bean(name = "studentSurveyQueries")
	public Properties studentSurveyQuiries() {
		try {
			return getProperties("sql/student-survey.sql.xml");

		} catch (IOException e) {
			return new Properties();
		}

	}
	
	
	@Bean(name = "messagesQueries")
	public Properties messagesQueries() {
		try {
			return getProperties("sql/messages-inbox.sql.xml");

		} catch (IOException e) {
			return new Properties();
		}

	}

	
	@Bean(name = "studentQueries")
	public Properties studentQueries() {
		try {
			return getProperties("sql/student-queries.sql.xml");
		} catch (IOException e) {
			return new Properties();
		}
	}
	
	@Bean(name = "marketingQueries")
	public Properties marketingQueries() {
		try {
			return getProperties("sql/marketing-queries.sql.xml");
		} catch (IOException e) {
			return new Properties();
		}
	}


	@Bean(name = "employerQueries")
	public Properties glJobQueries() {
		try {
			Properties props = new Properties();
			// Create a ClassPathResource, based on the given location within the resource
			// folder
			ClassPathResource res = new ClassPathResource("sql/employer-queries.sql.xml");
			// Load the properties using InputStream provided by the ClassPathResource
			props.loadFromXML(res.getInputStream());

			return props;

		} catch (IOException e) {
			return new Properties();
		}

	}

	
	@Bean(name = "credentialQueries")
	public Properties credentialQueries() {
		try {
			Properties props = new Properties();
			// Create a ClassPathResource, based on the given location within the resource
			// folder
			ClassPathResource res = new ClassPathResource("sql/credential.sql.xml");
			// Load the properties using InputStream provided by the ClassPathResource
			props.loadFromXML(res.getInputStream());

			return props;

		} catch (IOException e) {
			return new Properties();
		}
	}
	
	
	@Bean(name = "highschoolQueries")
	public Properties highSchoolQueries() {
		try {
			Properties props = new Properties();
			// Create a ClassPathResource, based on the given location within the resource
			// folder
			ClassPathResource res = new ClassPathResource("sql/highschool.sql.xml");
			// Load the properties using InputStream provided by the ClassPathResource
			props.loadFromXML(res.getInputStream());

			return props;

		} catch (IOException e) {
			return new Properties();
		}

	}
	

	@Bean(name = "holdQueries")
	public Properties holdQueries() {
		try {
			Properties props = new Properties();
			// Create a ClassPathResource, based on the given location within the resource
			// folder
			ClassPathResource res = new ClassPathResource("sql/hold.sql.xml");
			// Load the properties using InputStream provided by the ClassPathResource
			props.loadFromXML(res.getInputStream());

			return props;

		} catch (IOException e) {
			return new Properties();
		}

	}
}
