package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.UserActivityQueryService;
import com.gl.platform.service.UserActivityService;
import com.gl.platform.service.dto.FerpaDTO;
import com.gl.platform.service.dto.UserActivityCriteria;
import com.gl.platform.service.dto.UserActivityDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing UserActivity.
 */
@RestController
@RequestMapping("/api")
public class UserActivityResource {

	private final Logger log = LoggerFactory.getLogger(UserActivityResource.class);

	private static final String ENTITY_NAME = "userActivity";

	private final UserActivityService userActivityService;

	private final UserActivityQueryService userActivityQueryService;
	
	@Autowired
	private AuthorizationService authorizationService;

	public UserActivityResource(UserActivityService userActivityService,
			UserActivityQueryService userActivityQueryService) {
		this.userActivityService = userActivityService;
		this.userActivityQueryService = userActivityQueryService;
	}

	/**
	 * POST /user-activities : Create a new userActivity.
	 *
	 * @param userActivityDTO the userActivityDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         userActivityDTO, or with status 400 (Bad Request) if the userActivity
	 *         has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/user-activities")
	@Timed
	public ResponseEntity<UserActivityDTO> createUserActivity(@Valid @RequestBody UserActivityDTO userActivityDTO)
			throws URISyntaxException {
		log.debug("REST request to save UserActivity : {}", userActivityDTO);
		if (userActivityDTO.getId() != null) {
			throw new BadRequestAlertException("A new userActivity cannot already have an ID", ENTITY_NAME, "idexists");
		}
		UserActivityDTO result = userActivityService.save(userActivityDTO);
		return ResponseEntity.created(new URI("/api/user-activities/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /user-activities : Updates an existing userActivity.
	 *
	 * @param userActivityDTO the userActivityDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         userActivityDTO, or with status 400 (Bad Request) if the
	 *         userActivityDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the userActivityDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/user-activities")
	@Timed
	public ResponseEntity<UserActivityDTO> updateUserActivity(@Valid @RequestBody UserActivityDTO userActivityDTO)
			throws URISyntaxException {
		log.debug("REST request to update UserActivity : {}", userActivityDTO);
		if (userActivityDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		UserActivityDTO result = userActivityService.save(userActivityDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userActivityDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /user-activities : get all the userActivities.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         userActivities in body
	 */
	@GetMapping("/user-activities")
	@Timed
	public ResponseEntity<List<UserActivityDTO>> getAllUserActivities(UserActivityCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get UserActivities by criteria: {}", criteria);
				Page<UserActivityDTO> pages = userActivityQueryService.findByCriteria(criteria, pageable);
		for(UserActivityDTO page : pages) {
			if (!authorizationService.ownedByUserOnly(page.getUserId())) {
				throw new AccessDeniedException("not the owner");
			}
			
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pages, "/api/user-activities");
		return new ResponseEntity<>(pages.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /user-activities/:id : get the "id" userActivity.
	 *
	 * @param id the id of the userActivityDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         userActivityDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/user-activities/{id}")
	@Timed
	public ResponseEntity<UserActivityDTO> getUserActivity(@PathVariable Long id) {
		log.debug("REST request to get UserActivity : {}", id);
		Optional<UserActivityDTO> userActivityDTO = userActivityService.findOne(id);
		return ResponseUtil.wrapOrNotFound(userActivityDTO);
	}

	/**
	 * DELETE /user-activities/:id : delete the "id" userActivity.
	 *
	 * @param id the id of the userActivityDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/user-activities/{id}")
	@Timed
	public ResponseEntity<Void> deleteUserActivity(@PathVariable Long id) {
		log.debug("REST request to delete UserActivity : {}", id);
		userActivityService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
	
	/**
	 * GET /activity : get all the userActivities.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         userActivities in body
	 */
	@GetMapping("/activity")
	@Timed
	public ResponseEntity<List<UserActivityDTO>> getAllActivitiesByCriteria(UserActivityCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get UserActivities by criteria: {}", criteria);
		Page<UserActivityDTO> page = userActivityQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/activity");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}
		
}
