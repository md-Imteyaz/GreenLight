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
import com.gl.platform.service.TranscriptShareStatusQueryService;
import com.gl.platform.service.TranscriptShareStatusService;
import com.gl.platform.service.dto.TranscriptShareStatusCriteria;
import com.gl.platform.service.dto.TranscriptShareStatusDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing TranscriptShareStatus.
 */
@RestController
@RequestMapping("/api")
public class TranscriptShareStatusResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptShareStatusResource.class);

	private static final String ENTITY_NAME = "transcriptShareStatus";

	private final TranscriptShareStatusService transcriptShareStatusService;

	private final TranscriptShareStatusQueryService transcriptShareStatusQueryService;

	public TranscriptShareStatusResource(TranscriptShareStatusService transcriptShareStatusService,
			TranscriptShareStatusQueryService transcriptShareStatusQueryService) {
		this.transcriptShareStatusService = transcriptShareStatusService;
		this.transcriptShareStatusQueryService = transcriptShareStatusQueryService;
	}

	/**
	 * POST /transcript-share-statuses : Create a new transcriptShareStatus.
	 *
	 * @param transcriptShareStatusDTO the transcriptShareStatusDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptShareStatusDTO, or with status 400 (Bad Request) if the
	 *         transcriptShareStatus has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/transcript-share-statuses")
	@Timed
	public ResponseEntity<TranscriptShareStatusDTO> createTranscriptShareStatus(
			@Valid @RequestBody TranscriptShareStatusDTO transcriptShareStatusDTO) throws URISyntaxException {
		log.debug("REST request to save TranscriptShareStatus : {}", transcriptShareStatusDTO);
		if (transcriptShareStatusDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptShareStatus cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		TranscriptShareStatusDTO result = transcriptShareStatusService.save(transcriptShareStatusDTO);
		return ResponseEntity.created(new URI("/api/transcript-share-statuses/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /transcript-share-statuses : Updates an existing transcriptShareStatus.
	 *
	 * @param transcriptShareStatusDTO the transcriptShareStatusDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         transcriptShareStatusDTO, or with status 400 (Bad Request) if the
	 *         transcriptShareStatusDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the transcriptShareStatusDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/transcript-share-statuses")
	@Timed
	public ResponseEntity<TranscriptShareStatusDTO> updateTranscriptShareStatus(
			@Valid @RequestBody TranscriptShareStatusDTO transcriptShareStatusDTO) throws URISyntaxException {
		log.debug("REST request to update TranscriptShareStatus : {}", transcriptShareStatusDTO);
		if (transcriptShareStatusDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		TranscriptShareStatusDTO result = transcriptShareStatusService.save(transcriptShareStatusDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transcriptShareStatusDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /transcript-share-statuses : get all the transcriptShareStatuses.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptShareStatuses in body
	 */
	@GetMapping("/transcript-share-statuses")
	@Timed
	public ResponseEntity<List<TranscriptShareStatusDTO>> getAllTranscriptShareStatuses(
	    TranscriptShareStatusCriteria criteria, Pageable pageable) {
		log.debug("REST request to get TranscriptShareStatuses by criteria: {}", criteria);
		Page<TranscriptShareStatusDTO> page = transcriptShareStatusQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transcript-share-statuses");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /transcript-share-statuses/:id : get the "id" transcriptShareStatus.
	 *
	 * @param id the id of the transcriptShareStatusDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptShareStatusDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcript-share-statuses/{id}")
	@Timed
	public ResponseEntity<TranscriptShareStatusDTO> getTranscriptShareStatus(@PathVariable Long id) {
		log.debug("REST request to get TranscriptShareStatus : {}", id);
		Optional<TranscriptShareStatusDTO> transcriptShareStatusDTO = transcriptShareStatusService.findOne(id);
		return ResponseUtil.wrapOrNotFound(transcriptShareStatusDTO);
	}

	/**
	 * DELETE /transcript-share-statuses/:id : delete the "id"
	 * transcriptShareStatus.
	 *
	 * @param id the id of the transcriptShareStatusDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/transcript-share-statuses/{id}")
	@Timed
	public ResponseEntity<Void> deleteTranscriptShareStatus(@PathVariable Long id) {
		log.debug("REST request to delete TranscriptShareStatus : {}", id);
		transcriptShareStatusService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
