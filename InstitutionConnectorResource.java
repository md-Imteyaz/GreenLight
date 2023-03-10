package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.gl.platform.domain.InstitutionConnector;
import com.gl.platform.service.InstitutionConnectorQueryService;
import com.gl.platform.service.InstitutionConnectorService;
import com.gl.platform.service.dto.InstitutionConnectorCriteria;
import com.gl.platform.service.dto.InstitutionConnectorDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing InstitutionConnector.
 */
@RestController
@RequestMapping("/api")
public class InstitutionConnectorResource {
	
	private final Logger log = LoggerFactory.getLogger(InstitutionConnectorResource.class);

	private static final String ENTITY_NAME = InstitutionConnector.class.getSimpleName().toLowerCase();

	private final InstitutionConnectorService institutionConnectorService;
	
	private final InstitutionConnectorQueryService institutionConnectorQueryService;
	
	public InstitutionConnectorResource(InstitutionConnectorService institutionConnectorService,
			InstitutionConnectorQueryService institutionConnectorQueryService){
		this.institutionConnectorService = institutionConnectorService;
		this.institutionConnectorQueryService = institutionConnectorQueryService;
	}
	
	
	/**
	 * POST /salesforce-integration : Create a new institutionConnector.
	 *
	 * @param institutionConnectorDTO the institutionConnectorDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         institutionConnectorDTO, or with status 400 (Bad Request) if the institutionConnector has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/salesforce-integration")
	@Timed
	public ResponseEntity<InstitutionConnectorDTO> createInstitutionConnector(@Valid @RequestBody InstitutionConnectorDTO institutionConnectorDTO) throws URISyntaxException {
		log.debug("REST request to save InstitutionConnector : {}", institutionConnectorDTO);
		if (institutionConnectorDTO.getId() != null) {
			throw new BadRequestAlertException("A new institutionConnector cannot already have an ID", ENTITY_NAME, "idexists");
		}
		InstitutionConnectorDTO result = institutionConnectorService.save(institutionConnectorDTO);
		return ResponseEntity.created(new URI("/api/salesforce-integration" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
	
	/**
	 * PUT /salesforce-integration : Updates an existing institutionConnector.
	 *
	 * @param institutionConnectorDTO the institutionConnectorDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         institutionConnectorDTO, or with status 400 (Bad Request) if the institutionConnectorDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the institutionConnectorDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/salesforce-integration")
	@Timed
	public ResponseEntity<InstitutionConnectorDTO> updateInstitutionConnector(@Valid @RequestBody InstitutionConnectorDTO institutionConnectorDTO) throws URISyntaxException {
		log.debug("REST request to update InstitutionConnector : {}", institutionConnectorDTO);
		if (institutionConnectorDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		InstitutionConnectorDTO result = institutionConnectorService.save(institutionConnectorDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, institutionConnectorDTO.getId().toString()))
				.body(result);
	}
	
	
	/**
	 * GET /salesforce-integration : get all the institutionConnectors.
	 *
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of institutionConnectors in
	 *         body
	 */
	@GetMapping("/salesforce-integration")
	@Timed
	public ResponseEntity<List<InstitutionConnectorDTO>> getAllInstitutionConnectors(InstitutionConnectorCriteria criteria) {
		log.debug("REST request to get InstitutionConnectors by criteria: {}", criteria);

			List<InstitutionConnectorDTO> response = institutionConnectorQueryService.findByCriteria(criteria);
			return new ResponseEntity<>(response ,HttpStatus.OK);
	}
	

	/**
	 * GET /salesforce-integration : get all the institutionConnectors.
	 *
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of institutionConnectors in
	 *         body
	 */
	@GetMapping("/salesforce-integration/{id}")
	@Timed
	public ResponseEntity<InstitutionConnectorDTO> getAllInstitutionConnector(@PathVariable Long id) {
		log.debug("REST request to get InstitutionConnectors by id: {}", id);

			Optional<InstitutionConnectorDTO> result = institutionConnectorService.findByOne(id);
			return ResponseUtil.wrapOrNotFound(result);
	}

}
