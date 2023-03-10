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
import com.gl.platform.service.TranscriptCoursesQueryService;
import com.gl.platform.service.TranscriptCoursesService;
import com.gl.platform.service.dto.TranscriptCoursesCriteria;
import com.gl.platform.service.dto.TranscriptCoursesDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing TranscriptCourses.
 */
@RestController
@RequestMapping("/api")
public class TranscriptCoursesResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptCoursesResource.class);

	private static final String ENTITY_NAME = "transcriptCourses";

	private final TranscriptCoursesService transcriptCoursesService;

	private final TranscriptCoursesQueryService transcriptCoursesQueryService;

	public TranscriptCoursesResource(TranscriptCoursesService transcriptCoursesService,
			TranscriptCoursesQueryService transcriptCoursesQueryService) {
		this.transcriptCoursesService = transcriptCoursesService;
		this.transcriptCoursesQueryService = transcriptCoursesQueryService;
	}

	/**
	 * POST /transcript-courses : Create a new transcriptCourses.
	 *
	 * @param transcriptCoursesDTO the transcriptCoursesDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptCoursesDTO, or with status 400 (Bad Request) if the
	 *         transcriptCourses has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/transcript-courses")
	@Timed
	public ResponseEntity<TranscriptCoursesDTO> createTranscriptCourses(
			@Valid @RequestBody TranscriptCoursesDTO transcriptCoursesDTO) throws URISyntaxException {
		log.debug("REST request to save TranscriptCourses : {}", transcriptCoursesDTO);
		if (transcriptCoursesDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptCourses cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		TranscriptCoursesDTO result = transcriptCoursesService.save(transcriptCoursesDTO);
		return ResponseEntity.created(new URI("/api/transcript-courses/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /transcript-courses : Updates an existing transcriptCourses.
	 *
	 * @param transcriptCoursesDTO the transcriptCoursesDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         transcriptCoursesDTO, or with status 400 (Bad Request) if the
	 *         transcriptCoursesDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the transcriptCoursesDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/transcript-courses")
	@Timed
	public ResponseEntity<TranscriptCoursesDTO> updateTranscriptCourses(
			@Valid @RequestBody TranscriptCoursesDTO transcriptCoursesDTO) throws URISyntaxException {
		log.debug("REST request to update TranscriptCourses : {}", transcriptCoursesDTO);
		if (transcriptCoursesDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		TranscriptCoursesDTO result = transcriptCoursesService.save(transcriptCoursesDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transcriptCoursesDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /transcript-courses : get all the transcriptCourses.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptCourses in body
	 */
	@GetMapping("/transcript-courses")
	@Timed
	public ResponseEntity<List<TranscriptCoursesDTO>> getAllTranscriptCourses(TranscriptCoursesCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get TranscriptCourses by criteria: {}", criteria);
		Page<TranscriptCoursesDTO> page = transcriptCoursesQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transcript-courses");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /transcript-courses/:id : get the "id" transcriptCourses.
	 *
	 * @param id the id of the transcriptCoursesDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptCoursesDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcript-courses/{id}")
	@Timed
	public ResponseEntity<TranscriptCoursesDTO> getTranscriptCourses(@PathVariable Long id) {
		log.debug("REST request to get TranscriptCourses : {}", id);
		Optional<TranscriptCoursesDTO> transcriptCoursesDTO = transcriptCoursesService.findOne(id);
		return ResponseUtil.wrapOrNotFound(transcriptCoursesDTO);
	}

	/**
	 * DELETE /transcript-courses/:id : delete the "id" transcriptCourses.
	 *
	 * @param id the id of the transcriptCoursesDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/transcript-courses/{id}")
	@Timed
	public ResponseEntity<Void> deleteTranscriptCourses(@PathVariable Long id) {
		log.debug("REST request to delete TranscriptCourses : {}", id);
		transcriptCoursesService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
