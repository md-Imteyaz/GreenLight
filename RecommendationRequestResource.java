package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.RecommendationRequest;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.RecommendationRequestService;
import com.gl.platform.service.dto.RecommendationRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

/**
 * REST controller for managing RecommendationRequest.
 */
@RestController
@RequestMapping("/api")
public class RecommendationRequestResource {

	private final Logger log = LoggerFactory.getLogger(RecommendationRequestResource.class);

	private static final String ENTITY_NAME = RecommendationRequest.class.getSimpleName().toLowerCase();

	@Autowired
	private RecommendationRequestService recommendationRequestService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/recommendationrequest")
	@Timed
	public ResponseEntity<?> createRecommendationRequest(
			@Valid @RequestBody RecommendationRequestDTO recommendationRequestDTO) throws URISyntaxException {
		log.debug("REST request to save RecommendationRequest : {}", recommendationRequestDTO);
		if (recommendationRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new recommendationRequest cannot already have an ID", ENTITY_NAME,
					"idexists");
		}

		if (!authorizationService.ownedByUserOnly(recommendationRequestDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		RecommendationRequestDTO result = recommendationRequestService.save(recommendationRequestDTO);
		return ResponseEntity.created(new URI("/api/recommendationRequest/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/recommendationrequest/{userId}")
	@Timed
	public ResponseEntity<?> getRecommendationRequestDetailsByInstitutionId(@PathVariable Long userId) {
		log.debug("REST request to get recommendationRequest details by userID : {}", userId);
		List<RecommendationRequestDTO> response = recommendationRequestService.findByUserId(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/recommendationrequest/status")
	@Timed
	public ResponseEntity<?> getRecommendationRequestStatusByKey(@RequestParam("key") String reqKey) {
		log.debug("REST request to get recommendationRequest status by key : {}", reqKey);
		Map<String, Object> response = recommendationRequestService.getStatusByKey(reqKey);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/recommendationrequest/update/status")
	@Timed
	public ResponseEntity<Map<String, String>> updateStatus(@RequestParam("key") String reqKey,
			@RequestParam("status") String status, @RequestParam(value = "message", required = false) String message) {
		log.debug("REST request to update recommendationRequest status by key : {} to status: {}", reqKey, status);
		Map<String, String> response = recommendationRequestService.updateRequest(reqKey, status, message);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/dashboard/recommendationrequest/update/status")
	@Timed
	public ResponseEntity<Map<String, String>> updateStatus(@RequestParam("userId") Long userId,
			@RequestParam("key") String reqKey, @RequestParam("status") String status,
			@RequestParam(value = "message", required = false) String message) {
		log.debug("REST request to update recommendationRequest status by key : {} to status: {}", reqKey, status);
		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		Map<String, String> response = recommendationRequestService.updateRequest(reqKey, status, message);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
