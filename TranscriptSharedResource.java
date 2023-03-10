package com.gl.platform.web.rest;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.TranscriptShared;
import com.gl.platform.service.TranscriptSharedService;
import com.gl.platform.service.dto.TranscriptSharedRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.TranscriptNotAvailableException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class TranscriptSharedResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptSharedResource.class);

	private static final String ENTITY_NAME = TranscriptShared.class.getSimpleName().toLowerCase();
	
	@Autowired
	private TranscriptSharedService transcriptSharedService;
	
	

	/**
	 * POST /TranscriptShared : Create a new expandedCredentialShared.
	 *
	 * @param transcriptSharedDTO the transcriptSharedDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         badgeSharedDTO, or with status 400 (Bad Request) if the badgeShared has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws TranscriptNotAvailableException 
	 * @throws BadRequestException 
	 */
	@PostMapping("/transcript-shared")
	@Timed
	public ResponseEntity<TranscriptSharedRequestDTO> createTranscriptShared(@Valid @RequestBody TranscriptSharedRequestDTO transcriptSharedRequestDTO) throws URISyntaxException, IOException, NotFoundException, TranscriptNotAvailableException, BadRequestException {
		log.debug("REST request to save TranscriptShared : {}", transcriptSharedRequestDTO);
		if (transcriptSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptShared cannot already have an ID", ENTITY_NAME, "idexists");
		}
		TranscriptSharedRequestDTO result = transcriptSharedService.save(transcriptSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/transcript-shared/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
}
