package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gl.platform.service.S3StorageService;
import com.gl.platform.service.TranscriptDataLoadService;
import com.gl.platform.service.TranscriptsQueryService;
import com.gl.platform.service.TranscriptsService;
import com.gl.platform.service.UserActivityService;
import com.gl.platform.service.dto.NamedInputStreamWrapper;
import com.gl.platform.service.dto.TranscriptDTO;
import com.gl.platform.service.dto.TranscriptGeneration;
import com.gl.platform.service.dto.TranscriptsCriteria;
import com.gl.platform.service.dto.TranscriptsDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

/**
 * REST controller for managing Transcripts.
 */
@RestController
@RequestMapping("/api")
public class TranscriptsResource {

	private final Logger log = LoggerFactory.getLogger(TranscriptsResource.class);

	private static final String ENTITY_NAME = "transcripts";

	@Autowired
	private UserActivityService userActivityService;
	
	@Autowired
	private TranscriptDataLoadService 	transcriptDataLoadService;
	

	
	private final TranscriptsService transcriptsService;

	private final TranscriptsQueryService transcriptsQueryService;

	private final S3StorageService s3StorageService;

	public TranscriptsResource(TranscriptsService transcriptsService, TranscriptsQueryService transcriptsQueryService,
			S3StorageService s3StorageService) {
		this.transcriptsService = transcriptsService;
		this.transcriptsQueryService = transcriptsQueryService;
		this.s3StorageService = s3StorageService;
	}

	/**
	 * POST /transcripts : Create a new transcripts.
	 *
	 * @param transcriptsDTO the transcriptsDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/transcripts")
	@Timed
	public ResponseEntity<TranscriptDTO> createTranscripts(@Valid @RequestBody TranscriptDTO transcriptsDTO)
			throws URISyntaxException {
		log.debug("REST request to save Transcripts : {}", transcriptsDTO);
		if (transcriptsDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcripts cannot already have an ID", ENTITY_NAME, "idexists");
		}
		TranscriptDTO result = transcriptsService.save(transcriptsDTO);
		return ResponseEntity.created(new URI("/api/transcripts/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * POST /upload/transcripts : Create a new transcripts via an EDI file upload
	 *
	 * @param files
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws Exception 
	 */
	@PostMapping("/upload/transcripts")
	@Timed
	public @ResponseBody ResponseEntity<?> uploadTranscripts(
			@RequestParam("studentEmailAddress") String email, @RequestParam("universityId") Long universityId,
			@RequestParam("files") MultipartFile[] files,
			@RequestParam(value = "sendEnrollmentEmail", required = false) Boolean sendEnrollmentEmail)
			throws Exception {
		log.debug("Came to Transcript Upload Method");
		if (files == null || email == null || universityId == null) {
			throw new BadRequestAlertException("Transcrips files missing", ENTITY_NAME, "idnull");
		}
		log.debug("REST request to save Transcripts ");
		log.info("Files Came through : {}", files.length);

		Map<String,Object> response = transcriptsService.uploadTranscript(files, email, universityId,
				sendEnrollmentEmail);

		return ResponseEntity.created(new URI("/api/upload/transcripts/")).body(response);

	}

	/**
	 * PUT /transcripts : Updates an existing transcripts.
	 *
	 * @param transcriptsDTO the transcriptsDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         transcriptsDTO, or with status 400 (Bad Request) if the
	 *         transcriptsDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the transcriptsDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/transcripts")
	@Timed
	public ResponseEntity<TranscriptDTO> updateTranscripts(@Valid @RequestBody TranscriptDTO transcriptsDTO)
			throws URISyntaxException {
		log.debug("REST request to update Transcripts : {}", transcriptsDTO);
		if (transcriptsDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		TranscriptDTO result = transcriptsService.save(transcriptsDTO);
		userActivityService.audit("Transcript", "Updated Transcript");
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, transcriptsDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /transcripts : get all the transcripts.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of transcripts
	 *         in body
	 */
	@GetMapping("/transcripts")
	@Timed
	public ResponseEntity<List<TranscriptsDTO>> getAllTranscripts(TranscriptsCriteria criteria, Pageable pageable) {
		log.debug("REST request to get Transcripts by criteria: {}", criteria);
		Page<TranscriptsDTO> page = transcriptsQueryService.findByCriteria(criteria, pageable);

		List<TranscriptsDTO> transcripts = page.getContent();

		for (TranscriptsDTO transcript : transcripts) {
			String s3Path = "transcripts/" + transcript.getId() + "/pdf_transcript";
			transcript.setUrl(s3StorageService.generatePresignedUrl(s3Path));
		}

		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transcripts");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /transcripts/:id : get the "id" transcripts.
	 *
	 * @param id the id of the transcriptsDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         transcriptsDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/transcripts/{id}")
	@Timed
	public ResponseEntity<TranscriptDTO> getTranscripts(@PathVariable Long id) {
		log.debug("REST request to get Transcripts : {}", id);
		Optional<TranscriptDTO> transcriptsDTO = transcriptsService.findOne(id);
		if (transcriptsDTO.isPresent()) {
			TranscriptDTO transcript = transcriptsDTO.get();
			String s3Path = "transcripts/" + transcript.getId() + "/pdf_transcript";
			String s3ViewPath = "transcripts/" + transcript.getId() + "/pdf_transcript_view";

			transcript.setUrl(s3StorageService.generatePresignedUrl(s3Path));
			transcript.setViewUrl(s3StorageService.generatePresignedUrl(s3ViewPath));
		}

		return ResponseUtil.wrapOrNotFound(transcriptsDTO);
	}
	
	

	@GetMapping("/transcripts/{id}/view")
	@Timed
	public void getViewerableTranscript(HttpServletResponse response, @PathVariable Long id) throws IOException {
		log.debug("REST request to view transcript");
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "inline; filename=\"transcript_pdf_view\"");

		try (NamedInputStreamWrapper nisw = s3StorageService.retrieve("transcripts/" + id + "/pdf_transcript_view")) {
			org.apache.commons.io.IOUtils.copy(nisw, response.getOutputStream());
			response.flushBuffer();
		}
	}
	
	@GetMapping("/transcripts/{id}/download")
	@Timed
	public void getDownloadableTranscript(HttpServletResponse response, @PathVariable Long id) throws IOException {
		
		log.debug("REST request to download transcripts");
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"transcript_pdf\"");

		try (NamedInputStreamWrapper nisw = s3StorageService
				.retrieve("transcripts/" + id + "/pdf_transcript_student")) {
			org.apache.commons.io.IOUtils.copy(nisw, response.getOutputStream());
			response.flushBuffer();
		}

	}

	/**
	 * DELETE /transcripts/:id : delete the "id" transcripts.
	 *
	 * @param id the id of the transcriptsDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/transcripts/{id}")
	@Timed
	public ResponseEntity<Void> deleteTranscripts(@PathVariable Long id) {
		log.debug("REST request to delete Transcripts : {}", id);
		transcriptsService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	/**
	 * POST /refreshblockchainhashes : Validate transcript pdf file upload
	 *
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@PostMapping("/refreshblockchainhashes")
	@Timed
	public @ResponseBody ResponseEntity<Void> refreshBlockchainHash() throws URISyntaxException, 
	JsonParseException, JsonMappingException, IOException {
	
		transcriptsService.refreshBlockChainHash();
		
		return ResponseEntity.ok().build();
		
	}
	
	@GetMapping("/generate/{insId}/{id}")
    @Timed
    public ResponseEntity<TranscriptDTO> generateTranscripts(@PathVariable Long insId,@PathVariable Long id) throws Exception {
        log.debug("REST request to get Transcripts : {}", id);
        Optional<TranscriptDTO> transcriptsDTO = transcriptsService.findOne(id);

        if (transcriptsDTO.isPresent()) {
            transcriptsService.generateTranscript(insId, id);
        }
        return ResponseUtil.wrapOrNotFound(transcriptsDTO);
    }
	 
	 
	 /**
	 * POST /upload/transcripts : Create a new transcripts via an EDI file upload
	 *
	 * @param files
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws Exception 
	 */
	@PostMapping("/upload/dcccdtranscripts")
	@Timed
	public @ResponseBody ResponseEntity<Void>  saveTranscript(@Valid @RequestBody TranscriptGeneration transcriptGeneration)
	throws Exception {
		log.debug("DCCCD Transcript, Person and Hold Update");
		transcriptsService.saveTranscript(transcriptGeneration);
	
		return ResponseEntity.ok().build();

	}
	
	
	/**
	 * POST /upload/transcripts : Create a new transcripts via an EDI file upload
	 *
	 * @param files
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws Exception 
	 */
	@PostMapping("/upload/dcccdholds")
	@Timed
	public @ResponseBody ResponseEntity<Void>  processHold(@Valid @RequestBody TranscriptGeneration transcriptGeneration)
	throws Exception {
		
		log.debug("DCCCD Hold Update");
		
		transcriptDataLoadService.processTranscriptHold(transcriptGeneration);
	
		return ResponseEntity.ok().build();

	}

	
	 
	 /**
	 * POST /upload/transcripts : Create a new transcripts via an EDI file upload
	 *
	 * @param files
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws Exception 
	 */
	@PostMapping("/upload/dcccdstudent")
	@Timed
	public @ResponseBody ResponseEntity<Void>  saveStudent(@Valid @RequestBody TranscriptGeneration transcriptGeneration)
	throws Exception {
		
		Long start = System.currentTimeMillis();

		System.out.println("DCCCD Student Data");
		
		transcriptsService.saveStudent(transcriptGeneration);
	
		Long end = System.currentTimeMillis();
		
		System.out.println("DCCCD Student Data done " + (end - start));
		
		return ResponseEntity.ok().build();

	}
	
	
	@GetMapping("/generate/edi/{id}")
    @Timed
    public ResponseEntity<TranscriptDTO> generateEDI(@PathVariable Long id) throws IOException, NotFoundException {
        log.debug("REST request to get Transcripts : {}", id);
        Optional<TranscriptDTO> transcriptsDTO = transcriptsService.findOne(id);

        if (transcriptsDTO.isPresent()) {
            transcriptsService.generateEDIFromTranscript( id);
        }
        return ResponseUtil.wrapOrNotFound(transcriptsDTO);
    }
}
