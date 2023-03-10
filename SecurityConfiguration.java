package com.gl.platform.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import com.gl.platform.security.AuthoritiesConstants;
import com.gl.platform.security.jwt.JWTConfigurer;
import com.gl.platform.security.jwt.TokenProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	private final UserDetailsService userDetailsService;

	private final TokenProvider tokenProvider;

	private final CorsFilter corsFilter;

	private final SecurityProblemSupport problemSupport;

	public SecurityConfiguration(AuthenticationManagerBuilder authenticationManagerBuilder,
			UserDetailsService userDetailsService, TokenProvider tokenProvider, CorsFilter corsFilter,
			SecurityProblemSupport problemSupport) {
		this.authenticationManagerBuilder = authenticationManagerBuilder;
		this.userDetailsService = userDetailsService;
		this.tokenProvider = tokenProvider;
		this.corsFilter = corsFilter;
		this.problemSupport = problemSupport;
	}

	@PostConstruct
	public void init() {
		try {
			authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		} catch (Exception e) {
			throw new BeanInitializationException("Security configuration failed", e);
		}
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**").antMatchers("/app/**/*.{js,html}").antMatchers("/i18n/**")
				.antMatchers("/content/**").antMatchers("/swagger-ui/index.html").antMatchers("/test/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class).exceptionHandling()
				.authenticationEntryPoint(problemSupport).accessDeniedHandler(problemSupport).and().csrf().disable()
				.headers().frameOptions().disable().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers("/api/recommender/register").permitAll()
				.antMatchers("/api/recommender/details").permitAll()
				.antMatchers("/api/nroc/redirect").permitAll()
				.antMatchers("/api/vjfEmployer/details").permitAll()
				.antMatchers("/api/states-list").permitAll()
				.antMatchers("/api/employer/register/request").permitAll()
				.antMatchers("/api/institutions/employerlist").permitAll()
				.antMatchers("/api/career/pathway/dropdown").permitAll()
				.antMatchers("/api/affiliated/employer").permitAll()
				.antMatchers("/api/all/jobs").permitAll()
				.antMatchers("/api/training/student").permitAll()
				.antMatchers("/api/v1/verifycredentials").permitAll()
				.antMatchers("/api/oauth/v1/token").permitAll()
				.antMatchers("/api/verification/**").permitAll()
				.antMatchers("/api/degree/verification").permitAll()
				.antMatchers("/api/contactus").permitAll()
				.antMatchers("/api/edready/staff/details").permitAll()
				.antMatchers("/api/enrollment/getcode").permitAll()
				.antMatchers("/api/institutions/dropdownlist").permitAll()
				.antMatchers("/api/generate/temp.png").permitAll()
				.antMatchers("/api/register").permitAll()
				.antMatchers("/api/hs/claim").permitAll()
				.antMatchers("/api/gl-users/register").permitAll()
				.antMatchers("/api/gl-users").permitAll()
				.antMatchers("/api/salesforce/**").permitAll()
				.antMatchers("/api/forgotusername/**").permitAll()
				.antMatchers("/api/validatetranscriptsrequests").permitAll()
				.antMatchers("/api/gl-students/**").permitAll()
				.antMatchers("/api/addresses/**").permitAll()
				.antMatchers("/api/institutions").permitAll()
				.antMatchers("/api/gl-students/full").permitAll()
				.antMatchers("/api/gl-parent").permitAll()
				.antMatchers("/api/activate").permitAll()
				.antMatchers("/api/authenticate").permitAll()
				.antMatchers("/api/upload/dcccdtranscripts").permitAll()
				.antMatchers("/api/supportcases").permitAll()
				.antMatchers("/api/account/forgot-password/send-otp").permitAll()
				.antMatchers("/api/account/reset-password/**").permitAll()				
				.antMatchers("/api/account/forgot-password-update").permitAll()    	
			    .antMatchers("/api/account/forgot-password/resend-otp").permitAll()
				.antMatchers("/api/account/resend-otp").permitAll()
				.antMatchers("/api/account/change-reset-password").permitAll()
				.antMatchers("/api/activate-admin").permitAll()
			    .antMatchers("/api/account/reset-password/init").permitAll()
			    .antMatchers("/api/account/reset-password/finish").permitAll()
			    .antMatchers("/api/existing/user").permitAll()
			    .antMatchers("/api/student/all/ads").permitAll()
			    .antMatchers("/api/staff/registration").permitAll()
			    .antMatchers("/api/share/download").permitAll()
			    .antMatchers("/api/recommendationrequest/update/status").permitAll()
			    .antMatchers("/api/recommendationrequest/status").permitAll()
			    .antMatchers("/api/registar/referal").permitAll()
				.antMatchers("/api/resend/activate-mail/**").permitAll()
				.antMatchers("/api/validate/referal/**").permitAll()
				.antMatchers("/api/validatemail").permitAll()
				.antMatchers("/api/validatetranscriptsrequests").permitAll()
				.antMatchers("/api/upload/recommendationLetter").permitAll()
				.antMatchers("/api/**").authenticated()
				.antMatchers("/management/health").permitAll()
				.antMatchers("/management/info").permitAll()
				.antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
				.antMatchers("/v2/api-docs/**").permitAll()
				.antMatchers("/swagger-resources/configuration/ui").permitAll()
				.antMatchers("/swagger-ui/index.html").hasAuthority(AuthoritiesConstants.ADMIN).and()
	            .httpBasic().and()
				.apply(securityConfigurerAdapter());

	}

	private JWTConfigurer securityConfigurerAdapter() {
		return new JWTConfigurer(tokenProvider);
	}
}
