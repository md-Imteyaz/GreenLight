package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.TranscriptRequestQueryService;
import com.gl.platform.service.TranscriptRequestService;
import com.gl.platform.service.dto.CredentialRequests;
import com.gl.platform.service.dto.TranscriptRequestCriteria;
import com.gl.platform.service.dto.TranscriptRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import javassist.NotFoundException;

/**
 * REST controller for managing TranscriptRequest.
 */
@RestController
@RequestMapping("/api")
public class TranscriptRequestResource {
	private final Logger log = LoggerFactory.getLogger(TranscriptRequestResource.class);

	private static final String ENTITY_NAME = "transcripRequest";

	private final TranscriptRequestService transcriptRequestService;

	private final TranscriptRequestQueryService transcriptRequestQueryService;

	@Autowired
	private AuthorizationService authorizationService;

	public TranscriptRequestResource(TranscriptRequestService transcriptRequestService,
			TranscriptRequestQueryService transcriptRequestQueryService) {
		this.transcriptRequestService = transcriptRequestService;
		this.transcriptRequestQueryService = transcriptRequestQueryService;
	}

	/**
	 * POST /requests : Create a new transcriptRequest.
	 *
	 * @param transcriptRequestDTO the transcriptRequestDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptRequestDTO, or with status 400 (Bad Request) if the
	 *         transcriptRequest has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws NotFoundException
	 * @throws IOException
	 */
	@PostMapping("/request")
	@Timed
	public ResponseEntity<TranscriptRequestDTO> createTranscriptRequest(
			@Valid @RequestBody TranscriptRequestDTO transcriptRequestDTO)
			throws URISyntaxException, IOException, NotFoundException {
		log.debug("REST request to save TranscriptRequest : {}", transcriptRequestDTO);
		if (transcriptRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptRequest cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		TranscriptRequestDTO result = transcriptRequestService.saveRequest(transcriptRequestDTO);
		return ResponseEntity.created(new URI("/api/request/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * GET /requests : get all the transcriptRequest.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         transcriptRequest in body
	 */
	@GetMapping("/request")
	@Timed
	public ResponseEntity<List<TranscriptRequestDTO>> getAllTranscriptRequest(TranscriptRequestCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get TranscriptRequest by criteria: {}", criteria);
		Page<TranscriptRequestDTO> page = transcriptRequestQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/request");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	@GetMapping("/institutions/{id}/credentialrequests")
	@Timed
	public ResponseEntity<CredentialRequests> getCredentialRequestsByInstitutionId(@PathVariable Long id,
			@RequestParam(value = "studentSearchTerm", required = false) String studentSearchTerm,
			@RequestParam(value = "requestedFromInstitutionId", required = false) Long requestedFromInstitutionId,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		log.debug(
				"REST request to search institution requestedFromInstitutionId(id = {},searchTerm = {},startDate = {},endDate = {})",
				id, studentSearchTerm, startDate, endDate);

		if (!authorizationService.ownedByInstitution(id)) {
			throw new AccessDeniedException("not the owner");
		}
		if(requestedFromInstitutionId!=null && !authorizationService.ownedByInstitution(requestedFromInstitutionId)) {
			throw new AccessDeniedException("not the owner");
		}

		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		CredentialRequests response = transcriptRequestService.getCredentialRequests(id, studentSearchTerm,
				requestedFromInstitutionId, start, end);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
