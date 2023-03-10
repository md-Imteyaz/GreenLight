/**
 * 
 */
package com.gl.platform.web.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gl.platform.config.ApplicationProperties;
import com.gl.platform.domain.Institution;
import com.gl.platform.domain.InstitutionConnector;
import com.gl.platform.service.InstitutionService;
import com.gl.platform.service.dto.InstitutionConnectorDTO;
import com.gl.platform.service.mapper.InstitutionConnectorMapper;

/**
 * 
 * SalesForce Auth Controller
 * 
 * @author venkat
 *
 */
@Controller
@RequestMapping("/api/salesforce")
public class SalesForceAuthController {

	private final Logger log = LoggerFactory.getLogger(SalesForceAuthController.class);

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private InstitutionService institutionService;
	
	@Autowired
	private InstitutionConnectorMapper institutionConnectorMapper; 

	@GetMapping("/callback")
	public String authenticate(@Param("code") String code, @Param("state") String state) {
		log.debug("code params : {}",code);
		// Call the Rest API with Post.
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		// InstituteConnectors
		// connector=institutionService.getConnectorData(Long.parseLong(state));
		// String clientSecret=connector.getClientSecret();
		// String clientCode=connector.getClientId();
		map.add("grant_type", "authorization_code");
		map.add("client_secret", applicationProperties.getAppClientSecret());
		map.add("client_id", applicationProperties.getAppClientKey());
		map.add("redirect_uri", applicationProperties.getHostURL() + "/api/salesforce/callback");
		map.add("code", code);

		String responsJSON = restTemplate.postForObject(applicationProperties.getSalesForceTokenURL(), map,
				String.class);
		log.debug(responsJSON);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(responsJSON);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.debug("Exception at Sales Force Callback : {}",e.getMessage());
		}
		InstitutionConnector updateConnector = new InstitutionConnector();
		updateConnector.setAuthToken(jsonNode.get("access_token").asText());
		updateConnector.setRefreshToken(jsonNode.get("id_token").asText());
		updateConnector.setSignature(jsonNode.get("signature").asText());
		updateConnector.setInstanceUrl(jsonNode.get("instance_url").asText());
		Institution institute = new Institution();
		institute.setId(Long.parseLong(state));
		updateConnector.setInstitute(institute);
		// Save the Institute Conenctor.
		
		
		InstitutionConnectorDTO savedConnector = institutionService.saveConnectors(institutionConnectorMapper.toDto(updateConnector));
		return "redirect:/app/settings/salesforcesettings/" + savedConnector.getId();
	}

	@GetMapping("/authorize/{universityId}")
	public void authorize(HttpServletResponse response, @PathVariable Long universityId) {

		// Get the Data from the University Settings.
		// InstituteConnectors
		// connector=institutionService.getConnectorData(universityId);
		// String clientId=connector.getClientId();
		String newURL = applicationProperties.getSalesForceAuthURL() + "?response_type=code&client_id="
				+ applicationProperties.getAppClientKey() + "&redirect_uri=" + applicationProperties.getHostURL()
				+ "/api/salesforce/callback&state=" + universityId;
		try {
			response.sendRedirect(newURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.debug("Exception at Sales Force authorize : {}", e.getMessage());
		}
	}

}
