package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.activity.InvalidActivityException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.domain.GlStudent;
import com.gl.platform.domain.JhiUser;
import com.gl.platform.repository.JhiUserRepository;
import com.gl.platform.security.AppUser;
import com.gl.platform.security.SecurityUtils;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlStudentQueryService;
import com.gl.platform.service.GlStudentService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.dto.GlStudentCriteria;
import com.gl.platform.service.dto.GlStudentDTO;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.StudentStatisticsDTO;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.EmailAndUsernameAlreadyUsedException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

/**
 * REST controller for managing GlStudent.
 */
@RestController
@RequestMapping("/api")
public class GlStudentResource {

	private final Logger log = LoggerFactory.getLogger(GlStudentResource.class);

	private static final String ENTITY_NAME = GlStudent.class.getSimpleName().toLowerCase();

	private final GlStudentService glStudentService;

	private final GlStudentQueryService glStudentQueryService;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private GlUserService glUserService;

	@Autowired
	private JhiUserRepository userRepository;

	public GlStudentResource(GlStudentService glStudentService, GlStudentQueryService glStudentQueryService) {
		this.glStudentService = glStudentService;
		this.glStudentQueryService = glStudentQueryService;
	}

	/*
	 * @PostMapping("/gl-students")
	 * 
	 * @Timed public ResponseEntity<GlStudentDTO>
	 * createGlStudent(@Valid @RequestBody GlStudentDTO glStudentDTO) throws
	 * URISyntaxException { log.debug("REST request to save GlStudent : {}",
	 * glStudentDTO); if (glStudentDTO.getId() != null) { throw new
	 * BadRequestAlertException("A new glStudent cannot already have an ID",
	 * ENTITY_NAME, "idexists"); } GlStudentDTO result =
	 * glStudentService.save(glStudentDTO); return ResponseEntity.created(new
	 * URI("/api/gl-students/" + result.getId()))
	 * .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME,
	 * result.getId().toString())).body(result); }
	 * 
	 * 
	 * 
	 * @PutMapping("/gl-students")
	 * 
	 * @Timed public ResponseEntity<GlStudentDTO>
	 * updateGlStudent(@Valid @RequestBody GlStudentDTO glStudentDTO) throws
	 * URISyntaxException { log.debug("REST request to update GlStudent : {}",
	 * glStudentDTO); if (glStudentDTO.getId() == null) { throw new
	 * BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull"); } GlStudentDTO
	 * result = glStudentService.saveUpdate(glStudentDTO); return
	 * ResponseEntity.ok() .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME,
	 * glStudentDTO.getId().toString())).body(result); }
	 */

	/**
	 * PUT /gl-students : Updates an existing glStudent.
	 *
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         glStudentDTO, or with status 400 (Bad Request) if the glStudentDTO is
	 *         not valid, or with status 500 (Internal Server Error) if the
	 *         glStudentDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@GetMapping("/gl-students/{type}/{userId}/{studentId}")
	@Timed
	public ResponseEntity<GlStudentDTO> updateGlStudent(@PathVariable String type, @PathVariable Long userId,
			@PathVariable Long studentId) throws URISyntaxException, NotFoundException {
		log.debug("REST request to update GlStudent with credential type: {}, userId: {}, studentId: {} ", type, userId,
				studentId);
		GlStudentDTO result = glStudentService.saveUpdate(type, userId, studentId);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
				.body(result);
	}

	/**
	 * GET /gl-students : get all the glStudents.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of glStudents in
	 *         body
	 */
	@GetMapping("/gl-students")
	@Timed
	public ResponseEntity<List<GlStudentDTO>> getAllGlStudents(GlStudentCriteria criteria, Pageable pageable) {
		log.debug("REST request to get GlStudents by criteria: {}", criteria);
		Page<GlStudentDTO> page = glStudentQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/gl-students");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	@PostMapping("/gl-students/full")
	@Timed
	public ResponseEntity<?> createGlStudent(@Valid @RequestBody GlStudent glStudent)
			throws URISyntaxException, JsonProcessingException {
		log.debug("REST request to save GlStudent : {}", glStudent);
		if (userRepository.findOneByLogin(glStudent.getUser().getUsername().toLowerCase()).isPresent()
				&& userRepository.findOneByEmailIgnoreCase(glStudent.getUser().getEmail()).isPresent()) {
			throw new EmailAndUsernameAlreadyUsedException();
		}
		userRepository.findOneByLogin(glStudent.getUser().getUsername().toLowerCase()).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});
		userRepository.findOneByEmailIgnoreCase(glStudent.getUser().getEmail()).ifPresent(u -> {
			throw new EmailAlreadyUsedException();
		});

		GlStudentDTO result = glStudentService.saveFull(glStudent);
		return ResponseEntity.created(new URI("/api/gl-students/full" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PutMapping("/gl-students/full")
	@Timed
	public ResponseEntity<?> updateGlStudentFull(@Valid @RequestBody GlStudentDTO glStudent) throws URISyntaxException {
		log.debug("REST request to save GlStudent : {}", glStudent);
		userRepository.findOneByLogin(glStudent.getUser().getUsername().toLowerCase()).ifPresent(u -> {
			if (!u.getId().equals(glStudent.getUser().getUserId())) {
				log.debug("search for it" + u.getId() + " " + glStudent.getUser().getUserId());
				throw new LoginAlreadyUsedException();
			}
		});
		userRepository.findOneByEmailIgnoreCase(glStudent.getUser().getEmail()).ifPresent(u -> {
			if (!u.getId().equals(glStudent.getUser().getUserId())) {
				throw new EmailAlreadyUsedException();
			}

		});

		GlStudentDTO result = glStudentService.updateFull(glStudent);
		return ResponseEntity.created(new URI("/api/gl-students/full" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * GET /gl-students/:id : get the "id" glStudent.
	 *
	 * @param id the id of the glStudentDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         glStudentDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/gl-students/{id}")
	@Timed
	public ResponseEntity<GlStudentDTO> getGlStudent(@PathVariable Long id) {
		log.debug("REST request to get GlStudent : {}", id);
		Optional<GlStudentDTO> glStudentDTO = glStudentService.findOne(id);
		return ResponseUtil.wrapOrNotFound(glStudentDTO);
	}

	@GetMapping("/gl-students/{uuid}/{dateOfBirth}/{lastName}/{type}")
	@Timed
	public ResponseEntity<?> getGlStudent(@PathVariable String uuid, @PathVariable LocalDate dateOfBirth,
			@PathVariable String lastName, @PathVariable String type) {
		log.debug("REST request to get GlStudent by UUID: {}", uuid);
		boolean status = glStudentService.setTranscriptUUID(uuid, type);
		Optional<GlStudentDTO> glStudentDTO = Optional.empty();
		if ((type.equalsIgnoreCase("transcript") || type.equalsIgnoreCase("edready")) && status) {
			glStudentDTO = glStudentService.findByEnrollmentUUID(uuid, dateOfBirth, lastName);
			if (glStudentDTO.isPresent() && glStudentDTO.get().getUserId() != null) {
				return ResponseUtil.wrapOrNotFound(checkExitingUser(glStudentDTO));
			} else if (glStudentDTO.isPresent()) {
				glStudentDTO.get().setTranscripts(null);
				// check the details(like dob,ssn, fname,lname) of student already exists with
				// some other user account
				List<GlStudentDTO> existingStudentRecord = glStudentService.getExistingStudentwithDetials(
						glStudentDTO.get().getFirstName(), glStudentDTO.get().getLastName(),
						glStudentDTO.get().getDateOfBirth(), glStudentDTO.get().getLast4Ssn(),
						glStudentDTO.get().getId());
				if (!existingStudentRecord.isEmpty() && !type.equalsIgnoreCase("edready")) {
					existingStudentRecord.get(0).setTranscripts(null);
					glStudentDTO = Optional.of(existingStudentRecord.get(0));
					return ResponseUtil.wrapOrNotFound(checkExitingUser(glStudentDTO));
				} else if (type.equalsIgnoreCase("edready")) {

					if (!existingStudentRecord.isEmpty()) {
						for (GlStudentDTO glStudentDTO2 : existingStudentRecord) {
							if (glStudentDTO2.getUserId() != null) {
								glStudentDTO.get().setUserId(glStudentDTO2.getUserId());
								glStudentService.save(glStudentDTO.get());
								return ResponseUtil.wrapOrNotFound(glStudentDTO);
							}
						}
					}

					if (existingStudentRecord.isEmpty()) {
						existingStudentRecord = glStudentService.getExistingStudentwithDetials(
								glStudentDTO.get().getFirstName(), glStudentDTO.get().getLastName(),
								glStudentDTO.get().getDateOfBirth(), glStudentDTO.get().getId());
					}
					if (!existingStudentRecord.isEmpty()) {
						glStudentDTO.get().setUser(existingStudentRecord.get(0).getUser());
						// last 4 ssn is needed for further validation
						glStudentDTO.get().setAdditionalInformationRequired("ssn");
						return ResponseUtil.wrapOrNotFound(glStudentDTO);
					}
				}
			}
			return ResponseUtil.wrapOrNotFound(glStudentDTO);
		} else if (type.equalsIgnoreCase("badge")) {
			if (!status) {
				glStudentDTO = glStudentService.findByEnrollmentUUID(uuid);
				if (glStudentDTO.isPresent()) {
					glStudentDTO.get().setTranscripts(null);
				}
				return ResponseUtil.wrapOrNotFound(glStudentDTO);
			}
		} else if (type.equalsIgnoreCase("parent")) {
			Map<String, Object> response = glStudentService.getParentInformationFor(uuid, dateOfBirth, lastName);

			return ResponseEntity.ok().body(response);
		}
		return ResponseUtil.wrapOrNotFound(glStudentDTO);
	}

	private Optional<GlStudentDTO> checkExitingUser(Optional<GlStudentDTO> glStudentDTO) {
		Optional<GlUserDTO> glUser = glUserService.findByOne(glStudentDTO.get().getUserId());
		if (glUser.isPresent()) {
			Optional<JhiUser> jhiUser = userRepository.findById(glUser.get().getUserId());
			if (jhiUser.isPresent() && !jhiUser.get().isActivated()) {
				glUser.get().setAddress(null);
				glUser.get().setJhiPassword(null);
				glStudentDTO.get().setUser(glUser.get());
			}
		}
		return glStudentDTO;
	}

	@GetMapping("/gl-student")
	@Timed
	public ResponseEntity<?> getValidatedGlStudent(@RequestParam(value = "uuid") String uuid,
			@RequestParam(value = "dateOfBirth") LocalDate dateOfBirth,
			@RequestParam(value = "lastName") String lastName,
			@RequestParam(value = "newLastName", required = false) String newLastName,
			@RequestParam(value = "type") String type) {

		log.debug("REST request to get GlStudent by UUID: {}", uuid);
		Boolean status = glStudentService.setTranscriptUUID(uuid, type);
		Optional<GlStudentDTO> glStudentDTO = Optional.empty();
		if (type.equalsIgnoreCase("transcript")) {
			if (status == true) {
				GlStudentDTO currentAccount = getExistingAccount(uuid);
				if (currentAccount == null) {
					glStudentDTO = glStudentService.findByEnrollmentUUID(uuid, dateOfBirth, lastName);
					if (glStudentDTO.isPresent()) {
						glStudentDTO.get().setTranscripts(null);
					} else {
						glStudentDTO = glStudentService.findByEnrollmentUUID(uuid, dateOfBirth);

						if (glStudentDTO.isPresent()) {

							Optional<AppUser> currentUser = SecurityUtils.getCurrentUser();
							if (currentUser.isPresent()) {

								Long userId = currentUser.get().getUserId();

								Optional<GlUserDTO> glUser = glUserService.findByIdAndLast4ssn(userId,
										glStudentDTO.get().getLast4Ssn());

								if (glUser.isPresent() && glUser.get() != null) {

									glStudentDTO.get().setTranscripts(null);
									glStudentDTO.get().setUserLastName(glUser.get().getLastName());

								} else {

									GlStudentDTO glStudent = new GlStudentDTO();
									glStudent.setAdditionalInformationRequired("Last Name");
									glStudentDTO = Optional.of(glStudent);

								}
							}
						}
					}
				} else {
					return ResponseUtil.wrapOrNotFound(Optional.of(currentAccount));
				}
				return ResponseUtil.wrapOrNotFound(glStudentDTO);
			}
		} else if (type.equalsIgnoreCase("badge")) {
			if (status == false) {
				glStudentDTO = glStudentService.findByEnrollmentUUID(uuid);
				if (glStudentDTO.isPresent()) {
					glStudentDTO.get().setTranscripts(null);
				}
				return ResponseUtil.wrapOrNotFound(glStudentDTO);
			}
		} else if (type.equalsIgnoreCase("parent")) {
			Map<String, Object> response = glStudentService.getParentInformationFor(uuid, dateOfBirth, newLastName);
			return ResponseEntity.ok().body(response);
		}
		return ResponseUtil.wrapOrNotFound(glStudentDTO);
	}

	private GlStudentDTO getExistingAccount(String uuid) {
		Optional<GlStudentDTO> optGlStudentDTO = glStudentService.findByEnrollmentUUID(uuid);
		if (optGlStudentDTO.isPresent()) {
			GlStudentDTO glStudentDTO = optGlStudentDTO.get();
			Optional<AppUser> currentUser = SecurityUtils.getCurrentUser();
			if (currentUser.isPresent()) {
				Long userId = currentUser.get().getUserId();
				if (userId.equals(glStudentDTO.getUserId())) {
					return glStudentDTO;
				}
			}
		}
		return null;
	}

	/**
	 * GET /gl-students/:id/statistics : get the "id" glStudent.
	 *
	 * @param id the id of the glStudentDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         glStudentDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/gl-students/{id}/statistics")
	@Timed
	public ResponseEntity<StudentStatisticsDTO> getGlStudentStatistics(@PathVariable Long id) {
		log.debug("REST request to get GlStudent Statistics : {}", id);
		StudentStatisticsDTO glStudentStatistics = glStudentService.getStatisticsByStudentId(id);

		return ResponseUtil.wrapOrNotFound(Optional.of(glStudentStatistics));
	}

	/*
	 * @DeleteMapping("/gl-students/{id}")
	 * 
	 * @Timed public ResponseEntity<Void> deleteGlStudent(@PathVariable Long id) {
	 * log.debug("REST request to delete GlStudent : {}", id);
	 * glStudentService.delete(id); return
	 * ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
	 * id.toString())).build(); }
	 */

	@GetMapping("/statistics/student/{userId}")
	@Timed
	public ResponseEntity<StudentStatisticsDTO> getStatisticsByUserId(@PathVariable Long userId) {

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		StudentStatisticsDTO glStudentStatistics = glStudentService.getStatisticsByStudentId(userId);

		return new ResponseEntity<>(glStudentStatistics, HttpStatus.OK);
	}

	@GetMapping("/student/dashboard/statistics/{userId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getDashboardStatisticsByUserId(@PathVariable Long userId) {

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		Map<String, Object> glStudentStatistics = glStudentService.getDashboardStatisticsByStudentId(userId);

		return new ResponseEntity<>(glStudentStatistics, HttpStatus.OK);
	}

	@GetMapping("/resend/activate-mail/{userId}")
	@Timed
	public ResponseEntity<Void> resendActivationEmail(@PathVariable Long userId) {
		glStudentService.resendActivationMail(userId);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, userId.toString())).build();
	}

	@GetMapping("/existing/user")
	@Timed
	public ResponseEntity<?> getEnrollementid(@RequestParam("dob") LocalDate dob,
			@RequestParam("lastName") String lastName, @RequestParam("institutionId") Long institutionId,
			@RequestParam("last4ssn") String last4SSN, @RequestParam("uuid") String uuid)
			throws InvalidActivityException {

		if (dob == null && institutionId == null && (last4SSN == null || last4SSN.length() > 4)) {
			throw new InvalidActivityException("Provide all the valid details");
		}
		log.debug("REST request to verify existing user data: ");

		Optional<GlStudentDTO> glStudentDTO = glStudentService.getExistingUser(dob, lastName, institutionId, last4SSN,
				uuid);
		return ResponseUtil.wrapOrNotFound(glStudentDTO);
	}

	@GetMapping("/student/details/{userId}")
	@Timed
	public ResponseEntity<Set<Map<String, Object>>> getStduentDetails(@PathVariable Long userId)
			throws InvalidActivityException {

		if (userId == null) {
			throw new InvalidActivityException("Provide all the valid details");
		}
		log.debug("REST request to get the student details with user id : {}", userId);
		Set<Map<String, Object>> response = glStudentService.getStudentDetails(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/affiliations")
	@Timed
	public ResponseEntity<Set<String>> getStduentAffiliations() {
		log.debug("REST request to get the student affiliations");
		Set<String> response = glStudentService.getStudentAffifliations();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/survey/required/detials")
	@Timed
	public ResponseEntity<Map<String, Object>> getSurveyRequiredStudentDetails() throws NotFoundException {
		log.debug("REST request to get the student affiliations");
		Map<String, Object> response = glStudentService.getRequiredStudentDetails();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/nroc/redirect")
	@Timed
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Basic ")
	public ResponseEntity<Object> getStudentDetails(@RequestParam(value = "lastName", required = false) String lastName,
			@RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
			@RequestParam(value = "uuid", required = true) String enrollmentUuid)
			throws BadRequestException, NotFoundException {

		log.debug("REST request to get the details of students with enrollmentUuid : {}", enrollmentUuid);
		Object response = glStudentService.getStudentDetailsByEnrollmentUuid(enrollmentUuid, dateOfBirth, lastName);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
