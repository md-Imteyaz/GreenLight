package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.GlAdSubscriptionConfig;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.InstitutionAdsManagementService;
import com.gl.platform.service.dto.InstitutionAdManagementRequestDTO;
import com.gl.platform.service.dto.InstitutionAdsManagementDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

@RestController
@RequestScope
@RequestMapping("/api")
public class InstitutionAdsManagementResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String ENTITY_NAME = InstitutionAdsManagementDTO.class.getSimpleName().toLowerCase();

	@Autowired
	private InstitutionAdsManagementService institutionAdsManagementService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/institution/ads")
	public ResponseEntity<InstitutionAdsManagementDTO> createinstitutionAd(
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "redirectLink", required = true) String redirectLink,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "imageDisplayType", required = true) String imageDisplayType,
			@RequestParam(value = "criteriaIds", required = true) List<Long> criteriaIds,
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			@RequestParam(value = "isNewImage", required = false) Boolean isNewImage,
			@RequestParam(value = "subject", required = false) String subject,
			@RequestParam(value = "message", required = false) String message,
			@RequestParam(value = "adName", required = true) String adName) throws URISyntaxException, IOException {
		log.info("REST request to create the institution ad for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		InstitutionAdsManagementDTO instAdmanagementDTO = new InstitutionAdsManagementDTO();
		if (!StringUtils.isBlank(id) && !id.equals("null")) {
			instAdmanagementDTO.setId(Long.parseLong(id));
		}
		instAdmanagementDTO.setIsNewImage(isNewImage);
		instAdmanagementDTO.setContent(content);
		instAdmanagementDTO.setCriteriaIds(criteriaIds);
		instAdmanagementDTO.setImageDisplayType(imageDisplayType);
		instAdmanagementDTO.setInstitutionId(institutionId);
		instAdmanagementDTO.setRedirectLink(redirectLink);
		instAdmanagementDTO.setAdName(adName);
		if (subject != null && message != null) {
			instAdmanagementDTO.setIsCustomTemplate(Boolean.TRUE);
		}
		instAdmanagementDTO.setSubject(subject);
		instAdmanagementDTO.setMessage(message);
		instAdmanagementDTO = institutionAdsManagementService.createInstAds(instAdmanagementDTO, imageFile);
		return ResponseEntity.created(new URI(ServletUriComponentsBuilder.fromCurrentRequest().toUriString()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, instAdmanagementDTO.getId().toString()))
				.body(instAdmanagementDTO);
	}

	@GetMapping("/institution/ads/{institutionId}")
	public ResponseEntity<List<InstitutionAdsManagementDTO>> getInstitutionPostedAds(@PathVariable Long institutionId) {
		log.info("REST request to get all the institution posted ads for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		List<InstitutionAdsManagementDTO> response = institutionAdsManagementService.getAllAdsPosted(institutionId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/student/all/ads")
	public ResponseEntity<List<Map<String, Object>>> getAllPostedAds() {
		log.info("REST request to get all the  posted ads for Student view");
		List<Map<String, Object>> response = institutionAdsManagementService.getAllAdsPosted();
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/institution/ads/{status}/{id}")
	public ResponseEntity<Map<String, Object>> disablePostedAd(@PathVariable Long id, @PathVariable Boolean status) {
		log.info("REST request to delete the institution posted ad for id : {}", id);
		Map<String, Object> response = institutionAdsManagementService.disablePostedAd(id, status);
		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("/institution/ads/{id}")
	public ResponseEntity<Map<String, Object>> deletePostedAd(@PathVariable Long id) {
		log.info("REST request to delete the institution posted ad for id : {}", id);
		Map<String, Object> response = institutionAdsManagementService.deletePostedAd(id);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/institution/ads/subscription")
	public ResponseEntity<GlAdSubscriptionConfig> createInstitutionAdSubscription(
			@RequestBody GlAdSubscriptionConfig glAdSubscriptionConfig) throws URISyntaxException {
		log.info("REST request to create the  subscription plan for institution id : {}",
				glAdSubscriptionConfig.getInstitutionId());
		if (!authorizationService.ownedByInstitution(glAdSubscriptionConfig.getInstitutionId())) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		glAdSubscriptionConfig.setActive(true);
		glAdSubscriptionConfig = institutionAdsManagementService.createSubscription(glAdSubscriptionConfig);
		return ResponseEntity.created(new URI("/api/institution/ads/subscription" + glAdSubscriptionConfig.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, glAdSubscriptionConfig.getId().toString()))
				.body(glAdSubscriptionConfig);
	}

	@PutMapping("/institution/ads/subscription")
	public ResponseEntity<GlAdSubscriptionConfig> updateInstitutionAdSubscription(
			@RequestBody GlAdSubscriptionConfig glAdSubscriptionConfig) throws URISyntaxException {
		log.info("REST request to create the  subscription plan for institution id : {}",
				glAdSubscriptionConfig.getInstitutionId());

		if (!authorizationService.ownedByInstitution(glAdSubscriptionConfig.getInstitutionId())) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		if (glAdSubscriptionConfig.getId() == null) {
			throw new BadRequestAlertException("Id must not be null", "GlAdSubscriptionConfig", "idNull");
		}
		glAdSubscriptionConfig = institutionAdsManagementService.createSubscription(glAdSubscriptionConfig);
		return ResponseEntity.created(new URI("/api/institution/ads/subscription" + glAdSubscriptionConfig.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, glAdSubscriptionConfig.getId().toString()))
				.body(glAdSubscriptionConfig);
	}

	@GetMapping("/institution/ads/subscription/{institutionId}")
	public ResponseEntity<List<GlAdSubscriptionConfig>> getScubscriptionDetailsForInstitution(Long institutionId) {
		log.info("REST request to get all the  posted ads for Student view");
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		List<GlAdSubscriptionConfig> response = institutionAdsManagementService.getSubscriptionDetails(institutionId);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/institution/ads/email/content")
	public ResponseEntity<Map<String, Object>> createEmailContentAds(
			@RequestBody InstitutionAdManagementRequestDTO instAdManagementRequestDTO) {
		log.info("REST request to create email content for the selected ads : {}",
				instAdManagementRequestDTO.getAdIds());
		if (!authorizationService.ownedByInstitution(instAdManagementRequestDTO.getInstitutionId())) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = institutionAdsManagementService
				.createEmailContentForSelectedPostedAds(instAdManagementRequestDTO);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/targetted/ads/{candidateId}")
	@Timed
	public ResponseEntity<List<InstitutionAdsManagementDTO>> getTargettedAds(@PathVariable Long candidateId,
			@RequestParam("institutionType") String institutionType) {
		log.info("REST request to get the targetted ads fo candidate id : {}", candidateId);
		List<InstitutionAdsManagementDTO> response = institutionAdsManagementService.processTagettedAds(candidateId,
				institutionType);
		return ResponseEntity.ok().body(response);
	}

}
