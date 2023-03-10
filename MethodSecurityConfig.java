package com.gl.platform.config;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

import com.gl.platform.security.AuthoritiesConstants;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

	private DefaultMethodSecurityExpressionHandler defaultMethodExpressionHandler = new DefaultMethodSecurityExpressionHandler();

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		return defaultMethodExpressionHandler;
	}

	public class DefaultMethodSecurityExpressionHandler
			extends org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler {

		@Override
		public StandardEvaluationContext createEvaluationContextInternal(final Authentication auth,
				final MethodInvocation mi) {
			StandardEvaluationContext standardEvaluationContext = super.createEvaluationContextInternal(auth, mi);
			((StandardTypeLocator) standardEvaluationContext.getTypeLocator())
					.registerImport(AuthoritiesConstants.class.getPackage().getName());
			return standardEvaluationContext;
		}
	}

}
