package com.gl.platform.web.rest;

import java.net.URISyntaxException;
import java.util.Map;

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

import com.gl.platform.service.InstitutionPreferencesService;
import com.gl.platform.service.dto.InstitutionPreferencesDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class InstitutionPreferencesResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionPreferencesResource.class);

	private static final String ENTITY_NAME = InstitutionPreferencesResource.class.getSimpleName().toLowerCase();
	
	@Autowired
	private InstitutionPreferencesService instPreferencesService;

	@PostMapping("/employer/quicklinks")
	public ResponseEntity<?> createQuickLinks(@RequestBody InstitutionPreferencesDTO instPreferencesDTO) throws URISyntaxException {

		log.debug("REST request to create the links : {}", instPreferencesDTO);

		if (instPreferencesDTO.getId() != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}
		Map<String, Object> response = instPreferencesService.save(instPreferencesDTO);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/employer/quicklinks/{institutionId}")
	public InstitutionPreferencesDTO getstatus(@PathVariable Long institutionId) throws BadRequestException {
		
		log.debug("REST request to get the status : {}", institutionId);
		
		if(institutionId == null) {
			throw new BadRequestException("Institution Id is null");
		}
		
		InstitutionPreferencesDTO response = instPreferencesService.getLinksStatus(institutionId);
		
		return response;
	}
	
	@PutMapping("/employer/quicklinks")
	public ResponseEntity<?> updateLinks(@RequestBody InstitutionPreferencesDTO instPreferencesDTO) throws URISyntaxException {

		log.debug("REST request to create the links : {}", instPreferencesDTO);

		if (instPreferencesDTO.getId() == null) {
			throw new BadRequestAlertException("institution preferences must have an ID", ENTITY_NAME, "idnotexists");
		}
		Map<String, Object> response = instPreferencesService.save(instPreferencesDTO);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
