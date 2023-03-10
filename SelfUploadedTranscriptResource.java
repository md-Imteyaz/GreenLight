package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.SelfUploadedTranscriptService;
import com.gl.platform.service.dto.CertificateUploadDTO;
import com.gl.platform.service.dto.ResumeUploadDTO;
import com.gl.platform.service.dto.SelfUploadedTranscriptDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class SelfUploadedTranscriptResource {

	private final Logger log = LoggerFactory.getLogger(SelfUploadedTranscriptResource.class);

	private static final String ENTITY_NAME = SelfUploadedTranscriptResource.class.getSimpleName().toLowerCase();

	@Autowired
	private SelfUploadedTranscriptService selfUploadedService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/upload/transcript")
	public @ResponseBody ResponseEntity<SelfUploadedTranscriptDTO> createSelfUploadTrancript(
			@RequestParam("transcriptType") String transcriptType,
			@RequestParam("institutionName") String institutionName,
			@RequestParam(value = "fieldOfStudy", required = false) String fieldOfStudy,
			@RequestParam(value = "major", required = false) String major,
			@RequestParam("userId") Long userId,
			@RequestParam(value = "id", required = false) Long id,
			@RequestParam("files") MultipartFile file) throws URISyntaxException {

		log.debug("REST request to save the self uploaded transcript : ",userId);
		if (id != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (file == null || transcriptType == null || institutionName == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}

		SelfUploadedTranscriptDTO response = selfUploadedService.uploadTranscript(transcriptType, institutionName, fieldOfStudy, major, userId, file);

		return ResponseEntity.created(new URI("/api/upload/transcript/")).body(response);
	}
	
	@GetMapping("/{userId}/transcript")
	@Timed
	public ResponseEntity<List<SelfUploadedTranscriptDTO>> getStudentTranscript(@PathVariable Long userId) throws NotFoundException {
		
		log.debug("REST request to get the transcripts for user Id : {}",userId);
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}
		List<SelfUploadedTranscriptDTO> response = selfUploadedService.getUserTranscript(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
