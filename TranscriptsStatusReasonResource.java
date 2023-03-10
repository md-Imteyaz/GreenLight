package com.gl.platform.web.rest;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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
import com.gl.platform.service.TranscriptsStatusReasonQueryService;
import com.gl.platform.service.TranscriptsStatusReasonService;
import com.gl.platform.service.UniversityService;
import com.gl.platform.service.dto.TranscriptsStatusReasonDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

/**
 * REST controller for managing TranscriptsStatusReason.
 */
@RestController
@RequestMapping("/api")
public class TranscriptsStatusReasonResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptsStatusReasonResource.class);

	private static final String ENTITY_NAME = "transcriptsStatusReason";

	private final TranscriptsStatusReasonService transcriptsStatusReasonService;
	
	
	@Autowired
	private UniversityService universityService;
	
	@Autowired
	private AuthorizationService authorizationService;


	public TranscriptsStatusReasonResource(TranscriptsStatusReasonService transcriptsStatusReasonService) {
		this.transcriptsStatusReasonService = transcriptsStatusReasonService;
	}

	/**
	 * POST /transcripts-status-reasons : Create a new transcriptsStatusReason.
	 *
	 * @param transcriptsStatusReasonDTO the transcriptsStatusReasonDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsStatusReasonDTO, or with status 400 (Bad Request) if the
	 *         transcriptsStatusReason has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/transcripts-status-reasons")
	@Timed
	public ResponseEntity<?> createTranscriptsStatusReason(
			 @RequestBody TranscriptsStatusReasonDTO transcriptsStatusReasonDTO) throws URISyntaxException {
		log.debug("REST request to save TranscriptsStatusReason : {}", transcriptsStatusReasonDTO);
		
		if(!authorizationService.studentOwnedByInstitution(transcriptsStatusReasonDTO.getStudentId())) {
			throw new AccessDeniedException("not the owner");
		}
		
		if (transcriptsStatusReasonDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptsStatusReason cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		
		
		
		Map<String, Object> response = transcriptsStatusReasonService.save(transcriptsStatusReasonDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * PUT /transcripts-status-reasons : Updates an existing
	 * transcriptsStatusReason.
	 *
	 * @param transcriptsStatusReasonDTO the transcriptsStatusReasonDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         transcriptsStatusReasonDTO, or with status 400 (Bad Request) if the
	 *         transcriptsStatusReasonDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the transcriptsStatusReasonDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws NotFoundException 
	 */
	@PutMapping("/transcripts-status-reasons")
	@Timed
	public ResponseEntity<?> updateTranscriptsStatusReason(
			@Valid @RequestBody TranscriptsStatusReasonDTO transcriptsStatusReasonDTO) throws URISyntaxException, NotFoundException {
		log.debug("REST request to update TranscriptsStatusReason : {}", transcriptsStatusReasonDTO);
		
		if(!authorizationService.studentOwnedByInstitution(transcriptsStatusReasonDTO.getStudentId())) {
			throw new AccessDeniedException("not the owner");
		}
		
		
		Map<String, Object> result = transcriptsStatusReasonService.saveUpdate(transcriptsStatusReasonDTO);
	
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

/*
	*//**
	 * GET /transcripts-status-reasons : get all the transcriptsStatusReasons.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptsStatusReasons in body
	 *//*
	@GetMapping("/transcripts-status-reason")
	@Timed
	public ResponseEntity<List<TranscriptsStatusReasonDTO>> getAllTranscriptsStatusReasons(
			TranscriptsStatusReasonCriteria criteria) {
		
		log.debug("REST request to get TranscriptsStatusReasons by criteria: {}", criteria);
		
		
		List<TranscriptsStatusReasonDTO> list = transcriptsStatusReasonQueryService.findByCriteria(criteria);
		
		

		return new ResponseEntity<>(list, HttpStatus.OK);
	}*/
	
	@GetMapping("/view-holds-search/{id}/{searchTerm}")
	@Timed
	public ResponseEntity<?> getViewHolds(
			@PathVariable Long id,@PathVariable String searchTerm) {
		log.debug("REST request to get TranscriptsStatusReasons by serachTerm: {}", searchTerm);
		
		if(!authorizationService.ownedByInstitution(id)) {
			throw new AccessDeniedException("not the owner");
		}
		
		
		List<Map<String, Object>> list = universityService.getViewHoldSearch(id, searchTerm);

		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	
	/**
	 * GET /transcripts-status-reasons/:id : get the "id" transcriptsStatusReason.
	 *
	 * @param id the id of the transcriptsStatusReasonDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptsStatusReasonDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcripts-status-reasons/{id}")
	@Timed
	public ResponseEntity<?> getTranscriptsStatusReason(@PathVariable Long id) {
		log.debug("REST request to get TranscriptsStatusReason : {}", id);
		
		if(!authorizationService.ownedByInstitution(id)) {
			throw new AccessDeniedException("not the owner");
		}
		
		
		List<Map<String, Object>> result = transcriptsStatusReasonService.getArrayOfStatusOptimized(id);
		
		return new ResponseEntity<>(result,HttpStatus.OK);
	}

	/**
	 * DELETE /transcripts-status-reasons/:id : delete the "id"
	 * transcriptsStatusReason.
	 *
	 * @param id the id of the transcriptsStatusReasonDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/transcripts-status-reasons/{id}")
	@Timed
	public ResponseEntity<Void> deleteTranscriptsStatusReason(@PathVariable Long id) {
		log.debug("REST request to delete TranscriptsStatusReason : {}", id);
		
		 Optional<TranscriptsStatusReasonDTO> optTranscriptStatus = transcriptsStatusReasonService.findOne(id);
		 
		if (optTranscriptStatus.isPresent()) {
			if (!authorizationService.studentOwnedByInstitution(optTranscriptStatus.get().getStudentId())) {
				throw new AccessDeniedException("not the owner");
			}
		}
		
		transcriptsStatusReasonService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
