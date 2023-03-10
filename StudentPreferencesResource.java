package com.gl.platform.web.rest;

import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.domain.CareerPathList;
import com.gl.platform.domain.IndustryList;
import com.gl.platform.domain.StudentEmployerBlackList;
import com.gl.platform.repository.CareerPathListRepository;
import com.gl.platform.repository.IndustryListRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.StudentPreferencesService;
import com.gl.platform.service.dto.JobSurveyDTO;
import com.gl.platform.service.dto.PreferenceRequestDTO;
import com.gl.platform.service.dto.StudentCredentialVisibilityDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.swagger.annotations.ApiImplicitParam;
import javassist.NotFoundException;

/**
 * REST controller for managing StudentPreferences.
 */
@RestController
@RequestMapping("/api")
public class StudentPreferencesResource {

	private final Logger log = LoggerFactory.getLogger(StudentPreferencesResource.class);

	private static final String ENTITY_NAME = "studentPreferences";

	private final StudentPreferencesService studentPreferencesService;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private IndustryListRepository industryListRepo;

	@Autowired
	private CareerPathListRepository careerPathListRepository;

	public StudentPreferencesResource(StudentPreferencesService studentPreferencesService) {
		this.studentPreferencesService = studentPreferencesService;
	}

	/**
	 * POST /preference : Create a new studentPreference.
	 *
	 * @param PreferenceRequestDTO the PreferenceRequestDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         PreferenceRequestDTO, or with status 400 (Bad Request) if the
	 *         PreferenceRequestDTO has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/preferences")
	@Timed
	public ResponseEntity<PreferenceRequestDTO> createStudentPreference(
			@Valid @RequestBody PreferenceRequestDTO preferenceRequestDTO) throws URISyntaxException {
		log.debug("REST request to save StudentPreferences : {}", preferenceRequestDTO);
		if (preferenceRequestDTO.getUserId() == null) {
			throw new BadRequestAlertException("A new Preferences should have user ID", ENTITY_NAME, "id null");
		}

		if (!authorizationService.ownedByUserOnly(preferenceRequestDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		PreferenceRequestDTO result = new PreferenceRequestDTO();
		try {
			result = studentPreferencesService.save(preferenceRequestDTO);
		} catch (JsonProcessingException e) {
			log.error("Error in saving student preference", e);
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * PUT /preferences : Updates an existing studentPreference.
	 *
	 * @param PreferenceRequestDTO the PreferenceRequestDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         PreferenceRequestDTO, or with status 400 (Bad Request) if the
	 *         PreferenceRequestDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the PreferenceRequestDTO couldn't be updated
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws JsonProcessingException
	 */
	@PutMapping("/preferences")
	@Timed
	public ResponseEntity<PreferenceRequestDTO> updateStudentPreference(
			@Valid @RequestBody PreferenceRequestDTO preferenceRequestDTO)
			throws URISyntaxException, JsonProcessingException {
		log.debug("REST request to save StudentPreferences : {}", preferenceRequestDTO);
		if (!authorizationService.ownedByUserOnly(preferenceRequestDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		PreferenceRequestDTO result = studentPreferencesService.save(preferenceRequestDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/preferences/{id}")
	@Timed
	public ResponseEntity<PreferenceRequestDTO> getPreferences(@PathVariable Long id) throws JsonProcessingException {
		log.debug("REST request to get the student preferences");
		if (!authorizationService.ownedByUserOnly(id)) {
			throw new AccessDeniedException("not the owner");
		}
		PreferenceRequestDTO result = studentPreferencesService.getPreferencesBy(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/industry/dropdown")
	public ResponseEntity<List<IndustryList>> getIndustryList() {

		log.debug("Request to get industry list");

		List<IndustryList> list = industryListRepo.findAll();

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@PostMapping("/job/survey")
	@Timed
	public ResponseEntity<JobSurveyDTO> createJobSurvey(@Valid @RequestBody JobSurveyDTO jobSurveyDTO)
			throws JsonProcessingException {
		log.debug("REST request to save jobSurvey : {}", jobSurveyDTO);
		if (jobSurveyDTO.getUserId() == null) {
			throw new BadRequestAlertException("JobSurvey should have user ID", ENTITY_NAME, "id null");
		}

		if (!authorizationService.ownedByUserOnly(jobSurveyDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		jobSurveyDTO = studentPreferencesService.saveJobSurvey(jobSurveyDTO);
		return new ResponseEntity<>(jobSurveyDTO, HttpStatus.OK);
	}

	@GetMapping("/job/survey/{userId}")
	public ResponseEntity<?> getJobSurvey(@PathVariable Long userId) {

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		JobSurveyDTO jobSurveyDTO = studentPreferencesService.getJobSurveyByUserId(userId);
		return new ResponseEntity<>(jobSurveyDTO, HttpStatus.OK);
	}

	@GetMapping("/credential/visibility/{userId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<StudentCredentialVisibilityDTO> getCredentialVisibility(@PathVariable Long userId)
			{

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		StudentCredentialVisibilityDTO credVis = studentPreferencesService.getStudentCredentialview(userId);
		return new ResponseEntity<>(credVis, HttpStatus.OK);
	}

	@GetMapping("/career/pathway/dropdown")
	public ResponseEntity<?> getCareerPathList() {

		log.debug("Request to get Career Path list");

		List<CareerPathList> list = careerPathListRepository.findAll();

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("/student/employer/blacklist")
	public ResponseEntity<?> getStudentEmployerBlackList() throws NotFoundException {

		log.debug("Request to get student employer black list");

		List<StudentEmployerBlackList> list = studentPreferencesService.getStudentEmployerBlackList();

		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@PostMapping("/student/employer/blacklist")
	@Timed
	public ResponseEntity<?> employerBlackList(@RequestBody List<Long> employerIdsList) throws NotFoundException {

		List<StudentEmployerBlackList> list = studentPreferencesService.saveBlackList(employerIdsList);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

}
