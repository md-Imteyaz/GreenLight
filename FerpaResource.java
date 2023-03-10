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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.domain.Ferpa;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.FerpaQueryService;
import com.gl.platform.service.FerpaService;
import com.gl.platform.service.GlJobAplicantService;
import com.gl.platform.service.dto.FerpaCriteria;
import com.gl.platform.service.dto.FerpaDTO;
import com.gl.platform.service.dto.HsParentAuthConsentDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.service.filter.LongFilter;

/**
 * REST controller for managing Ferpa.
 */
@RestController
@RequestMapping("/api")
public class FerpaResource {

	private final Logger log = LoggerFactory.getLogger(FerpaResource.class);

	private static final String ENTITY_NAME = Ferpa.class.getSimpleName().toLowerCase();

	private final FerpaService ferpaService;

	private final FerpaQueryService ferpaQueryService;
	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private GlJobAplicantService glJobAplicantService;
	
	
	public FerpaResource(FerpaService ferpaService, FerpaQueryService ferpaQueryService) {
		this.ferpaQueryService = ferpaQueryService;
		this.ferpaService = ferpaService;
	}

	/**
	 * POST /ferpa : Create a new ferpa.
	 *
	 * @param ferpaDTO the ferpaDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         ferpaDTO, or with status 400 (Bad Request) if the ferpa has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws JsonProcessingException 
	 */
	@PostMapping("/ferpa")
	@Timed
	//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT')")
	public ResponseEntity<FerpaDTO> createFerpa(@Valid @RequestBody FerpaDTO ferpaDTO) throws URISyntaxException, JsonProcessingException {
		log.debug("REST request to save Ferpa : {}", ferpaDTO);
		if (ferpaDTO.getId() != null) {
			throw new BadRequestAlertException("A new ferpa cannot already have an ID", ENTITY_NAME, "idexists");
		}
		
		if (!authorizationService.ownedByUserOnly(ferpaDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		
		LongFilter filterByUSerId=new LongFilter();
		filterByUSerId.setEquals(ferpaDTO.getUserId());

		FerpaCriteria ferpaCriteria=new FerpaCriteria();
		ferpaCriteria.setUserId(filterByUSerId);
		
		List<FerpaDTO> ferpaUserDTO=ferpaQueryService.findByCriteria(ferpaCriteria);
		log.debug("Ferpa user object : {}",ferpaUserDTO);
		
		
		
		if(ferpaUserDTO.size()>0) {		
			throw new BadRequestAlertException("Already UserID verified cannot add a new ferpa ID", ENTITY_NAME, "useridexists");
		}
		
		glJobAplicantService.calcualteMatches(ferpaDTO.getUserId());
		FerpaDTO result = ferpaService.save(ferpaDTO);
		return ResponseEntity.created(new URI("/api/ferpa/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * GET /ferpa : get ferpa/ferpas.
	 *
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of ferpa/ferpas
	 *         in body
	 */
	@GetMapping("/ferpa")
	@Timed
	//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT') or hasRole('ROLE_EMPLOYER') or hasRole('ROLE_UNIVERSITY')")
	public ResponseEntity<List<FerpaDTO>> getFerpa(FerpaCriteria criteria) {
		log.debug("REST request to get ferpa by criteria: {}", criteria);
		List<FerpaDTO> response = ferpaQueryService.findByCriteria(criteria);

		for(FerpaDTO ferpa : response) {
			if (!authorizationService.ownedByUserOnly(ferpa.getUserId())) {
				throw new AccessDeniedException("not the owner");
			}
			
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * PUT /ferpa : Updates an existing address.
	 *
	 * @param ferpaDTO the ferpaDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         ferpaDTO, or with status 400 (Bad Request) if the ferpaDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the ferpaDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws JsonProcessingException 
	 */
	@PutMapping("/ferpa")
	@Timed
	//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT')")
	public ResponseEntity<FerpaDTO> updateAddress(@Valid @RequestBody FerpaDTO ferpaDTO) throws URISyntaxException, JsonProcessingException {
		log.debug("REST request to update Ferpa : {}", ferpaDTO);
		if (ferpaDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!authorizationService.ownedByUserOnly(ferpaDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		
		FerpaDTO result = ferpaService.update(ferpaDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ferpaDTO.getId().toString()))
				.body(result);
	}
	
	
	@GetMapping("/student/type")
	@Timed
	public ResponseEntity<?> getSudentType() {
		log.debug("REST request to get student types");
		Map<String, Object> response = ferpaService.getStudentCredentialTypes();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/student/parents")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getSudentParents(){
		log.debug("REST request to get parent names");
		List<Map<String, Object>> response = ferpaService.getStudentParents();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping("/student/parent/ferpa")
	public ResponseEntity<HsParentAuthConsentDTO> createParentFerpa(
			@Valid @RequestBody HsParentAuthConsentDTO hsParentDTO) throws URISyntaxException {

		log.debug("REST request to save parent Ferpa: {}", hsParentDTO);
		if (hsParentDTO.getId() != null) {
			throw new BadRequestAlertException("A new parent ferpa cannot already have an ID", ENTITY_NAME, "idexists");
		}

		HsParentAuthConsentDTO result = ferpaService.saveParentFerpaThruStudent(hsParentDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
		}
	

}
