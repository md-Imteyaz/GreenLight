package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.GlJobApplicant;
import com.gl.platform.service.GlJobAplicantService;
import com.gl.platform.service.dto.ApplicantCredentailsRequestDTO;
import com.gl.platform.service.dto.GlJobApplicantCustomDTO;
import com.gl.platform.service.dto.GlJobApplicantDTO;
import com.gl.platform.service.dto.GlJobApplicantInterviewDetailsDTO;
import com.gl.platform.service.dto.GlJobInterviewSchedulesDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

/**
 * REST controller for managing GlJobAplicant.
 */
@RestController
@RequestMapping("/api")

public class GlJobApplicantResource {
	private final Logger log = LoggerFactory.getLogger(GlJobApplicantResource.class);

	private final GlJobAplicantService glJobApplicantService;

	public GlJobApplicantResource(GlJobAplicantService glJobApplicantService) {
		this.glJobApplicantService = glJobApplicantService;

	}

	private static final String ENTITY_NAME = GlJobApplicant.class.getSimpleName().toLowerCase();

	/**
	 * POST /apply-job apply the job
	 * 
	 * @param GlJobApplicationDTO the GlJobApplicationDTO to create
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@PostMapping("/apply-job")
	@Timed
	public ResponseEntity<?> applyJob(@Valid @RequestBody GlJobApplicantDTO glJobApplicantDTO)
			throws URISyntaxException {
		log.debug("REST request to apply Job : {}", glJobApplicantDTO);
		if (glJobApplicantDTO.getId() != null) {
			throw new BadRequestAlertException("A new applicant cannot already have an ID", ENTITY_NAME, "idexists");
		}
		GlJobApplicantDTO result = glJobApplicantService.saveJobApplicant(glJobApplicantDTO);
		log.debug("JobApplicant data object : {} ", result);
		return ResponseEntity.created(new URI("/api/apply-job" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PutMapping("/apply-job")
	@Timed
	public ResponseEntity<?> updateApplyJob(@Valid @RequestBody GlJobApplicantDTO glJobApplicantDTO)
			throws URISyntaxException {
		log.debug("REST request update apply Job : {}", glJobApplicantDTO);
		if (glJobApplicantDTO.getId() == null) {
			throw new BadRequestAlertException("A new applicant must have an ID", ENTITY_NAME, "idMustNotNull");
		}
		GlJobApplicantDTO result = glJobApplicantService.save(glJobApplicantDTO);
		log.debug("JobApplicant data object : {} ", result);
		return ResponseEntity.created(new URI("/api/apply-job" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * GET /jobs/applicant/view/{id}: get data by applicant.
	 *
	 * @param id :id the id of the applicant
	 * @return the ResponseEntity with status 200 (OK) with response body
	 * @throws JSONException
	 * 
	 */
	@GetMapping("/jobs/applicant/view/{id}")
	@Timed
	public ResponseEntity<?> getAllJobsByApplicantBy(@PathVariable Long id) throws JSONException {
		log.debug("REST request to get job applicant by id : {}", id);

		@SuppressWarnings("unchecked")
		Map<String, Object> response = (Map<String, Object>) glJobApplicantService.applicantViewData(id);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * PUT /applicant/status update the applicant status
	 * 
	 * @param GlJobApplicationDTO the GlJobApplicationDTO to update(contains only id
	 *                            and status)
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@PutMapping("/applicant/status")
	@Timed
	public ResponseEntity<?> updateApplicantStatus(@Valid @RequestBody GlJobApplicantDTO glJobApplicantDTO)
			throws URISyntaxException {
		log.debug("REST request to apply Job : {}", glJobApplicantDTO);
		if (glJobApplicantDTO.getId() == null) {
			throw new BadRequestAlertException("A applicant must have an ID", ENTITY_NAME, "idnull");
		}
		GlJobApplicantDTO response = glJobApplicantService.updateJobApplicantStatus(glJobApplicantDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * PUT /applicant/share-credentials update the applicant credentials after Job
	 * applied
	 * 
	 * @param GlJobApplicationDTO the GlJobApplicationDTO to update(contains id, job
	 *                            id and List of Credentials)
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@PutMapping("/applicant/share-credentials")
	@Timed
	public ResponseEntity<?> updateApplicantShares(@Valid @RequestBody GlJobApplicantCustomDTO glJobApplicantDTO)
			throws URISyntaxException {
		log.debug("REST request to update Job with Credentials : {}", glJobApplicantDTO);
		GlJobApplicantDTO response = glJobApplicantService.updateJobApplicantCredentials(glJobApplicantDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * GET /applicant/credentials/{userId}: get credentials data by userId.
	 *
	 * @param userId :id the id of the glUser
	 * @return the ResponseEntity with status 200 (OK) with response body
	 * 
	 */
	@GetMapping("/applicant/credentials/{userId}")
	@Timed
	public ResponseEntity<?> getCredentialsForShare(@PathVariable Long userId) {

		log.debug("REST request to get user credentials data by userId : {}", userId);

		List<Object> response = glJobApplicantService.getUserCredentials(userId);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/applicant/credentials/{jobId}/{userId}")
	@Timed
	public ResponseEntity<?> getAppliedCredentialsByApplicantId(@PathVariable Long jobId, @PathVariable Long userId) {

		log.debug("REST request to get user applied credentials data by jobId : {} and userId", jobId, userId);

		Optional<GlJobApplicantDTO> response = glJobApplicantService.getUserAppliedCredentials(jobId, userId);

		return ResponseUtil.wrapOrNotFound(response);
	}

	@PostMapping("/applicants/credentials/download")
	@Timed
	public ResponseEntity<Map<String, Object>> getDownloadLink(
			@RequestBody ApplicantCredentailsRequestDTO applicantCustomDTO)
			throws BadRequestException, JSONException, IOException {
		log.info("REST request to download the job applicants credentials with job id {}",
				applicantCustomDTO.getJobId());
		Map<String, Object> response = glJobApplicantService.getCredentails(applicantCustomDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/applicants/credentials/download/{jobId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getAllDownloadLink(@PathVariable Long jobId)
			throws BadRequestException, JSONException, IOException {
		log.info("REST request to download the job applicants credentials with job id {}", jobId);
		Map<String, Object> response = glJobApplicantService.getAllCredentialsDownload(jobId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/calculate/job/matches/{userId}")
	@Timed
	public ResponseEntity<List<Object>> getJobMatchesForNewlyRegisteredUser(@PathVariable Long userId) {
		log.debug("REST request to get matches for newly registered user");

		glJobApplicantService.calcualteMatches(userId);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/job/affliation/matches/{userId}")
	@Timed
	public ResponseEntity<List<Object>> getAffliationJobMatchesForNewlyRegisteredUser(@PathVariable Long userId) {
		log.debug("REST request to get Afflication matches for newly registered user");

		glJobApplicantService.extendOfferForNewlyRegistredStudentWithAffliation(userId);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/student/opportunities")
	@Timed
	public ResponseEntity<?> getStudentOpportunities() {

		log.debug("REST request to get logged in student's opportunities counts");

		Map<String, Object> response = glJobApplicantService.getStudentCounts();

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@PostMapping("/applicant/interview/details")
	@Timed
	public ResponseEntity<?> saveInterviewDetails(@Valid @RequestBody GlJobApplicantInterviewDetailsDTO glJobApplicantDTO)
			throws URISyntaxException, NotFoundException {
		log.debug("REST request to post GlJobApplicantInterviewDetailsDTO : {}", glJobApplicantDTO);
		if (glJobApplicantDTO.getId() != null) {
			throw new BadRequestAlertException("request cannot already have an ID", ENTITY_NAME, "idexists");
		}
		GlJobApplicantInterviewDetailsDTO result = glJobApplicantService.saveJobApplicantInterviewDetails(glJobApplicantDTO,false);
		log.debug("JobApplicant data object : {} ", result);
		return ResponseEntity.created(new URI("/api/apply-job" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
	
	@PutMapping("/applicant/interview/details")
	@Timed
	public ResponseEntity<?> updateInterviewDetails(@Valid @RequestBody GlJobApplicantInterviewDetailsDTO glJobApplicantDTO)
			throws URISyntaxException, NotFoundException {
		log.debug("REST request to post GlJobApplicantInterviewDetailsDTO : {}", glJobApplicantDTO);
		if (glJobApplicantDTO.getId() == null) {
			throw new BadRequestAlertException("update msut have an ID", ENTITY_NAME, "idnotexists");
		}
		GlJobApplicantInterviewDetailsDTO result = glJobApplicantService.saveJobApplicantInterviewDetails(glJobApplicantDTO,true);
		log.debug("JobApplicant data object : {} ", result);
		return ResponseEntity.created(new URI("/api/apply-job" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
	@GetMapping("/applicant/interview/details/{jobId}/{userId}")
	@Timed
	public ResponseEntity<?> getInterviewDetails(@PathVariable Long jobId,@PathVariable Long userId)
			throws NotFoundException {
		log.debug("REST request to get GlJobApplicantInterviewDetails by jobid: {} and userId: {}  ", jobId,userId);
		if (jobId == null || userId ==null) {
			throw new BadRequestAlertException("jobId and userId must not be null","GlJobApplicantInterviewDetails","idNull");
		}
		GlJobApplicantInterviewDetailsDTO response = glJobApplicantService.getJobInterviewAvailableSlots(jobId, userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	
	@GetMapping("/applicant/interview/details/{applicantId}")
	@Timed
	public ResponseEntity<?> getApplicantInterviewDetails(@PathVariable Long applicantId)
			throws NotFoundException {
		log.debug("REST request to get GlJobApplicantInterviewDetails by applicantId: {} ", applicantId);
		if (applicantId == null ) {
			throw new BadRequestAlertException("applicantId must not be null","GlJobApplicantInterviewDetails","idNull");
		}
		GlJobApplicantInterviewDetailsDTO response = glJobApplicantService.getApplicantInterviewDetails(applicantId);
		return new ResponseEntity<>(response, HttpStatus.OK);	
	}
	
	@GetMapping("/applicant/interview/requests/{userId}")
	@Timed
	public ResponseEntity<?> getApplicantInterviewDetailsForGlUser(@PathVariable Long userId)
			throws NotFoundException {
		log.debug("REST request to get GlJobApplicantInterviewDetails by userId: {} ", userId);
		if (userId == null ) {
			throw new BadRequestAlertException("userId must not be null","GlJobApplicantInterviewDetails","idNull");
		}
		List<Map<String,Object>> response = glJobApplicantService.getApplicantInterviewDetailsByUserId(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);	
	}
}
