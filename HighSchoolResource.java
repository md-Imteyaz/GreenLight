package com.gl.platform.web.rest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.HighSchoolService;
import com.gl.platform.service.HighSchoolTranscriptService;
import com.gl.platform.service.dto.StatisticsRequestDTO;

import io.swagger.annotations.ApiImplicitParam;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api/hs")
public class HighSchoolResource {

	private final Logger log = LoggerFactory.getLogger(HighSchoolResource.class);

	@Autowired
	private HighSchoolTranscriptService highSchoolTranscriptService;

	@Autowired
	private HighSchoolService highSchoolService;

	@GetMapping("/parent/{id}/childs")
	@Timed
	public ResponseEntity<?> getAllChildsInfo(@PathVariable Long id) throws IOException, NotFoundException {
		log.debug("Rest Request to get the students list using parent user id : {}", id);
		return ResponseEntity.ok().body(highSchoolTranscriptService.getChildsInformation(id));
	}

	@GetMapping("/credentials/{studentId}")
	@Timed
	public ResponseEntity<?> getStudentCredential(@PathVariable Long studentId) throws NotFoundException {
		log.debug("Rest Request to get the transcript using student id : {}", studentId);

		return ResponseEntity.ok().body(highSchoolTranscriptService.getTranscript(studentId));

	}

	@GetMapping("/claim/{uuid}/{dob}/{lastName}")
	@Timed
	public ResponseEntity<?> getValidatedGlParents(@PathVariable String uuid, @PathVariable LocalDate dob,
			@PathVariable String lastName) throws NotFoundException {

		log.debug("REST request to get GlStudent by UUID: {}", uuid);
		Map<String, Object> response = highSchoolTranscriptService.claimCredentials(uuid, dob, lastName);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/claim")
	@Timed
	public ResponseEntity<?> getValidatedParentsBy(@RequestParam(value = "uuid") String uuid,
			@RequestParam(value = "dateOfBirth") String dateOfBirth, @RequestParam(value = "lastName") String lastName,
			@RequestParam(value = "parentLastName", required = false) String parentLastName,
			@RequestParam(value = "parentFirstName", required = false) String parentFirstName)
			throws NotFoundException {

		log.debug("REST request to get GlStudent by UUID: {}", uuid);
		Map<String, Object> response = highSchoolTranscriptService.claimCredentials(uuid, dateOfBirth, lastName,
				parentLastName, parentFirstName);
		return ResponseEntity.ok(response);

	}

	@GetMapping("/parent/enrollment")
	@Timed
	public ResponseEntity<?> getEnrollmentsByLoggedInParent() {
		log.debug("REST request to get enrollments by parent");
		Map<String, Object> response = highSchoolTranscriptService.getEnrollments();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/add/child/{studentId}")
	@Timed
	public ResponseEntity<?> addChildToParent(@PathVariable Long studentId) throws NotFoundException {
		log.debug("REST request to add child to parent with glStudentId: {}", studentId);
		Map<String, Object> response = highSchoolTranscriptService.addChildDetialsToParent(studentId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/parent/credentials/{parentId}")
	@Timed
	public ResponseEntity<?> getAllTranscriptBasedOnParentId(@PathVariable Long parentId) {
		log.debug("REST request to get enrollments by parent");
		Map<String, Object> response = highSchoolTranscriptService.getChildCredentialToParent(parentId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/statistics")
	@Timed
	public ResponseEntity<?> getStatisticsByParent() {
		log.debug("REST request to get statistics by parent");
		Map<String, Object> response = highSchoolTranscriptService.getStatisticsToParent();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/consent/counts/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getParentConsentDetails(@PathVariable Long institutionId,
			@RequestParam(value = "excludeNonZeroSchool", required = true) boolean excludeNonZeroSchool,
			@RequestParam(value = "isCurrentlyEnrolledOnly", required = true) boolean isCurrentlyEnrolledOnly) {
		log.debug("REST request to get parent consent records from institution : {}", institutionId);
		List<Map<String, Object>> response = highSchoolService.getParentConsentDetails(institutionId,
				excludeNonZeroSchool, isCurrentlyEnrolledOnly);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/campus/specific/details")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<?> getCampusSpecificDetails(
			@RequestParam(value = "schoolId", required = true) String schoolId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "registrationstatus", required = true) String registrationstatus,
			@RequestParam(value = "parentConsent", required = true) String parentConsent,
			@RequestParam(value = "isCurrentlyEnrolledOnly", required = true) boolean isCurrentlyEnrolledOnly,
			@RequestParam(value = "userIdForOnlyImported", required = false) String userIdForOnlyImported) {
		log.debug("REST request to get parent consent records from school id : {}", schoolId);
		List<Map<String, Object>> response = highSchoolService.getCampusSpecificParentRecords(schoolId, institutionId,
				registrationstatus, parentConsent, isCurrentlyEnrolledOnly, userIdForOnlyImported);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/institution/schoolids/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<?> getCampusSpecificSchoolIds(@PathVariable Long institutionId) {
		log.debug("REST request to get child institution records from institution : {}", institutionId);
		List<Map<String, Object>> response = highSchoolService.getSchoolIds(institutionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/user/counts")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<?> getHighschoolUsersDetails(
			@RequestParam(value = "childInstId", required = false) String childInstId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "registrationstatus", required = false) String registrationstatus,
			@RequestParam(value = "gradeLevel", required = false) String gradeLevel,
			@RequestParam(value = "isCurrentlyEnrolledOnly", required = false) boolean isCurrentlyEnrolledOnly) {
		List<Map<String, Object>> response = highSchoolService.getUserCounts(institutionId, childInstId,
				isCurrentlyEnrolledOnly, registrationstatus, gradeLevel);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/share/counts")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<?> getHighschoolShareDetails(@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		Map<String, Object> response = highSchoolService.getShareCounts(statisTicsRequestDTO);
		return ResponseEntity.ok(response);
	}

}
