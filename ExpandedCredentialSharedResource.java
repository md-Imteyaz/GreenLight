package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.ExpandedCredentialShared;
import com.gl.platform.service.ExpandedCredentialSharedService;
import com.gl.platform.service.dto.ExpandedCredentialSharedRequestDTO;
import com.gl.platform.web.rest.errors.AccomplishmentShareFailedExcecption;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class ExpandedCredentialSharedResource {

private final Logger log = LoggerFactory.getLogger(ExpandedCredentialSharedResource.class);
	
	private static final String ENTITY_NAME = ExpandedCredentialShared.class.getSimpleName().toLowerCase();

	@Autowired
	private ExpandedCredentialSharedService expandedCredentialSharedService;

	
	/**
	 * POST /expandedCredentialShared : Create a new expandedCredentialShared.
	 *
	 * @param expandedCredentialSharedDTO the expandedCredentialSharedDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         badgeSharedDTO, or with status 400 (Bad Request) if the badgeShared has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws AccomplishmentShareFailedExcecption 
	 * @throws NotFoundException 
	 */
	@PostMapping("/expanded-credential-shared")
	@Timed
	public ResponseEntity<ExpandedCredentialSharedRequestDTO> createBadgeShared(@Valid @RequestBody ExpandedCredentialSharedRequestDTO expandedCredentialSharedRequestDTO) throws URISyntaxException, AccomplishmentShareFailedExcecption, IOException, NotFoundException {
		log.debug("REST request to save expandedCredentialShared : {}", expandedCredentialSharedRequestDTO);
		if (expandedCredentialSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new badgeShared cannot already have an ID", ENTITY_NAME, "idexists");
		}
		ExpandedCredentialSharedRequestDTO result = expandedCredentialSharedService.save(expandedCredentialSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/badgeShared/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
}
