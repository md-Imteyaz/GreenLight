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
import com.gl.platform.domain.StateCounty;
import com.gl.platform.service.StateService;
import com.gl.platform.service.dto.StateDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing State.
 */
@RestController
@RequestMapping("/api")
public class StateResource {

	private final Logger log = LoggerFactory.getLogger(StateResource.class);

	private static final String ENTITY_NAME = "state";

	private final StateService stateService;


	public StateResource(StateService stateService) {
		this.stateService = stateService;
	}

	/**
	 * POST /states : Create a new state.
	 *
	 * @param stateDTO the stateDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         stateDTO, or with status 400 (Bad Request) if the state has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/states")
	@Timed
	public ResponseEntity<StateDTO> createState(@RequestBody StateDTO stateDTO) throws URISyntaxException {
		log.debug("REST request to save State : {}", stateDTO);
		if (stateDTO.getId() != null) {
			throw new BadRequestAlertException("A new state cannot already have an ID", ENTITY_NAME, "idexists");
		}
		StateDTO result = stateService.save(stateDTO);
		return ResponseEntity.created(new URI("/api/states/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /states : Updates an existing state.
	 *
	 * @param stateDTO the stateDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         stateDTO, or with status 400 (Bad Request) if the stateDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the stateDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/states")
	@Timed
	public ResponseEntity<StateDTO> updateState(@RequestBody StateDTO stateDTO) throws URISyntaxException {
		log.debug("REST request to update State : {}", stateDTO);
		if (stateDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		StateDTO result = stateService.save(stateDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, stateDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /states : get all the states.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of states in
	 *         body
	 */
	@GetMapping("/states")
	@Timed
	public ResponseEntity<List<StateDTO>> getAllStates(Pageable pageable) {
		log.debug("REST request to get a page of States");
		Page<StateDTO> page = stateService.findAll(pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/states");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	
	
	
	
	
	
	/**
	 * GET /states/:id : get the "id" state.
	 *
	 * @param id the id of the stateDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the stateDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/states/{id}")
	@Timed
	public ResponseEntity<StateDTO> getState(@PathVariable Long id) {
		log.debug("REST request to get State : {}", id);
		Optional<StateDTO> stateDTO = stateService.findOne(id);
		return ResponseUtil.wrapOrNotFound(stateDTO);
	}

	
	@GetMapping("/states-list")
	@Timed
	public ResponseEntity<List<StateDTO>> getStates() {
		log.debug("REST request to get List of State :");
		List<StateDTO> states = stateService.listOfStates();
		return new ResponseEntity<>(states,HttpStatus.OK);
	}
	
	
	
	
	
	/**
	 * DELETE /states/:id : delete the "id" state.
	 *
	 * @param id the id of the stateDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/states/{id}")
	@Timed
	public ResponseEntity<Void> deleteState(@PathVariable Long id) {
		log.debug("REST request to delete State : {}", id);
		stateService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
	
	
	/**
	 * GET /states/:id : get the "id" state.
	 *
	 * @param id the id of the stateDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the stateDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/state/{stateId}/counties")
	@Timed
	public ResponseEntity<List<StateCounty>> getStateCounties(@PathVariable Long stateId) {
		log.debug("REST request to get State counties by stateId : {}", stateId);
		List<StateCounty> counties = stateService.findStateCountiesByStateId(stateId);
		return new ResponseEntity<>(counties,HttpStatus.OK);
	}
}
