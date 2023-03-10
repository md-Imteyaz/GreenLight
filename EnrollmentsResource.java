package com.gl.platform.web.rest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.activity.InvalidActivityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.Enrollments;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.EnrollmentsQueryService;
import com.gl.platform.service.EnrollmentsService;
import com.gl.platform.service.dto.EnrollmentCriteria;
import com.gl.platform.service.dto.EnrollmentDTO;
import com.gl.platform.service.dto.GlStudentDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

/**
 * REST controller for managing Enrollments.
 */
@RestController
@RequestMapping("/api")
// @RolesAllowed({"ROLE_ADMIN", "ROLE_SITE_ADMIN", "ROLE_UNIVERSITY"})
public class EnrollmentsResource {

	private final Logger log = LoggerFactory.getLogger(EnrollmentsResource.class);

	private static final String ENTITY_NAME = Enrollments.class.getSimpleName().toLowerCase();

	private final EnrollmentsService enrollmentsService;

	private final EnrollmentsQueryService enrollmentsQueryService;

	@Autowired
	private AuthorizationService authorizationService;

	public EnrollmentsResource(EnrollmentsService enrollmentsService, EnrollmentsQueryService enrollmentsQueryService) {
		this.enrollmentsService = enrollmentsService;
		this.enrollmentsQueryService = enrollmentsQueryService;
	}

	/**
	 * GET /enrollments : get all the enrollments.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of enrollments
	 *         in body
	 */
	@GetMapping("/enrollments")
	@Timed
	public ResponseEntity<Page<EnrollmentDTO>> getAllEnrollments(EnrollmentCriteria criteria, Pageable page) {
		log.debug("REST request to get Enrollments by criteria: {}", criteria);
		if (criteria.getInstitutionId() == null) {
			throw new BadRequestAlertException("InstitutionId must not be null", "Enrollment", "Enrollment");
		}
		if (!authorizationService.ownedByInstitution(criteria.getInstitutionId().getEquals())) {
			throw new AccessDeniedException("not the owner");
		}

		Page<EnrollmentDTO> pages = enrollmentsQueryService.findByCriteria(criteria, page);
		for (EnrollmentDTO eDTO : pages) {
			eDTO.setInstitution(null);
			eDTO.setTranscript(null);
			GlStudentDTO student = new GlStudentDTO();
			student.setId(eDTO.getId());
			student.setFirstName(eDTO.getStudent().getFirstName());
			student.setLastName(eDTO.getStudent().getLastName());
			student.setDateOfBirth(eDTO.getStudent().getDateOfBirth());
			student.setEmail(eDTO.getStudent().getEmail());
			student.setCreatedDate(eDTO.getStudent().getCreatedDate());
			student.setSchoolStudentId(eDTO.getStudent().getSchoolStudentId());
			eDTO.setStudent(student);
		}

		return new ResponseEntity<>(pages, HttpStatus.OK);
	}

	/**
	 * GET /enrollment : get all the enrollments.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of enrollments
	 *         in body
	 */
	@GetMapping("/claimcredentials/{userId}")
	@Timed
	public ResponseEntity<Object> getClaimCredentials(@PathVariable Long userId) {
		log.debug("In Calim Credentils Get call by userId {}", userId);

		List<Map<String, Object>> claimCredentialsList = enrollmentsQueryService.getClaimCredentials(userId);
		return new ResponseEntity<>(claimCredentialsList, HttpStatus.OK);
	}

	@GetMapping("/enrollment/search/{id}")
	@Timed
	public ResponseEntity<Map<String, String>> getEnrollmentsBySearhTerm(@PathVariable Long id,
			@RequestParam(value = "searchTerm", required = false) String searchTerm) throws IOException {

		if (!authorizationService.ownedByInstitution(id)) {
			throw new AccessDeniedException("not the owner");
		}

		Map<String, String> response = enrollmentsService.exportEnrollmentsCsv(id, searchTerm);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/enrollment/getcode")
	@Timed
	public Map<String, String> getEnrollementid(@RequestParam("studentId") String studentId,
			@RequestParam("dob") LocalDate dob, @RequestParam("lastName") String lastName,
			@RequestParam("institutionId") Long institutionId,
			@RequestParam(value = "last4ssn", required = false) String last4SSN)
			throws InvalidActivityException, NotFoundException {

		if (studentId == null || dob == null && institutionId == null && (last4SSN == null || last4SSN.length() > 4)) {
			throw new InvalidActivityException("Provide all the valid details");
		}
		log.debug("REST request to get an enrollement id: ");

		return enrollmentsService.getEnrollementCode(studentId, dob, lastName, institutionId, last4SSN);
	}

}
