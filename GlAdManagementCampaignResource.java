package com.gl.platform.web.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlAdManagementCampaignService;
import com.gl.platform.service.dto.GlAdManagementCampaignResponseDTO;
import com.gl.platform.service.dto.StudentEmailCampaignResponseDTO;

@RestController
@RequestMapping("/api")
public class GlAdManagementCampaignResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlAdManagementCampaignService glAdManagementCampaignService;

	@Autowired
	private AuthorizationService authorizationService;

	@GetMapping("/ad/campaigns")
	public ResponseEntity<Page<GlAdManagementCampaignResponseDTO>> getAdCampaigns(
			@RequestParam(value = "filterBy", required = false) String filterBy, Pageable pageable) {
		log.info("REST request to get the ad campaigns");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException("not the owner");
		}
		Page<GlAdManagementCampaignResponseDTO> response = glAdManagementCampaignService
				.getGlAdManagementCampaignDetails(filterBy, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/ad/campaign")
	public ResponseEntity<Map<String, Object>> getAdCampaigns(
			@RequestBody StudentEmailCampaignResponseDTO emailCampaignDTO) {
		log.info("REST request to update the ad campaign status for id : {}", emailCampaignDTO.getId());
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException("not the owner");
		}
		Map<String, Object> response = glAdManagementCampaignService
				.updateStatus(emailCampaignDTO.getProcessingStatus(), emailCampaignDTO.getId());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
