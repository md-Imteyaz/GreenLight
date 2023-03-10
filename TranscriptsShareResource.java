package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.GlStudentQueryService;
import com.gl.platform.service.InstitutionQueryService;
import com.gl.platform.service.InstitutionService;
import com.gl.platform.service.S3StorageService;
import com.gl.platform.service.TranscriptService;
import com.gl.platform.service.TranscriptsQueryService;
import com.gl.platform.service.TranscriptsShareQueryService;
import com.gl.platform.service.TranscriptsShareService;
import com.gl.platform.service.dto.GlStudentCriteria;
import com.gl.platform.service.dto.GlStudentDTO;
import com.gl.platform.service.dto.InstitutionCriteria;
import com.gl.platform.service.dto.InstitutionDTO;
import com.gl.platform.service.dto.TranscriptDTO;
import com.gl.platform.service.dto.TranscriptsCriteria;
import com.gl.platform.service.dto.TranscriptsDTO;
import com.gl.platform.service.dto.TranscriptsShareByUserIDDTO;
import com.gl.platform.service.dto.TranscriptsShareCriteria;
import com.gl.platform.service.dto.TranscriptsShareDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing TranscriptsShare.
 */
@RestController
@RequestMapping("/api")
public class TranscriptsShareResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptsShareResource.class);

	private static final String ENTITY_NAME = "transcriptsShare";

	private final TranscriptsShareService transcriptsShareService;

	private final TranscriptsShareQueryService transcriptsShareQueryService;

	private final TranscriptService transcriptService;

	private final InstitutionService institutionService;

	@Autowired
	private TranscriptsQueryService transcriptsQueryService;

	@Autowired
	private GlStudentQueryService studentQueryService;

	@Autowired
	private InstitutionQueryService institutionQueryService;

	@Autowired
	private S3StorageService s3StorageService;

	public TranscriptsShareResource(TranscriptsShareService transcriptsShareService,
			TranscriptsShareQueryService transcriptsShareQueryService, TranscriptService transcriptService,
			InstitutionService institutionService) {
		this.transcriptsShareService = transcriptsShareService;
		this.transcriptsShareQueryService = transcriptsShareQueryService;
		this.transcriptService = transcriptService;
		this.institutionService = institutionService;
	}

	/**
	 * POST /transcripts-shares : Create a new transcriptsShare.
	 *
	 * @param transcriptsShareDTO the transcriptsShareDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsShareDTO, or with status 400 (Bad Request) if the
	 *         transcriptsShare has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/transcripts-shares")
	@Timed
	public ResponseEntity<TranscriptsShareDTO> createTranscriptsShare(
			@Valid @RequestBody TranscriptsShareDTO transcriptsShareDTO) throws URISyntaxException {
		log.debug("REST request to save TranscriptsShare : {}", transcriptsShareDTO);
		if (transcriptsShareDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptsShare cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		TranscriptsShareDTO result = transcriptsShareService.save(transcriptsShareDTO);
		return ResponseEntity.created(new URI("/api/transcripts-shares/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /transcripts-shares : Updates an existing transcriptsShare.
	 *
	 * @param transcriptsShareDTO the transcriptsShareDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         transcriptsShareDTO, or with status 400 (Bad Request) if the
	 *         transcriptsShareDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the transcriptsShareDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/transcripts-shares")
	@Timed
	public ResponseEntity<TranscriptsShareDTO> updateTranscriptsShare(
			@Valid @RequestBody TranscriptsShareDTO transcriptsShareDTO) throws URISyntaxException {
		log.debug("REST request to update TranscriptsShare : {}", transcriptsShareDTO);
		if (transcriptsShareDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		TranscriptsShareDTO result = transcriptsShareService.save(transcriptsShareDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transcriptsShareDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /transcripts-shares : get all the transcriptsShares.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptsShares in body
	 */
	@GetMapping("/transcripts-shares")
	@Timed
	public ResponseEntity<List<TranscriptsShareDTO>> getAllTranscriptsShares(TranscriptsShareCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get TranscriptsShares by criteria: {}", criteria);
		Page<TranscriptsShareDTO> page = transcriptsShareQueryService.findByCriteria(criteria, pageable);

		List<TranscriptsShareDTO> transcriptShares = page.getContent();

		if (!transcriptShares.isEmpty()) {

			List<Long> transcriptIds = transcriptShares.stream().map(TranscriptsShareDTO::getTranscriptId)
					.collect(Collectors.toList());
			List<Long> studentIds = transcriptShares.stream().map(TranscriptsShareDTO::getStudentId)
					.collect(Collectors.toList());
			List<Long> institutionIds = transcriptShares.stream().map(TranscriptsShareDTO::getInstitutionId)
					.collect(Collectors.toList());

			LongFilter filterByTranscriptIds = new LongFilter();
			filterByTranscriptIds.setIn(transcriptIds);

			TranscriptsCriteria transcriptsCriteria = new TranscriptsCriteria();
			transcriptsCriteria.setId(filterByTranscriptIds);

			LongFilter filterByStudentIds = new LongFilter();
			filterByStudentIds.setIn(studentIds);

			GlStudentCriteria studentCriteria = new GlStudentCriteria();
			studentCriteria.setId(filterByStudentIds);

			LongFilter filterByInstitutionIds = new LongFilter();
			filterByInstitutionIds.setIn(institutionIds);

			InstitutionCriteria institutionCriteria = new InstitutionCriteria();
			institutionCriteria.setId(filterByInstitutionIds);

			Map<Long, TranscriptsDTO> transcripts = transcriptsQueryService.findByCriteria(transcriptsCriteria).stream()
					.collect(Collectors.toMap(TranscriptsDTO::getId, Function.identity()));

			Map<Long, GlStudentDTO> students = studentQueryService.findByCriteria(studentCriteria).stream()
					.collect(Collectors.toMap(GlStudentDTO::getId, Function.identity()));

			Map<Long, InstitutionDTO> institutions = institutionQueryService.findByCriteria(institutionCriteria)
					.stream().collect(Collectors.toMap(InstitutionDTO::getId, Function.identity()));
			;

			for (TranscriptsShareDTO transcriptsShareDTO : transcriptShares) {
				transcriptsShareDTO.setStudent(students.get(transcriptsShareDTO.getStudentId()));
				transcriptsShareDTO.setInstitution(institutions.get(transcriptsShareDTO.getInstitutionId()));
				transcriptsShareDTO.setTranscript(transcripts.get(transcriptsShareDTO.getTranscriptId()));

				if (transcriptsShareDTO.getTranscript() != null) {
					TranscriptsDTO transcript = transcriptsShareDTO.getTranscript();
					String s3Path = "transcripts/" + transcript.getId() + "/pdf_transcript";
					transcript.setUrl(s3StorageService.generatePresignedUrl(s3Path));
				}

			}
		}

		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transcripts-shares");

		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /transcripts-shares/:id : get the "id" transcriptsShare.
	 *
	 * @param id the id of the transcriptsShareDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptsShareDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcripts-shares/{id}")
	@Timed
	public ResponseEntity<TranscriptsShareDTO> getTranscriptsShare(@PathVariable Long id) {
		log.debug("REST request to get TranscriptsShare : {}", id);
		Optional<TranscriptsShareDTO> transcriptsShareDTO = transcriptsShareService.findOne(id);

		if (transcriptsShareDTO.isPresent()) {
			TranscriptsShareDTO transcriptShare = transcriptsShareDTO.get();
			if (transcriptShare.getTranscriptId() != null) {
				TranscriptsDTO transcript = transcriptShare.getTranscript();
				String s3Path = "transcripts/" + transcript.getId() + "/pdf_transcript";
				String s3ViewPath = "transcripts/" + transcript.getId() + "/pdf_transcript_view";

				transcript.setUrl(s3StorageService.generatePresignedUrl(s3Path));
				transcript.setViewUrl(s3StorageService.generatePresignedUrl(s3ViewPath));
			}
		}
		return ResponseUtil.wrapOrNotFound(transcriptsShareDTO);
	}

	/**
	 * DELETE /transcripts-shares/:id : delete the "id" transcriptsShare.
	 *
	 * @param id the id of the transcriptsShareDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/transcripts-shares/{id}")
	@Timed
	public ResponseEntity<Void> deleteTranscriptsShare(@PathVariable Long id) {
		log.debug("REST request to delete TranscriptsShare : {}", id);
		transcriptsShareService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	/**
	 * GET /transcripts-shares/:id : get the "id" transcriptsShare.
	 *
	 * @param id the id of the transcriptsShareDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptsShareDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcripts-shares/userId/{id}")
	@Timed
	public ResponseEntity<List<TranscriptsShareByUserIDDTO>> getTranscriptsShareByUserID(@PathVariable Long id) {
		log.debug("REST request to post getTranscriptsShareByUserID : {}", id);
		List<TranscriptsShareByUserIDDTO> transcriptsShareDTOs = transcriptsShareService
				.getTranscriptsShareByUserID(id);

		if (transcriptsShareDTOs != null) {
			return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
					.body(transcriptsShareDTOs);
		} else {
			log.warn("Returning null");
			return null;
		}
	}

	/**
	 * GET /transcripts-shares : get all the transcriptsShares.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptsShares in body
	 */
	@GetMapping("/transcript-share/{institutionId}")
	@Timed
	public ResponseEntity<?> getAllTranscriptsSharesByCriteria(@PathVariable Long institutionId) {
		log.debug("REST request to get Transcript-Shares by institutionId: {}", institutionId);
		List<Object> responseData = transcriptsShareService.findTranscriptSharesByInstitute(institutionId);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@GetMapping("/transcript-share")
	@Timed
	public ResponseEntity<List<Object>> getListOfObjects(TranscriptsShareCriteria criteria) {

		List<Object> result = new ArrayList<Object>();

		List<TranscriptsShareDTO> listTranscriptsShareDTO = transcriptsShareQueryService.findByCriteria(criteria);
		if (listTranscriptsShareDTO.isEmpty()) {
			return new ResponseEntity<>(result, HttpStatus.OK);
		}

		for (TranscriptsShareDTO transcriptsShareDTO : listTranscriptsShareDTO) {

			Map<String, Object> response = new HashMap<String, Object>();

			response.put("recepient_email", transcriptsShareDTO.getShareWithEmailAddress());
			response.put("shared_date", transcriptsShareDTO.getShareDate());
			response.put("status", null);

			Optional<TranscriptDTO> transcriptDTO = transcriptService.findOne(transcriptsShareDTO.getTranscriptId());

			if (transcriptDTO.isPresent()) {
				response.put("shared_transcript_name", transcriptDTO.get().getName());
			}

			Optional<InstitutionDTO> institutionDTO = institutionService
					.findOne(transcriptsShareDTO.getInstitutionId());

			if (institutionDTO.isPresent()) {
				response.put("recipent_institution_name", institutionDTO.get().getName());
			}
			
			result.add(response);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}
