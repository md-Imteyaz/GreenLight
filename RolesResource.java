package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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
import com.gl.platform.domain.Roles;
import com.gl.platform.service.RolesQueryService;
import com.gl.platform.service.RolesService;
import com.gl.platform.service.dto.RolesCriteria;
import com.gl.platform.service.dto.RolesDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Roles.
 */
@RestController
@RequestMapping("/api")
public class RolesResource {

	private final Logger log = LoggerFactory.getLogger(RolesResource.class);

	private static final String ENTITY_NAME = Roles.class.getSimpleName().toLowerCase();

	private final RolesService rolesService;

	private final RolesQueryService rolesQueryService;

	public RolesResource(RolesService rolesService, RolesQueryService rolesQueryService) {
		this.rolesService = rolesService;
		this.rolesQueryService = rolesQueryService;
	}

	/**
	 * POST /roles : Create a new roles.
	 *
	 * @param rolesDTO the rolesDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         rolesDTO, or with status 400 (Bad Request) if the roles has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/roles")
	@Timed
	public ResponseEntity<RolesDTO> createRoles(@Valid @RequestBody RolesDTO rolesDTO) throws URISyntaxException {
		log.debug("REST request to save Roles : {}", rolesDTO);
		if (rolesDTO.getId() != null) {
			throw new BadRequestAlertException("A new roles cannot already have an ID", ENTITY_NAME, "idexists");
		}
		RolesDTO result = rolesService.save(rolesDTO);
		return ResponseEntity.created(new URI("/api/roles/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /roles : Updates an existing roles.
	 *
	 * @param rolesDTO the rolesDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         rolesDTO, or with status 400 (Bad Request) if the rolesDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the rolesDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/roles")
	@Timed
	public ResponseEntity<RolesDTO> updateRoles(@Valid @RequestBody RolesDTO rolesDTO) throws URISyntaxException {
		log.debug("REST request to update Roles : {}", rolesDTO);
		if (rolesDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		RolesDTO result = rolesService.save(rolesDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, rolesDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /roles : get all the roles.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of roles in body
	 */
	@GetMapping("/roles")
	@Timed
	public ResponseEntity<List<RolesDTO>> getAllRoles(RolesCriteria criteria, Pageable pageable) {
		log.debug("REST request to get Roles by criteria: {}", criteria);
		Page<RolesDTO> page = rolesQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/roles");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /roles/:id : get the "id" roles.
	 *
	 * @param id the id of the rolesDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the rolesDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/roles/{id}")
	@Timed
	public ResponseEntity<RolesDTO> getRoles(@PathVariable Long id) {
		log.debug("REST request to get Roles : {}", id);
		Optional<RolesDTO> rolesDTO = rolesService.findOne(id);
		return ResponseUtil.wrapOrNotFound(rolesDTO);
	}

	/**
	 * DELETE /roles/:id : delete the "id" roles.
	 *
	 * @param id the id of the rolesDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/roles/{id}")
	@Timed
	public ResponseEntity<Void> deleteRoles(@PathVariable Long id) {
		log.debug("REST request to delete Roles : {}", id);
		rolesService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
