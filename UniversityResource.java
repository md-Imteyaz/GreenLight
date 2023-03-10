package com.gl.platform.web.rest;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.repository.UserRepository;
import com.gl.platform.security.AuthoritiesConstants;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.UniversityService;
import com.gl.platform.service.dto.GlStudentDTO;
import com.gl.platform.service.dto.ProspectDTO;
import com.gl.platform.service.dto.adminPostRequestDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;

/**
 * REST controller for managing University.
 */
@RestController
@RequestMapping("/api")
public class UniversityResource {

	private final Logger log = LoggerFactory.getLogger(UniversityResource.class);

	private final UniversityService universityService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthorizationService authorizationService;

	public UniversityResource(UniversityService universityService) {
		this.universityService = universityService;
	}

	/**
	 * GET /prospects : get all the institution prospects.
	 *
	 * @param id the id of the institution for which prospects to retrieve
	 * @return the ResponseEntity with status 200 (OK) and the list of prospects in
	 *         body
	 */
	@GetMapping("/prospects/{institutionId}")
	@Timed
	public ResponseEntity<List<ProspectDTO>> getProspectsByInstitutionId(@PathVariable Long institutionId) {
		log.debug("REST request to get prospects by criteria: {}", institutionId);
		List<ProspectDTO> prospects = universityService.getProspectsByInstitutionId(institutionId);
		return new ResponseEntity<>(prospects, HttpStatus.OK);
	}

	/**
	 * GET /search-students : get all the institution students.
	 *
	 * @param id the id of the institution for which students to retrieve
	 * @return the ResponseEntity with status 200 (OK) and the list of prospects in
	 *         body
	 */
	@GetMapping("/search-students/{institutionId}")
	@Timed
	@RolesAllowed(AuthoritiesConstants.UNIVERSITY)
	public ResponseEntity<List<GlStudentDTO>> getStudentsByInstitutionId(@PathVariable Long institutionId) {
		log.debug("REST request to get instituion students by criteria: {}", institutionId);
		List<GlStudentDTO> students = universityService.getStudentsByInstitutionIdOptmized(institutionId);
		return new ResponseEntity<>(students, HttpStatus.OK);
	}

	@GetMapping("/search-students/{institutionId}/{searchTerm}")
	@Timed
	public ResponseEntity<List<GlStudentDTO>> getHoldsByInstitutionId(@PathVariable Long institutionId,
			@PathVariable String searchTerm) {
		log.debug("REST request to get instituion students by criteria: {}", institutionId);
		List<GlStudentDTO> students = universityService.getHoldsByInstitutionId(institutionId, searchTerm);
		return new ResponseEntity<>(students, HttpStatus.OK);
	}

	/**
	 * GET /search-students : get all the institution students.
	 *
	 * @param id the id of the institution for which students to retrieve
	 * @return the ResponseEntity with status 200 (OK) and the list of prospects in
	 *         body
	 * @throws NotFoundException
	 */
	@GetMapping("/student-view/{institutionId}/{studentId}")
	@Timed
	public ResponseEntity<GlStudentDTO> getStudentBy(@PathVariable Long institutionId, @PathVariable Long studentId)
			throws NotFoundException {
		log.debug("REST request to get student information by criteria: {}", institutionId);
		GlStudentDTO student = universityService.getStudentBy(institutionId, studentId);
		return new ResponseEntity<>(student, HttpStatus.OK);
	}

	/**
	 * GET /search-students : get all the institution students.
	 *
	 * @param id the id of the institution for which students to retrieve
	 * @return the ResponseEntity with status 200 (OK) and the list of prospects in
	 *         body
	 */
	@GetMapping("/view-holds/{institutionId}")
	@Timed
	public ResponseEntity<List<GlStudentDTO>> getStudentHoldsBy(@PathVariable Long institutionId) {
		log.debug("REST request to get prospects by criteria: {}", institutionId);
		List<GlStudentDTO> students = universityService.getStudentHoldsBy(institutionId);
		return new ResponseEntity<>(students, HttpStatus.OK);
	}

	@GetMapping("/administrator/{institutionId}/{userId}")
	@Timed
	public ResponseEntity<Object> getUserRole(@PathVariable Long institutionId, @PathVariable Long userId) {
		log.debug("REST request to get adminstrators by University: {}", institutionId);
		List<Map<String, Object>> response = universityService.getUserRoleObjects(institutionId, userId);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/administrator/remove/{id}")
	public ResponseEntity<Map<String, Object>> deleteAllStudents(@PathVariable Long id) {
		log.info("REST request to delete administartor : {}", id);
		Map<String, Object> response = universityService.deleteAdminByUserId(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/administrator")
	@Timed
	public ResponseEntity<Object> save(@RequestBody adminPostRequestDTO requestedDTO) {

		userRepository.findOneByEmailIgnoreCase(requestedDTO.getUser().getEmail().toLowerCase()).ifPresent(u -> {
			throw new EmailAlreadyUsedException();
		});

		userRepository.findOneByLogin(requestedDTO.getUser().getUsername()).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});

		Map<String, Object> response = universityService.saveByValidate(requestedDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * api/administrator/deactivate Deactivate campus adminstrators by using userids
	 * 
	 * @param userIds
	 * @return
	 */
	@PostMapping("/administrator/deactivate")
	@Timed
	public ResponseEntity<Object> deactivateAdminstrator(@RequestBody List<Long> userIds) {
		log.debug("REST request to deactivate adminstrators by userIds: {}", userIds);
		Map<String, Object> response = universityService.deactivateAdminsByUserIds(userIds);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * api/administrator/activate Deactivate campus adminstrators by using userids
	 * 
	 * @param userIds
	 * @return
	 */
	@PostMapping("/administrator/activate")
	@Timed
	public ResponseEntity<Object> activateAdminstrator(@RequestBody List<Long> userIds) {
		log.debug("REST request to activate adminstrators by userIds: {}", userIds);
		Map<String, Object> response = universityService.activateAdminsByUserIds(userIds);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * api/administrator/resendMail Deactivate campus adminstrators by using userids
	 * 
	 * @param userIds
	 * @return
	 */

	@PostMapping("/administrator/resendMail")
	@Timed
	public ResponseEntity<Object> resendMailForAdminstrator(@RequestBody List<Long> userIds) {
		log.debug("REST request to send mails to activate adminstrators by userIds: {}", userIds);
		Map<String, Object> response = universityService.resendMailForAdminstrator(userIds);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/su-admin/admins")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.GREENLIGHT_SUPER_ADMIN })
	public ResponseEntity<Object> getAllAdmins(@RequestParam(required = false) String keyword, Pageable pageable) {
		log.debug("REST request to get Institution users by keyword: {}", keyword);
		Map<String, Object> response = universityService.getAdminsByCriteria(keyword, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/parentadmins/{id}")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.GREENLIGHT_SUPER_ADMIN })
	public ResponseEntity<Object> getAllAdminsByParent(@PathVariable Long id) {
		log.debug("REST request to get adminstrators :");
		List<Map<String, Object>> response = universityService.getAllAdminsByParent(id);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/stubbed/student/{schoolId}/{type}")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.GREENLIGHT_SUPER_ADMIN })
	public ResponseEntity<Void> changeStatusOfTranscript(@PathVariable Long schoolId, @PathVariable String type)
			throws NotFoundException {
		log.debug("REST request to get adminstrators :");
		universityService.setTranscriptStatus(schoolId, type);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("/inst/counsellor")
	@Timed
	public ResponseEntity<Map<String, String>> updateCounsellorInstitution(
			@RequestParam(value = "userId", required = true) Long userId,
			@RequestParam(value = "previousInstitutionId", required = true) Long previousInstitutionId,
			@RequestParam(value = "currentInstitutionId", required = true) Long currentInstitutionId)
			throws IOException {
		log.debug("REST request to update the counsellor for institution id : {}", currentInstitutionId);
		if (!authorizationService.isSiteAdmin()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, String> result = universityService.updateCousellorSchool(userId, previousInstitutionId,
				currentInstitutionId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/developer/credentials/{userId}")
	public ResponseEntity<Map<String, Object>> getApiCredentials(@PathVariable Long userId)
			throws javassist.NotFoundException {
		log.info("REST request to get the api credentials with user id : {}", userId);
		Map<String, Object> response = universityService.getDeveloperApiCredentials(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
