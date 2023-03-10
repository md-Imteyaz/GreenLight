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
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.OtherCredentialsService;
import com.gl.platform.service.dto.OtherCredentialsDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class OtherCredentialsResource {

	private final Logger log = LoggerFactory.getLogger(OtherCredentialsResource.class);

	private static final String ENTITY_NAME = "otherCredentialUpload";

	@Autowired
	private OtherCredentialsService otherCredService;

	@Autowired
	private AuthorizationService authorizationService;

	private static final String OWNER = "not the owner";

	@PostMapping("/upload/other/credential")
	@Timed
	public @ResponseBody ResponseEntity<OtherCredentialsDTO> uploadCredential(
			@RequestParam("documentName") String documentName, @RequestParam("documentType") String documentType,
			@RequestParam("userId") Long userId, @RequestParam(value = "message", required = false) String message,
			@RequestParam(value = "covidReport", required = false) String covidReport,
			@RequestParam(value = "tsiScore", required = false) String tsiScoreValue,
			@RequestParam("files") MultipartFile file) throws URISyntaxException, NotFoundException, IOException {

		log.debug("REST request to upload credential");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException(OWNER);
		}

		if (file == null || documentName == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}
		log.debug("REST request to save Transcripts ");
		Long tsiScore;
		if (!"null".equalsIgnoreCase(tsiScoreValue) && tsiScoreValue != null) {
			tsiScore = Long.parseLong(tsiScoreValue);
		} else {
			tsiScore = null;
		}
		OtherCredentialsDTO response = otherCredService.uploadOtherCredentials(file, documentName, documentType, userId,
				message, tsiScore, null, covidReport);

		return ResponseEntity.created(new URI("/upload/other/credential")).body(response);
	}

	@PostMapping("/update/other/credential")
	@Timed
	public @ResponseBody ResponseEntity<OtherCredentialsDTO> updateCredential(
			@RequestParam("documentName") String documentName, @RequestParam("documentType") String documentType,
			@RequestParam("userId") Long userId, @RequestParam(value = "message", required = false) String message,
			@RequestParam(value = "covidReport", required = false) String covidReport,
			@RequestParam(value = "tsiScore", required = false) Long tsiScoreValue,
			@RequestParam("files") MultipartFile file, @RequestParam("id") Long id)
			throws NotFoundException, IOException {
		log.debug("REST request to update credential");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException(OWNER);
		}

		if (id == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}

		if (file == null || documentName == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "idnull");
		}
		OtherCredentialsDTO response = otherCredService.uploadOtherCredentials(file, documentName, documentType, userId,
				message, tsiScoreValue, id, covidReport);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/other/credential/{userId}")
	@Timed
	public ResponseEntity<List<OtherCredentialsDTO>> getSudetnType(@PathVariable Long userId) throws IOException {
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException(OWNER);
		}
		log.info("RESt request to get the list of credentials with user id : {}", userId);
		List<OtherCredentialsDTO> response = otherCredService.getOtherDocumentsList(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/other/credential/{id}")
	@Timed
	public ResponseEntity<OtherCredentialsDTO> deleteStudentResume(@PathVariable Long id) throws NotFoundException {
		otherCredService.deleteOtherCredential(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
