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
import com.gl.platform.service.UserRolesQueryService;
import com.gl.platform.service.UserRolesService;
import com.gl.platform.service.dto.UserRolesCriteria;
import com.gl.platform.service.dto.UserRolesDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing UserRoles.
 */
@RestController
@RequestMapping("/api")
public class UserRolesResource {

	private final Logger log = LoggerFactory.getLogger(UserRolesResource.class);

	private static final String ENTITY_NAME = "userRoles";

	private final UserRolesService userRolesService;

	private final UserRolesQueryService userRolesQueryService;

	public UserRolesResource(UserRolesService userRolesService, UserRolesQueryService userRolesQueryService) {
		this.userRolesService = userRolesService;
		this.userRolesQueryService = userRolesQueryService;
	}

	/**
	 * POST /user-roles : Create a new userRoles.
	 *
	 * @param userRolesDTO the userRolesDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         userRolesDTO, or with status 400 (Bad Request) if the userRoles has
	 *         already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/user-roles")
	@Timed
	public ResponseEntity<UserRolesDTO> createUserRoles(@RequestBody UserRolesDTO userRolesDTO)
			throws URISyntaxException {
		log.debug("REST request to save UserRoles : {}", userRolesDTO);
		if (userRolesDTO.getId() != null) {
			throw new BadRequestAlertException("A new userRoles cannot already have an ID", ENTITY_NAME, "idexists");
		}
		UserRolesDTO result = userRolesService.save(userRolesDTO);
		return ResponseEntity.created(new URI("/api/user-roles/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /user-roles : Updates an existing userRoles.
	 *
	 * @param userRolesDTO the userRolesDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         userRolesDTO, or with status 400 (Bad Request) if the userRolesDTO is
	 *         not valid, or with status 500 (Internal Server Error) if the
	 *         userRolesDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/user-roles")
	@Timed
	public ResponseEntity<UserRolesDTO> updateUserRoles(@RequestBody UserRolesDTO userRolesDTO)
			throws URISyntaxException {
		log.debug("REST request to update UserRoles : {}", userRolesDTO);
		if (userRolesDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		UserRolesDTO result = userRolesService.save(userRolesDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userRolesDTO.getId().toString())).body(result);
	}

	/**
	 * GET /user-roles : get all the userRoles.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of userRoles in
	 *         body
	 */
	@GetMapping("/user-roles")
	@Timed
	public ResponseEntity<List<UserRolesDTO>> getAllUserRoles(UserRolesCriteria criteria, Pageable pageable) {
		log.debug("REST request to get UserRoles by criteria: {}", criteria);
		Page<UserRolesDTO> page = userRolesQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-roles");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /user-roles/:id : get the "id" userRoles.
	 *
	 * @param id the id of the userRolesDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         userRolesDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/user-roles/{id}")
	@Timed
	public ResponseEntity<UserRolesDTO> getUserRoles(@PathVariable Long id) {
		log.debug("REST request to get UserRoles : {}", id);
		Optional<UserRolesDTO> userRolesDTO = userRolesService.findOne(id);
		return ResponseUtil.wrapOrNotFound(userRolesDTO);
	}

	/**
	 * DELETE /user-roles/:id : delete the "id" userRoles.
	 *
	 * @param id the id of the userRolesDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/user-roles/{id}")
	@Timed
	public ResponseEntity<Void> deleteUserRoles(@PathVariable Long id) {
		log.debug("REST request to delete UserRoles : {}", id);
		userRolesService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
