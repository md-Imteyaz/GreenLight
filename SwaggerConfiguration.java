package com.gl.platform;

import static com.google.common.base.Predicates.or;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import com.google.common.base.Predicate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerConfiguration {

	public Docket swaggerConfiguration() {
		String groupName = "Swagger";
		ArrayList<SecurityScheme> auth = new ArrayList<>(1);
		auth.add(new BasicAuth("basicAuth"));
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.gl.platform.web")).paths(paths()).build()
				.groupName(groupName).apiInfo(apiDetails()).securitySchemes(auth)
				.genericModelSubstitutes(Optional.class).pathMapping("/");
	}

	private ApiInfo apiDetails() {
		return new ApiInfo("GreenLight Credentials", "API collection for GreenLight Locker Website", "2.0",
				"Free to use", new Contact("GreenLight", "https://greenlightlocker.com", "support@glcredentials.com"),
				"API License", "https://greenlightlocker.com", Collections.emptyList());
	}

//	private Predicate<String> paths() {
//		return or(PathSelectors.regex("/api/v1/badge/upload"), PathSelectors.regex("/api/oauth/v1/token"),
//				PathSelectors.regex("/api/checkforbadges/.*"), PathSelectors.regex("/api/upload/certificate"),
//				PathSelectors.regex("/api/.*/certificate"), PathSelectors.regex("/api/upload/certificate/update"),
//				PathSelectors.regex("/api/v1/verifycredentials"), PathSelectors.regex("/api/group/jobs"), 
//				PathSelectors.regex("/api/institution/membership"));
//	}

	private Predicate<String> paths() {
		return or(PathSelectors.ant("/api/**"));
	}

}
