package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.ResumeUpload;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.ResumeUploadService;
import com.gl.platform.service.dto.ResumeUploadDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class ResumeUploadResource {

	private final Logger log = LoggerFactory.getLogger(ResumeUploadResource.class);

	private static final String ENTITY_NAME = "resumeUpload";

	@Autowired
	private ResumeUploadService resumeService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/upload/resume")
	@Timed
	public @ResponseBody ResponseEntity<ResumeUploadDTO> uploadResume(@RequestParam("name") String name,
			@RequestParam("userId") Long userId, @RequestParam(value = "message", required = false) String message,
			@RequestParam("files") MultipartFile file) throws URISyntaxException, IOException {

		log.debug("Rest request to upload resume : {}", file);

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (file == null || name == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}
		log.debug("REST request to save Transcripts ");

		ResumeUploadDTO response = resumeService.uploadResume(file, name, userId, message, null);

		return ResponseEntity.created(new URI("/api/upload/resume/")).body(response);

	}

	@PostMapping("/upload/resume/update")
	@Timed
	public @ResponseBody ResponseEntity<ResumeUploadDTO> updateResume(@RequestParam("userId") Long userId,
			@RequestParam("name") String name, @RequestParam(value = "message", required = false) String message,
			@RequestParam("files") MultipartFile file, @RequestParam("id") Long id) throws IOException {
		log.debug("Rest request to upload resume");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (id == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}

		if (file == null || name == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "idnull");
		}
		log.debug("REST request to save Transcripts ");

		ResumeUploadDTO response = resumeService.uploadResume(file, name, userId, message, id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{userId}/resume")
	@Timed
	public ResponseEntity<List<ResumeUploadDTO>> getSudetnType(@PathVariable Long userId) throws NotFoundException {
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}
		List<ResumeUploadDTO> response = resumeService.getUserResume(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/resume/{id}")
	@Timed
	public ResponseEntity<ResumeUpload> deleteStudentResume(@PathVariable Long id) throws NotFoundException {

		resumeService.deleteResume(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
