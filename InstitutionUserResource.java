package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.InstitutionUser;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.InstitutionService;
import com.gl.platform.service.InstitutionUserQueryService;
import com.gl.platform.service.InstitutionUserService;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.InstitutionDTO;
import com.gl.platform.service.dto.InstitutionUserCriteria;
import com.gl.platform.service.dto.InstitutionUserDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing InstitutionUser.
 */
@RestController
@RequestMapping("/api")
public class InstitutionUserResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionUserResource.class);

	private static final String ENTITY_NAME = InstitutionUser.class.getSimpleName().toLowerCase();

	private final InstitutionUserService institutionUserService;

	private final InstitutionUserQueryService institutionUserQueryService;
	
	@Autowired
	private GlUserService glUserService;
	
	@Autowired
	private InstitutionService institutionService;

	public InstitutionUserResource(InstitutionUserService institutionUserService,
			InstitutionUserQueryService institutionUserQueryService) {
		this.institutionUserService = institutionUserService;
		this.institutionUserQueryService = institutionUserQueryService;
	}

	/**
	 * POST /institution-users : Create a new institutionUser.
	 *
	 * @param institutionUserDTO the institutionUserDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         institutionUserDTO, or with status 400 (Bad Request) if the
	 *         institutionUser has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */

	@PostMapping("/institution-users")
	@Timed
	public ResponseEntity<InstitutionUserDTO> createInstitutionUser(@RequestBody InstitutionUserDTO institutionUserDTO)
			throws URISyntaxException {
		log.debug("REST request to save InstitutionUser : {}", institutionUserDTO);
		if (institutionUserDTO.getId() != null) {
			throw new BadRequestAlertException("A new institutionUser cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		InstitutionUserDTO result = institutionUserService.save(institutionUserDTO);
		return ResponseEntity.created(new URI("/api/institution-users/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /institution-users : Updates an existing institutionUser.
	 *
	 * @param institutionUserDTO the institutionUserDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         institutionUserDTO, or with status 400 (Bad Request) if the
	 *         institutionUserDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the institutionUserDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/institution-users")
	@Timed
	public ResponseEntity<InstitutionUserDTO> updateInstitutionUser(@RequestBody InstitutionUserDTO institutionUserDTO)
			throws URISyntaxException {
		log.debug("REST request to update InstitutionUser : {}", institutionUserDTO);
		if (institutionUserDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		InstitutionUserDTO result = institutionUserService.save(institutionUserDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, institutionUserDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /institution-users : get all the institutionUsers.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         institutionUsers in body
	 */
	@GetMapping("/institution-users")
	@Timed
	public ResponseEntity<List<InstitutionUserDTO>> getAllInstitutionUsers(InstitutionUserCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get InstitutionUsers by criteria: {}", criteria);
		Page<InstitutionUserDTO> page = institutionUserQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/institution-users");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /institution-users/:id : get the "id" institutionUser.
	 *
	 * @param id the id of the institutionUserDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         institutionUserDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/institution-users/{id}")
	@Timed
	public ResponseEntity<InstitutionUserDTO> getInstitutionUser(@PathVariable Long id) {
		log.debug("REST request to get InstitutionUser : {}", id);

		Optional<InstitutionUserDTO> institutionUserDTO = institutionUserService.findOne(id);
		return ResponseUtil.wrapOrNotFound(institutionUserDTO);
	}

	/**
	 * DELETE /institution-users/:id : delete the "id" institutionUser.
	 *
	 * @param id the id of the institutionUserDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/institution-users/{id}")
	@Timed
	public ResponseEntity<Void> deleteInstitutionUser(@PathVariable Long id) {
		log.debug("REST request to delete InstitutionUser : {}", id);
		institutionUserService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	@GetMapping("/institution-users/instituteId")
	@Timed
	public ResponseEntity<?> getUserStatusDetails(@RequestParam("institute_id") Long id) {
		log.debug("REST request to get InstitutionUser : {}", id);
		List<Map<String, Object>> institutionUsers = institutionUserService.findByInstituteId(id);
		return new ResponseEntity<>(institutionUsers, HttpStatus.OK);
	}
	
	@PostMapping("/register-instituion")
	@Timed
	public ResponseEntity<?> registerInstitutionUser(@RequestBody InstitutionUser institutionUser)
			throws URISyntaxException {
		log.debug("REST request to save InstitutionUser ");
		InstitutionUserDTO result = institutionUserService.saveFull(institutionUser);
		return ResponseEntity.created(new URI("/api/institution-users/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/instituion-user/{id}")
	@Timed
	public ResponseEntity<?> getInstitutionUserById(@PathVariable Long id){
		log.debug("REST request to get institution by id : {}",id);
		Optional<InstitutionUserDTO> institutionUserDTO = institutionUserService.findOne(id);
		Map<String, Object> response = new HashMap<String, Object>();
	
		if(institutionUserDTO.isPresent()) {
			response.put("institutionUser", institutionUserDTO.get());
			Optional<InstitutionDTO> institution = institutionService.findOne(institutionUserDTO.get().getInstitutionId());
			if(institution.isPresent()) {
				response.put("institution", institution.get());
			}
			Optional<GlUserDTO> glUser = glUserService.findByOne(institutionUserDTO.get().getUserId());
			if(glUser.isPresent()) {
				glUser.get().setJhiPassword(null);
				response.put("user", glUser.get());
			}
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
