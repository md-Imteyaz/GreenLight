package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.EdiUploadHistory;
import com.gl.platform.domain.FourYearTranscriptCourse;
import com.gl.platform.security.AppUser;
import com.gl.platform.security.SecurityUtils;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.FourYearDataloadService;
import com.gl.platform.service.FourYearEDIParsingService;
import com.gl.platform.service.FourYearTranscriptService;
import com.gl.platform.service.HighSchoolTranscriptService;
import com.gl.platform.service.dto.CredentialDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class FourYearTranscriptResource {

	private final Logger log = LoggerFactory.getLogger(FourYearTranscriptResource.class);

	private static final String ENTITY_NAME = "foutYearTranscript";

	@Autowired
	private FourYearTranscriptService transcriptService;
	
	@Autowired
	private FourYearEDIParsingService fourYearEDIParsingService;

	@Autowired
	private HighSchoolTranscriptService highSchoolTranscriptService;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private FourYearDataloadService fyDataloadService;

	@GetMapping("/generate/{id}")
	@Timed
	public @ResponseBody ResponseEntity<?> generateCredential(@PathVariable Long id)
			throws IOException, NotFoundException, BadRequestException {
		log.debug("REST request to generate credential");
		CredentialDTO response = transcriptService.generateCredentialWithId(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/credentials")
	@Timed
	public ResponseEntity<?> getStudentCredentials() throws NotFoundException {
		log.debug("REST request to get highschool transcripts by logged in user");
		List<Map<String, Object>> transcripts = highSchoolTranscriptService.getStudentTranscripts();
		return ResponseEntity.ok(transcripts);
	}

	@GetMapping("/credential/{credentialId}")
	@Timed
	public ResponseEntity<?> getCredentialView(@PathVariable Long credentialId) {
		log.debug("REST request to get credential by parent with credentialId {}", credentialId);
		Optional<CredentialDTO> cred = highSchoolTranscriptService.getTranscriptDetailsByCredId(credentialId);
		return ResponseUtil.wrapOrNotFound(cred);
	}

	@GetMapping("/edi/uploadhistory/{userId}")
	public ResponseEntity<?> getEDIUploadHistory(@PathVariable Long userId) throws BadRequestException {

		log.debug("REST request to get EDI upload history {}", userId);
		List<EdiUploadHistory> result = transcriptService.getRecentUploadHistory(userId);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/fouryear/majors")
	public ResponseEntity<?> getAllMajorsList() throws BadRequestException {

		log.debug("REST request to get four year majors list");
		List<String> result = transcriptService.getAllMajors();
		return ResponseEntity.ok(result);
	}

	@PostMapping("/upload/fouryear/transcripts")
	@Timed
	public @ResponseBody ResponseEntity<?> uploadTranscripts(@RequestParam("studentEmailAddress") String email,
			@RequestParam("universityId") Long universityId, @RequestParam("files") MultipartFile[] files,
			@RequestParam(value = "sendEnrollmentEmail", required = false) Boolean sendEnrollmentEmail)
			throws Exception {
		log.debug("Came to Transcript Upload Method");
		if (files == null || email == null || universityId == null) {
			throw new BadRequestAlertException("Transcrips files missing", ENTITY_NAME, "idnull");
		}
		if (!authorizationService.ownedByInstitution(universityId)) {
			throw new AccessDeniedException("not the owner");
		}

		log.debug("REST request to save Transcripts ");
		log.info("Files Came through : {}", files.length);
		Optional<AppUser> createdUser = SecurityUtils.getCurrentUser();

		Long createdUserId = null;
		if (createdUser.isPresent()) {
			createdUserId = createdUser.get().getUserId();
		}

		String result = IOUtils.toString(files[0].getInputStream(), StandardCharsets.UTF_8);
		Map<String, Object> response = fourYearEDIParsingService.uploadTranscript(result, email, universityId,
				sendEnrollmentEmail, createdUserId);

		return ResponseEntity.created(new URI("/api/upload/transcripts/")).body(response);

	}

	@GetMapping("/fouryear/courses/{credentialId}")
	public ResponseEntity<?> getCourses(@PathVariable Long credentialId) throws NotFoundException {

		log.debug("REST request to get four year courses for credential id : {}", credentialId);

		if (credentialId == null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}
		List<FourYearTranscriptCourse> response = transcriptService.getFourYearCourses(credentialId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/load/fouryear/data/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Map<String, Object>> loadFourYearData(@PathVariable Long institutionId)
			throws NotFoundException, IOException, URISyntaxException, ParseException {
		log.debug("Came to load csv data method");
		if (institutionId == null) {
			throw new BadRequestAlertException("Transcrips files missing", ENTITY_NAME, "idnull");
		}
//		if (!authorizationService.ownedByInstitution(universityId)) {
//			throw new AccessDeniedException("not the owner");
//		}

		log.debug("REST request to load all csv files");
		fyDataloadService.loadFourYearData(institutionId);
		Map<String, Object> response = new HashMap<>();
		response.put("message", "All csv files loaded successfully");

		return ResponseEntity.created(new URI("/api/load/fouryear/data/")).body(response);

	}
}
