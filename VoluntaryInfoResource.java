package com.gl.platform.web.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.VoluntaryInfoService;
import com.gl.platform.service.dto.VoluntaryInfoDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class VoluntaryInfoResource {

	private final Logger log = LoggerFactory.getLogger(VoluntaryInfoResource.class);

	private static final String ENTITY_NAME = "voluntaryInfoResource";

	@Autowired
	private VoluntaryInfoService voluntaryInfoService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/voluntary/info")
	public @ResponseBody ResponseEntity<VoluntaryInfoDTO> createVoluntaryInfo(
			@RequestBody VoluntaryInfoDTO voluntaryInfoDTO) {

		log.debug("REST request to create voluntary info for user : {}", voluntaryInfoDTO.getUserId());

		if (voluntaryInfoDTO.getId() != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (!authorizationService.ownedByUserOnly(voluntaryInfoDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		VoluntaryInfoDTO response = voluntaryInfoService.save(voluntaryInfoDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{userId}/voluntary/info")
	@Timed
	public ResponseEntity<VoluntaryInfoDTO> getVoluntaryInfo(@PathVariable Long userId) throws NotFoundException {

		log.debug("REST request to get the certificates for user Id : {}", userId);
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}
		Optional<VoluntaryInfoDTO> response = voluntaryInfoService.getInfo(userId);
		return ResponseUtil.wrapOrNotFound(response);
	}

	@PutMapping("/voluntary/info")
	public @ResponseBody ResponseEntity<VoluntaryInfoDTO> updateVoluntaryInfo(
			@RequestBody VoluntaryInfoDTO voluntaryInfoDTO) {

		log.debug("REST request to update voluntary info for user : {}", voluntaryInfoDTO.getUserId());

		if (voluntaryInfoDTO.getId() == null) {
			throw new BadRequestAlertException("Mandatory fields are missing", ENTITY_NAME, "idIsNull");
		}

		if (!authorizationService.ownedByUserOnly(voluntaryInfoDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		VoluntaryInfoDTO response = voluntaryInfoService.save(voluntaryInfoDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
