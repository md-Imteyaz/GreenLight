package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.AccomplishmentPortfolio;
import com.gl.platform.domain.FourYearTranscriptCourse;
import com.gl.platform.service.AccomplishmentPortfolioService;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.dto.AccomplishmentPortfolioDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

/**
 * REST controller for managing Accomplishment portfolio.
 */
@RestController
@RequestMapping("/api")
public class AccomplishmentPortfolioResource {

	private final Logger log = LoggerFactory.getLogger(AccomplishmentPortfolioResource.class);

	private static final String ENTITY_NAME = AccomplishmentPortfolio.class.getSimpleName().toLowerCase();
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private AccomplishmentPortfolioService accomplishmentService;

	@PostMapping("/accomplishment")
	public ResponseEntity<?> createAccomplishment(@RequestBody AccomplishmentPortfolioDTO accomplishmentDTO) throws URISyntaxException, IOException {

		log.debug("Rest request to create accomplishment portfolio");

		if (accomplishmentDTO.getId() != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (!authorizationService.ownedByUserOnly(accomplishmentDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		
		AccomplishmentPortfolioDTO response = accomplishmentService.save(accomplishmentDTO);

		return ResponseEntity.created(new URI("/api/create/accomplishment/portfolio/")).body(response);
	}
	
	@GetMapping("/{userId}/accomplishments")
	public ResponseEntity<List<AccomplishmentPortfolioDTO>> getStudentCertificate(@PathVariable Long userId) throws NotFoundException {
		
		log.debug("REST request to get the accomplishment portfolio by user Id : {}",userId);
		
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}
		List<AccomplishmentPortfolioDTO> response = accomplishmentService.getAccomplishment(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@DeleteMapping("/accomplishment/delete/{id}")
	@Timed
	public ResponseEntity<?> activateOrDeactivate(@PathVariable Long id) {
		log.debug("REST request to soft delete accomplishment portfolio : {}", id);
		Map<String, Object> response = accomplishmentService.activateOrDeactivate(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/{id}/accomplishment")
	public ResponseEntity<?> getSingleStudent(@PathVariable Long id) throws NotFoundException {
		
		log.debug("REST request to get the accomplishment portfolio by id : {}",id);
		
		if (id == null) {
			throw new BadRequestAlertException("Mandotory fields are missing", ENTITY_NAME, "idIsNull");
		}
		Optional<AccomplishmentPortfolio> response = accomplishmentService.getSingleAccomplishment(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/{credentialId}/diploma")
	public ResponseEntity<?> getDiplomaDetails(@PathVariable Long credentialId) {
		log.debug("RESR request to get the diploma details for high school accomplishment : ", credentialId);
		
		if(credentialId == null) {
			throw new BadRequestAlertException("Mandatory fields are missing", ENTITY_NAME, "credentialIdIsNull");
		}
		
		Map<String, Object> response = accomplishmentService.getDiplomaDetails(credentialId);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}
