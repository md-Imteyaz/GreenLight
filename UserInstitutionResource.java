package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.UserInstitutionQueryService;
import com.gl.platform.service.UserInstitutionService;
import com.gl.platform.service.dto.UserInstitutionCriteria;
import com.gl.platform.service.dto.UserInstitutionDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing UserInstitution.
 */
@RestController
@RequestMapping("/api")
public class UserInstitutionResource {

	private final Logger log = LoggerFactory.getLogger(UserInstitutionResource.class);

	private static final String ENTITY_NAME = "userInstitution";

	private final UserInstitutionService userInstitutionService;

	private final UserInstitutionQueryService userInstitutionQueryService;

	public UserInstitutionResource(UserInstitutionService userInstitutionService,
			UserInstitutionQueryService userInstitutionQueryService) {
		this.userInstitutionService = userInstitutionService;
		this.userInstitutionQueryService = userInstitutionQueryService;
	}

	/**
	 * POST /user-institutions : Create a new userInstitution.
	 *
	 * @param userInstitutionDTO the userInstitutionDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         userInstitutionDTO, or with status 400 (Bad Request) if the
	 *         userInstitution has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/user-institutions")
	@Timed
	public ResponseEntity<UserInstitutionDTO> createUserInstitution(@RequestBody UserInstitutionDTO userInstitutionDTO)
			throws URISyntaxException {
		log.debug("REST request to save UserInstitution : {}", userInstitutionDTO);
		if (userInstitutionDTO.getId() != null) {
			throw new BadRequestAlertException("A new userInstitution cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		UserInstitutionDTO result = userInstitutionService.save(userInstitutionDTO);
		return ResponseEntity.created(new URI("/api/user-institutions/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /user-institutions : Updates an existing userInstitution.
	 *
	 * @param userInstitutionDTO the userInstitutionDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         userInstitutionDTO, or with status 400 (Bad Request) if the
	 *         userInstitutionDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the userInstitutionDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/user-institutions")
	@Timed
	public ResponseEntity<UserInstitutionDTO> updateUserInstitution(@RequestBody UserInstitutionDTO userInstitutionDTO)
			throws URISyntaxException {
		log.debug("REST request to update UserInstitution : {}", userInstitutionDTO);
		if (userInstitutionDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		UserInstitutionDTO result = userInstitutionService.save(userInstitutionDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userInstitutionDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /user-institutions : get all the userInstitutions.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         userInstitutions in body
	 */
	@GetMapping("/user-institutions")
	@Timed
	public ResponseEntity<List<UserInstitutionDTO>> getAllUserInstitutions(UserInstitutionCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get UserInstitutions by criteria: {}", criteria);
		Page<UserInstitutionDTO> page = userInstitutionQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-institutions");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /user-institutions/:id : get the "id" userInstitution.
	 *
	 * @param id the id of the userInstitutionDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         userInstitutionDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/user-institutions/{id}")
	@Timed
	public ResponseEntity<UserInstitutionDTO> getUserInstitution(@PathVariable Long id) {
		log.debug("REST request to get UserInstitution : {}", id);
		Optional<UserInstitutionDTO> userInstitutionDTO = userInstitutionService.findOne(id);
		return ResponseUtil.wrapOrNotFound(userInstitutionDTO);
	}

	/**
	 * DELETE /user-institutions/:id : delete the "id" userInstitution.
	 *
	 * @param id the id of the userInstitutionDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/user-institutions/{id}")
	@Timed
	public ResponseEntity<Void> deleteUserInstitution(@PathVariable Long id) {
		log.debug("REST request to delete UserInstitution : {}", id);
		userInstitutionService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
