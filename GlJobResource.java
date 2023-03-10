package com.gl.platform.web.rest;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.GlJob;
import com.gl.platform.domain.InstitutionGroup;
import com.gl.platform.domain.JobGroupMapping;
import com.gl.platform.repository.InstitutionGroupRepository;
import com.gl.platform.repository.JobGroupMappingRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlJobMatchService;
import com.gl.platform.service.GlJobService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.UserActivityService;
import com.gl.platform.service.dto.GlJobApplicantDTO;
import com.gl.platform.service.dto.GlJobCriteria;
import com.gl.platform.service.dto.GlJobDTO;
import com.gl.platform.service.dto.GlJobFilterDTO;
import com.gl.platform.service.dto.GlJobMatchDTO;
import com.gl.platform.service.dto.GlJobMatchResponseDTO;
import com.gl.platform.service.dto.GlJobQualifiedCandidateDTO;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.StudentJobsFilterRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import javassist.NotFoundException;

/**
 * REST controller for managing GlJob.
 */
@RestController
@RequestMapping("/api")
public class GlJobResource {

	private final Logger log = LoggerFactory.getLogger(GlJobResource.class);

	private static final String ENTITY_NAME = GlJob.class.getSimpleName().toLowerCase();

	private final GlJobService glJobService;

	private final UserActivityService userActivityService;

	@Autowired
	private GlUserService glUserService;

	@Autowired
	private GlJobMatchService glJobMatchService;

	@Autowired
	private InstitutionGroupRepository instGroupRepository;

	@Autowired
	private JobGroupMappingRepository jobGroupMappingRepository;

	@Autowired
	private AuthorizationService authorizationService;

	public GlJobResource(GlJobService glJobService, UserActivityService userActivityService) {
		this.glJobService = glJobService;
		this.userActivityService = userActivityService;
	}

	@GetMapping("/gl-jobs/view/{id}")
	@Timed
	public ResponseEntity<Map<String, Object>> getJobInfoById(@PathVariable Long id) {
		log.debug("REST Request to get GlJob by id : {}", id);
		Map<String, Object> response = glJobService.getGlJobInfoByJobId(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * GET /gl-jobs/:id : Re Open the glJob by "id" .
	 *
	 * @param id the id of the glJobDTO to re open
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@GetMapping("/jobs/re-open/{id}")
	@Timed
	public ResponseEntity<GlJobDTO> reOpenGlJob(@PathVariable Long id,@RequestParam LocalDate expirationDate) {
		log.debug("REST request to Re Open GlJob : {}", id);
		GlJobDTO glJobDTO = glJobService.jobReOpen(id,expirationDate);
		return new ResponseEntity<>(glJobDTO, HttpStatus.OK);
	}
	
	
	/**
	 * PUT /gl-jobs/:id : Re Open the glJob by "id" .
	 *
	 * @param id the id of the glJobDTO to re open
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@PutMapping("/jobs/re-open/{id}")
	@Timed
	public ResponseEntity<GlJobDTO> closeGlJob(@PathVariable Long id) {
		log.debug("REST request to close GlJob : {}", id);
		GlJobDTO glJobDTO = glJobService.jobReOpen(id,null);
		return new ResponseEntity<>(glJobDTO, HttpStatus.OK);
	}

	/**
	 * POST /jobs : Create a new glJob.
	 *
	 * @param glJobDTO the glJobDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         glJobDTO, or with status 400 (Bad Request) if the glJob has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/group/jobs")
	@Timed
	public ResponseEntity<Map<String, Object>> createGroupPostedJob(@Valid @RequestBody GlJobDTO glJobDTO) {
		log.debug("REST request to save GlJob : {}", glJobDTO);

		if (!authorizationService.ownedByUserOnly(glJobDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (glJobDTO.getId() != null) {
			throw new BadRequestAlertException("A new glJob cannot already have an ID", ENTITY_NAME, "idexists");
		}
		if (glJobDTO.getJobStatus() == null) {
			glJobDTO.setJobStatus("Open");
		}
		glJobDTO.setActive(true);
		if (glJobDTO.getJobPostStartDate() == null) {
			glJobDTO.setJobPostStartDate(LocalDate.now());
		}
		if (glJobDTO.getJobPostStartDate().compareTo(LocalDate.now()) < 1) {
			glJobDTO.setMatchesCalculationInProgress(true);
		}
		GlJobDTO result = glJobService.save(glJobDTO);
		Optional<InstitutionGroup> iGroup = instGroupRepository.findById(glJobDTO.getGroupId());

		if (iGroup.isPresent()) {
			JobGroupMapping mapping = new JobGroupMapping();
			mapping.setActive(true);
			mapping.setCreatedDate(LocalDate.now());
			mapping.setJobId(result.getId());
			mapping.setGroupId(iGroup.get().getId());
			jobGroupMappingRepository.save(mapping);
		}
		if (glJobDTO.getJobPostStartDate().compareTo(LocalDate.now()) < 1) {
			glJobService.saveGroupJobAndGetMatches(result.getId(), glJobDTO.getGroupId());
		} else {
			result.setMatchesCalculationInProgress(false);
			result.setJobStatus("Pending");
			glJobService.save(result);
		}
		userActivityService.audit("Job ", "Created a Job");
		Map<String, Object> map = new HashMap<>();
		return ResponseEntity.ok(map);
	}

	@PostMapping("/nongroup/jobs")
	@Timed
	public ResponseEntity<Map<String, Object>> createNonGroupPostedJob(@Valid @RequestBody GlJobDTO glJobDTO) {
		log.debug("REST request to save GlJob : {}", glJobDTO);

		if (!authorizationService.ownedByUserOnly(glJobDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		if (glJobDTO.getId() != null) {
			throw new BadRequestAlertException("A new glJob cannot already have an ID", ENTITY_NAME, "idexists");
		}
		if (glJobDTO.getJobStatus() == null) {
			glJobDTO.setJobStatus("Open");
		}

		glJobDTO.setActive(true);

		if (glJobDTO.getJobPostStartDate() == null) {
			glJobDTO.setJobPostStartDate(LocalDate.now());
		}

		if (glJobDTO.getJobPostStartDate().compareTo(LocalDate.now()) >= 1) {
			glJobDTO.setJobStatus("Pending");
		} else {
			glJobDTO.setMatchesCalculationInProgress(true);
		}
		GlJobDTO result = glJobService.save(glJobDTO);
		glJobService.saveNonGroupJobAndGetMatches(result);
		Map<String, Object> response = new HashMap<>();
		userActivityService.audit("Job ", "Created a Job");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * GET /jobs : get all the glJobs based on criteria.
	 *
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of glJobs in
	 *         body
	 */
	@GetMapping("/jobs")
	@Timed
	public ResponseEntity<List<Object>> getAllJobsByCriteria(GlJobCriteria criteria) {
		log.debug("REST request to get GlJobs by criteria: {}", criteria);
		List<Object> response = glJobService.getJobsList(criteria);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping("/archive")
	@Timed
	public ResponseEntity<?> archiveJobs(@RequestBody List<Long> idList) {
		log.debug("REST request to archive jobs : {}", idList);
		glJobService.archiveJobs(idList);
		return ResponseEntity.ok().headers(HeaderUtil.createAlert("Successfully archived", null)).build();

	}

	@GetMapping("/jobs/{id}")
	@Timed
	public ResponseEntity<GlJobDTO> getJobById(@PathVariable Long id) {
		log.debug("REST request to get GlJob by id: {}", id);
		GlJobDTO glJobDTO = new GlJobDTO();
		Optional<GlJobDTO> glJob = glJobService.findOne(id);
		if (glJob.isPresent()) {
			glJob.get().setApplicantList(null);
			glJobDTO = glJob.get();
		}
		return new ResponseEntity<>(glJobDTO, HttpStatus.OK);
	}

	@GetMapping("/jobs/student/{id}")
	@Timed
	public ResponseEntity<Map<String, Object>> getjobDataByForStudent(@PathVariable Long id) {
		Map<String, Object> response = glJobService.getStudentJobView(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/matchedjobs")
	@Timed
	public ResponseEntity<Page<GlJobMatchResponseDTO>> getMatchedJobsByCriteria(
			@RequestBody StudentJobsFilterRequestDTO studentJobsFilterRequestDTO, Pageable pageable) {

		log.debug("REST request to get GlJobs by Criteria: {}", studentJobsFilterRequestDTO);
		Page<GlJobMatchResponseDTO> response = glJobMatchService.getMatchedJobsByCriteria(studentJobsFilterRequestDTO,
				pageable);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/matchedjobs/{id}")
	@Timed
	public ResponseEntity<Map<String, Object>> getMatchedJobEntryById(@PathVariable Long id) {

		Optional<GlJobMatchDTO> glJobMatchDTO = glJobMatchService.findOne(id);
		Map<String, Object> data = new HashMap<>();
		if (glJobMatchDTO.isPresent()) {
			data.put("matchedData", glJobMatchDTO.get());
			Optional<GlUserDTO> glUser = glUserService.findByOne(glJobMatchDTO.get().getUserId());
			if (glUser.isPresent()) {
				glUser.get().setJhiPassword(null);
				data.put("userData", glUser.get());
			}
		}
		return new ResponseEntity<>(data, HttpStatus.OK);
	}

	@PutMapping("/matchedjobs")
	@Timed
	public ResponseEntity<List<GlJobMatchDTO>> extendOffer(@Valid @RequestBody GlJobMatchDTO glJobMatchDTO)
			throws NotFoundException {

		log.debug("REST request to update GlJobMatch : {}", glJobMatchDTO);
		if (glJobMatchDTO.getIds() == null || glJobMatchDTO.getIds().isEmpty()) {
			throw new BadRequestAlertException("Invalid ids", ENTITY_NAME, "idsnull");
		}

		List<GlJobMatchDTO> result = glJobMatchService.extendJobOffer(glJobMatchDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/job/keywords")
	@Timed
	public ResponseEntity<Set<String>> getAllStringListForKeywords() {
		Set<String> result = glJobService.getAllStringKeywordsForjobCreation();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/degree/{keywords}")
	@Timed
	public ResponseEntity<Set<String>> getAllDegreeListForKeywords(@PathVariable String keywords) {
		Set<String> result = glJobService.getAllDegreeKeywordsForjobCreation(keywords);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/location/keywords")
	@Timed
	public ResponseEntity<Set<String>> getAllLocationListForKeywords() {
		Set<String> result = glJobService.getAllStringKeywordsForlocation();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/fouryear/awards")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getAll4yrAwards() {
		List<Map<String, Object>> result = glJobService.getAllFourYearDegreesAlonWithFos();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/group/jobs")
	@Timed
	public ResponseEntity<Map<String, Object>> updateGroupPostedJob(@Valid @RequestBody GlJobDTO glJobDTO) {
		log.debug("REST request to update GlJob : {}", glJobDTO);

		if (!authorizationService.ownedByUserOnly(glJobDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (glJobDTO.getId() == null) {
			throw new BadRequestAlertException("ID is mandatory for updating existing job", ENTITY_NAME, "idexists");
		}
		glJobDTO.setActive(true);
		if (("Expired".equals(glJobDTO.getJobStatus()) || "Closed".equals(glJobDTO.getJobStatus()))
				&& glJobDTO.getExpirationDate().compareTo(LocalDate.now()) > 0) {
			glJobDTO.setJobStatus("Open");
		}
		glJobService.save(glJobDTO);
		if (glJobDTO.getJobPostStartDate().compareTo(LocalDate.now()) < 1
				&& "Pending".equalsIgnoreCase(glJobDTO.getJobStatus())) {
			glJobDTO.setMatchesCalculationInProgress(true);
			glJobService.saveGroupJobAndGetMatches(glJobDTO.getId(), glJobDTO.getGroupId());
		} else {
			glJobDTO.setMatchesCalculationInProgress(false);
		}
		userActivityService.audit("Job ", "Updated a Job");
		Map<String, Object> map = new HashMap<>();
		return ResponseEntity.ok(map);
	}

	@PutMapping("/nongroup/jobs")
	@Timed
	public ResponseEntity<Map<String, Object>> updateNonGroupPostedJob(@Valid @RequestBody GlJobDTO glJobDTO) {
		log.debug("REST request to update GlJob : {}", glJobDTO);

		if (!authorizationService.ownedByUserOnly(glJobDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		if (glJobDTO.getId() == null) {
			throw new BadRequestAlertException("ID is mandatory for updating existing job", ENTITY_NAME, "idexists");
		}
		if (glJobDTO.getJobStatus() == null) {
			glJobDTO.setJobStatus("Open");
		}
		glJobDTO.setActive(true);
		// Map<String, Object> response =
		// glJobService.saveNonGroupJobAndGetMatches(glJobDTO);
		userActivityService.audit("Job ", "Updated a Job");
		glJobService.save(glJobDTO);
		Map<String, Object> map = new HashMap<>();
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@GetMapping("/job/filters")
	@Timed
	public ResponseEntity<Page<GlJobQualifiedCandidateDTO>> filterJobsBySkillsAndGpa(@RequestParam Long jobId,
			@RequestParam String skills, @RequestParam String gpa, Pageable page) throws NotFoundException {
		log.debug("REST request to get candidates by filters jobId : {} skills : {} gpa : {}", jobId, skills, gpa);

		Page<GlJobQualifiedCandidateDTO> jobMatches = glJobService.filterApplicantsBy(jobId, skills, gpa, page);
		return new ResponseEntity<>(jobMatches, HttpStatus.OK);
	}

	@GetMapping("/job/matches/{jobId}")
	@Timed
	public ResponseEntity<List<GlJobQualifiedCandidateDTO>> getJobMatches(@PathVariable Long jobId) {
		log.debug("REST request to get qualified candidates by jobId : {}", jobId);
		List<GlJobQualifiedCandidateDTO> jobMatches = glJobService.getJobMatches(jobId);
		return new ResponseEntity<>(jobMatches, HttpStatus.OK);
	}

	@GetMapping("/job/group/matches/{jobId}")
	@Timed
	public ResponseEntity<Page<GlJobQualifiedCandidateDTO>> getJobMatches(@PathVariable Long jobId, Pageable page)
			throws NotFoundException {
		log.debug("REST request to get qualified candidates by jobId : {}", jobId);
		Page<GlJobQualifiedCandidateDTO> jobMatches = glJobService.getJobMatchesForGroupJobs(jobId, page);
		return new ResponseEntity<>(jobMatches, HttpStatus.OK);
	}

	@GetMapping("/job/applied/{jobId}")
	@Timed
	public ResponseEntity<List<GlJobApplicantDTO>> getJobAppliedCandidates(@PathVariable Long jobId)
			throws NotFoundException {
		log.debug("REST request to get applied candidates by jobId : {}", jobId);
		List<GlJobApplicantDTO> jobAppliedCandidates = glJobService.getJobApplicants(jobId);
		return new ResponseEntity<>(jobAppliedCandidates, HttpStatus.OK);
	}

	@GetMapping("/job/group/applied/{jobId}")
	@Timed
	public ResponseEntity<Page<GlJobApplicantDTO>> getJobAppliedCandidatesPage(@PathVariable Long jobId,
			Pageable pageable) throws NotFoundException {
		Page<GlJobApplicantDTO> pages = glJobService.getJobApplicants(jobId, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pages, "/api/job/applied/");
		return new ResponseEntity<>(pages, headers, HttpStatus.OK);
	}

	@PostMapping("/all/jobs")
	public ResponseEntity<Page<GlJobDTO>> getAllJobs(@RequestBody GlJobFilterDTO glJobFilterDTO, Pageable pageable) {
		log.info("Request to get all the jobs using filter : {}", glJobFilterDTO);
		Page<GlJobDTO> response = glJobService.getAllActiveJobs(glJobFilterDTO, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/edit/jobs/salary")
	@Timed
	public ResponseEntity<Object> editSalary() {
		glJobService.editSalary();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/affiliated/employer")
	@Timed
	public ResponseEntity<?> getAffliatedEmployer(@RequestParam String instName) throws NotFoundException {
		log.info("Request to get affliated employer to the education institution: {}", instName);
		Map<String, Object> response = glJobService.getAffliatedEmployer(instName);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/affiliated/institution")
	@Timed
	public ResponseEntity<?> getAffliatedInstitution(@RequestParam Long employerId) throws NotFoundException {
		log.info("Request to get affliated education institution by  employerId: {}", employerId);
		Map<String, Object> response = glJobService.getAffliatedInstition(employerId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}