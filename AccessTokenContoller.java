package com.gl.platform.web.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.security.jwt.TokenProvider;
import com.gl.platform.web.rest.vm.AccessTokenVM;

@RestController
@RequestMapping("/api")
public class AccessTokenContoller {

	private final TokenProvider tokenProvider;

	private final AuthenticationManager authenticationManager;

	public AccessTokenContoller(TokenProvider tokenProvider, AuthenticationManager authenticationManager) {
		this.tokenProvider = tokenProvider;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/oauth/v1/token")
	@Timed
	public ResponseEntity<Map<String, Object>> authorize(@RequestParam("clientId") String clientId,
			@RequestParam("clientSecret") String clientSecret) {

		HttpHeaders httpHeaders = new HttpHeaders();
		Map<String, Object> token = new HashMap<>();
		AccessTokenVM accessVM = new AccessTokenVM();
		accessVM.setClientId(clientId);
		accessVM.setClientSecret(clientSecret);

		if (!"Lisajackson".equalsIgnoreCase(clientId)) {
			httpHeaders.set("Cache-Control", "no-store");
			httpHeaders.set("Pragma", "no-cache");
			token.put("error", "invalid_client");
			token.put("error_description", "Client authentication failed, invalid client ID or secret.");
			return new ResponseEntity<>(token, httpHeaders, HttpStatus.UNAUTHORIZED);
		}

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				accessVM.getClientId(), accessVM.getClientSecret());

		Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.createToken(authentication, true);
		httpHeaders.set("expires", "3600");
		httpHeaders.set("Cache-Control", "no-store");
		httpHeaders.set("Pragma", "no-cache");
		token.put("access_token", jwt);
		token.put("token_type", "Bearer");
		return new ResponseEntity<>(token, httpHeaders, HttpStatus.OK);
	}
}
