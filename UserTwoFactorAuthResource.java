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
import com.gl.platform.service.UserTwoFactorAuthQueryService;
import com.gl.platform.service.UserTwoFactorAuthService;
import com.gl.platform.service.dto.UserTwoFactorAuthCriteria;
import com.gl.platform.service.dto.UserTwoFactorAuthDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing UserTwoFactorAuth.
 */
@RestController
@RequestMapping("/api")
public class UserTwoFactorAuthResource {

	private final Logger log = LoggerFactory.getLogger(UserTwoFactorAuthResource.class);

	private static final String ENTITY_NAME = "userTwoFactorAuth";

	private final UserTwoFactorAuthService userTwoFactorAuthService;

	private final UserTwoFactorAuthQueryService userTwoFactorAuthQueryService;

	public UserTwoFactorAuthResource(UserTwoFactorAuthService userTwoFactorAuthService,
			UserTwoFactorAuthQueryService userTwoFactorAuthQueryService) {
		this.userTwoFactorAuthService = userTwoFactorAuthService;
		this.userTwoFactorAuthQueryService = userTwoFactorAuthQueryService;
	}

	/**
	 * POST /user-two-factor-auths : Create a new userTwoFactorAuth.
	 *
	 * @param userTwoFactorAuthDTO the userTwoFactorAuthDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         userTwoFactorAuthDTO, or with status 400 (Bad Request) if the
	 *         userTwoFactorAuth has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/user-two-factor-auths")
	@Timed
	public ResponseEntity<UserTwoFactorAuthDTO> createUserTwoFactorAuth(
			@Valid @RequestBody UserTwoFactorAuthDTO userTwoFactorAuthDTO) throws URISyntaxException {
		log.debug("REST request to save UserTwoFactorAuth : {}", userTwoFactorAuthDTO);
		if (userTwoFactorAuthDTO.getId() != null) {
			throw new BadRequestAlertException("A new userTwoFactorAuth cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		UserTwoFactorAuthDTO result = userTwoFactorAuthService.save(userTwoFactorAuthDTO);
		return ResponseEntity.created(new URI("/api/user-two-factor-auths/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /user-two-factor-auths : Updates an existing userTwoFactorAuth.
	 *
	 * @param userTwoFactorAuthDTO the userTwoFactorAuthDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         userTwoFactorAuthDTO, or with status 400 (Bad Request) if the
	 *         userTwoFactorAuthDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the userTwoFactorAuthDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/user-two-factor-auths")
	@Timed
	public ResponseEntity<UserTwoFactorAuthDTO> updateUserTwoFactorAuth(
			@Valid @RequestBody UserTwoFactorAuthDTO userTwoFactorAuthDTO) throws URISyntaxException {
		log.debug("REST request to update UserTwoFactorAuth : {}", userTwoFactorAuthDTO);
		if (userTwoFactorAuthDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		UserTwoFactorAuthDTO result = userTwoFactorAuthService.save(userTwoFactorAuthDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userTwoFactorAuthDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /user-two-factor-auths : get all the userTwoFactorAuths.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         userTwoFactorAuths in body
	 */
	@GetMapping("/user-two-factor-auths")
	@Timed
	public ResponseEntity<List<UserTwoFactorAuthDTO>> getAllUserTwoFactorAuths(UserTwoFactorAuthCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get UserTwoFactorAuths by criteria: {}", criteria);
		Page<UserTwoFactorAuthDTO> page = userTwoFactorAuthQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/user-two-factor-auths");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /user-two-factor-auths/:id : get the "id" userTwoFactorAuth.
	 *
	 * @param id the id of the userTwoFactorAuthDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         userTwoFactorAuthDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/user-two-factor-auths/{id}")
	@Timed
	public ResponseEntity<UserTwoFactorAuthDTO> getUserTwoFactorAuth(@PathVariable Long id) {
		log.debug("REST request to get UserTwoFactorAuth : {}", id);
		Optional<UserTwoFactorAuthDTO> userTwoFactorAuthDTO = userTwoFactorAuthService.findOne(id);
		return ResponseUtil.wrapOrNotFound(userTwoFactorAuthDTO);
	}

	/**
	 * DELETE /user-two-factor-auths/:id : delete the "id" userTwoFactorAuth.
	 *
	 * @param id the id of the userTwoFactorAuthDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/user-two-factor-auths/{id}")
	@Timed
	public ResponseEntity<Void> deleteUserTwoFactorAuth(@PathVariable Long id) {
		log.debug("REST request to delete UserTwoFactorAuth : {}", id);
		userTwoFactorAuthService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
