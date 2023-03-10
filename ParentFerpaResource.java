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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.Ferpa;
import com.gl.platform.service.HsParentAuthConsentService;
import com.gl.platform.service.dto.HsParentAuthConsentDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class ParentFerpaResource {

	private final Logger log = LoggerFactory.getLogger(FerpaResource.class);

	private static final String ENTITY_NAME = Ferpa.class.getSimpleName().toLowerCase();

	@Autowired
	HsParentAuthConsentService hsParentService;

	@PostMapping("/parent/ferpa")
	public ResponseEntity<HsParentAuthConsentDTO> createParentFerpa(
			@Valid @RequestBody HsParentAuthConsentDTO hsParentDTO) throws URISyntaxException {

		log.debug("REST request to save parent Ferpa: {}", hsParentDTO);
		if (hsParentDTO.getId() != null) {
			throw new BadRequestAlertException("A new parent ferpa cannot already have an ID", ENTITY_NAME, "idexists");
		}

		HsParentAuthConsentDTO result = hsParentService.save(hsParentDTO);
		return ResponseEntity.created(new URI("/api/parent/ferpa/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PutMapping("/parent/ferpa")
	@Timed
	public ResponseEntity<HsParentAuthConsentDTO> updateParentFerpa(
			@Valid @RequestBody HsParentAuthConsentDTO hsParentAuthDTO) {

		log.debug("REST request to update parent Ferpa: {}", hsParentAuthDTO);
		
		if (hsParentAuthDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		
		HsParentAuthConsentDTO result = hsParentService.save(hsParentAuthDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, hsParentAuthDTO.getId().toString()))
				.body(result);
	}
	
	@GetMapping("/parent/ferpa")
	@Timed
	public ResponseEntity<HsParentAuthConsentDTO> getParentFerpa(
			@Valid @RequestBody HsParentAuthConsentDTO hsParentAuthDTO) {

		log.debug("REST request to update parent Ferpa: {}", hsParentAuthDTO);
		
		if (hsParentAuthDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		
		HsParentAuthConsentDTO result = hsParentService.save(hsParentAuthDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, hsParentAuthDTO.getId().toString()))
				.body(result);
	}
	
	@GetMapping(path = "/parent/{userId}/ferpa")
	@Timed
	public ResponseEntity<?> getDetails(@PathVariable Long userId) throws NotFoundException {
		log.debug("REST request to update parent Ferpa with userId: {}", userId);
		if(userId == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "id null");
		}
		Map<String, Object> result = hsParentService.details(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@GetMapping(path = "/parent/over18")
	@Timed
	public ResponseEntity<?> getOver18StudentDetails(){
		log.debug("REST request to over18 student details with login");
		List<Map<String, String>> result = hsParentService.getOver18StudetnsDetails();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
