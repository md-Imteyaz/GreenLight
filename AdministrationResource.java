package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gl.platform.service.AdministrationService;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.dto.CounsellorStudentIdsRequestDTO;
import com.gl.platform.service.dto.GlCounsellorFilterDTO;
import com.gl.platform.service.dto.MarketingUserAnalyticsDTO;
import com.gl.platform.service.util.GlConstraints;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class AdministrationResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AdministrationService administrationService;

	@Autowired
	private AuthorizationService authorizationService;

	public static final String OWNER = "not the owner";

	@PostMapping("/import/counsellor/students")
	public ResponseEntity<Map<String, Object>> importStudents(
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "userId", required = true) Long userId,
			@RequestParam(value = "institutionId", required = true) Long institutionId)
			throws IOException, NotFoundException {
		log.info("REST request to import the students for counsellor id : {}", userId);
		if (!authorizationService.ownedByCounsellor(userId) && !authorizationService.ownedByInstitution(institutionId)
				&& !authorizationService.isSupport()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = new HashMap<>();
		try {
			response = administrationService.counsellorStudentsImport(file, userId, institutionId);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IOException | NotFoundException e) {
			response.put(GlConstraints.MESSAGE, e.getLocalizedMessage());
			e.printStackTrace();
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/counsellor/student/ids")
	public ResponseEntity<Map<String, Object>> importStudentsByIds(
			@RequestBody CounsellorStudentIdsRequestDTO counsellorStudentIdsRequestDTO) throws IOException {
		log.info("REST request to import the students for counsellor id : {}",
				counsellorStudentIdsRequestDTO.getUserId());
		if (!authorizationService.ownedByCounsellor(counsellorStudentIdsRequestDTO.getUserId())
				&& !authorizationService.ownedByInstitution(counsellorStudentIdsRequestDTO.getInstitutionId())
				&& !authorizationService.isSupport()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = new HashMap<>();
		try {
			response = administrationService.loadCounsellorUploadedData(counsellorStudentIdsRequestDTO.getStudentIds(),
					counsellorStudentIdsRequestDTO.getInstitutionId(), counsellorStudentIdsRequestDTO.getUserId(),
					"Students imported via text");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IOException | NotFoundException e) {
			response.put(GlConstraints.MESSAGE, e.getLocalizedMessage());
			e.printStackTrace();
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/import/su/counsellor/students")
	public ResponseEntity<Map<String, Object>> importStudents(
			@RequestParam(value = "file", required = true) MultipartFile file) throws IOException, NotFoundException {
		log.info("REST request to import the students for multiple counsellor ids");
		if (!authorizationService.isGreenLightAdmin()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.loadStudentsForCounsellorFromSuperAdmin(file);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/counsellor/students")
	public ResponseEntity<Page<Map<String, Object>>> getStudents(
			@RequestBody GlCounsellorFilterDTO glCounsellorFilterDTO, Pageable pageable) throws NotFoundException {
		log.info("REST request to get the students for counsellor id : {}", glCounsellorFilterDTO.getUserId());
		if (!authorizationService.ownedByInstitution(glCounsellorFilterDTO.getInstitutionId())) {
			throw new AccessDeniedException(OWNER);
		}
		Page<Map<String, Object>> response = administrationService.getStudentsWithCounselorId(glCounsellorFilterDTO,
				pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/counsellor/students/searchterm")
	public ResponseEntity<Page<Map<String, Object>>> getStudentsBySearchTerm(
			@RequestBody GlCounsellorFilterDTO glCounsellorFilterDTO, Pageable pageable) {
		log.info("REST request to get the students for counsellor id : {}", glCounsellorFilterDTO.getUserId());
		if (!authorizationService.ownedByInstitution(glCounsellorFilterDTO.getInstitutionId())) {
			throw new AccessDeniedException(OWNER);
		}
		Page<Map<String, Object>> response = administrationService.getAllStudentDataPageable(glCounsellorFilterDTO,
				pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/export/all/students/{institutionId}/{counsellorId}")
	public ResponseEntity<List<Map<String, Object>>> exportAll(@PathVariable Long institutionId,
			@PathVariable Long counsellorId) {
		log.info("REST request to get the students for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId) || !authorizationService.isCounsellor()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.exportAllStudentsByInstitutionId(institutionId,
				counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/counsellor/student/{userId}")
	public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Long userId,
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId)
			throws NotFoundException, IOException {
		log.info("REST request to delete student for counsellor id : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.deleteStudent(userId, counsellorId, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/counsellor/students")
	public ResponseEntity<Map<String, Object>> deleteAllStudents(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId) {
		log.info("REST request to delete All students for counsellor id : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId) && !authorizationService.isCounsellor()
				&& !authorizationService.isSupport()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.deleteAllStudents(counsellorId, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/counsellor/specific/students")
	public ResponseEntity<Map<String, Object>> deleteSelectedStudents(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestBody List<String> studentNumbers) throws NotFoundException, IOException {
		log.info("REST request to delete All students for counsellor id : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.deletespecificStudents(studentNumbers, counsellorId,
				institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/all/credentials/{studentNumber}")
	public ResponseEntity<Map<String, Object>> getStudentCredentails(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@PathVariable String studentNumber) throws NotFoundException, InterruptedException {
		log.info("REST request to get All student details for student number : {}", studentNumber);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.getStudentCredentails(studentNumber, institutionId,
				counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/all/shareactivity/{studentNumber}")
	public ResponseEntity<List<Map<String, Object>>> getStudentShareActivity(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@PathVariable String studentNumber) throws NotFoundException {
		log.info("REST request to get the share activity for student number : {}", studentNumber);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getStudentShareActivity(studentNumber, institutionId,
				counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/students/all/shareactivity")
	public ResponseEntity<List<Map<String, Object>>> getStudentsShareActivities(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId)
			throws NotFoundException, InterruptedException, BadRequestException {
		log.info("REST request to get the share activities for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getStudentShareActivities(institutionId,
				counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/counsellor/students/statistics")
	public ResponseEntity<List<Map<String, Object>>> getCounsellorStudentsStatistics(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId) {
		log.info("REST request to get the statistics for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getStudentsStatisticsByCounsellor(institutionId,
				counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/counsellor/enrollmentfastpass/report")
	public ResponseEntity<List<Map<String, Object>>> getEnrollemntFastPass(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "studentNumber", required = false) String studentNumber) {
		log.info("REST request to get enrollment fast pass report for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getEnrollemntFastPassStatistics(studentNumber,
				institutionId, counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/counsellor/export")
	public ResponseEntity<List<Map<String, Object>>> exportAllStudents(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId) throws IOException {
		log.info("REST request to get enrollment fast pass report for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getAllStudentData(institutionId, counsellorId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/counsellor/jobstatus/report/export")
	public ResponseEntity<Map<String, String>> exportJobStatusReport(
			@RequestParam(value = "counsellorId", required = false) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId) throws IOException {
		log.info("REST request to get job status report for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, String> response = administrationService.getJobStatusReportByCounsellorId(counsellorId,
				institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/counsellor/consent/status/report/export")
	public ResponseEntity<Map<String, String>> exportConsentRequestReport(
			@RequestParam(value = "counsellorId", required = true) Long counsellorId,
			@RequestParam(value = "institutionId", required = true) Long institutionId) throws IOException {
		log.info("REST request to get job status report for counsellor : {}", counsellorId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, String> response = administrationService
				.getEducationalOpportunityStatusReportByCounsellorId(counsellorId, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/edready/certificates/{studentNumber}")
	public ResponseEntity<Map<String, Object>> getEdreadyStudentCredentails(
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@PathVariable String studentNumber) throws NotFoundException {
		log.info("REST request to get All Edready certificate details for student number : {}", studentNumber);
		if (!authorizationService.isNROCadmin()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.getEdreadyStudentCertificates(studentNumber,
				institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/edready/certificates/shareactivity/{studentNumber}")
	public ResponseEntity<List<Map<String, Object>>> getEdreadyStudentShareActivity(
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@PathVariable String studentNumber) throws NotFoundException {
		log.info("REST request to get the share activity for student number : {}", studentNumber);
		if (!authorizationService.isNROCadmin()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService
				.getEdreadyStudentCertificateShareActivity(studentNumber, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/load/all/counsellor/students")
	public ResponseEntity<Map<String, Object>> loadAllCounselorBelongedStudents(
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "userId", required = true) Long userId) throws IOException, NotFoundException {
		log.info("REST request to import all the currently enrolled students belongs to institution id : {}",
				institutionId);
		if (!authorizationService.isSupport() && !authorizationService.isCounsellor()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService.loadAllCounselorBelongedStudents(institutionId, userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/institution/counsellor/report")
	public ResponseEntity<List<Map<String, Object>>> loadAllCounselorBelongedStudents(
			@RequestParam(value = "institutionId", required = true) Long institutionId) {
		log.info("REST request to get the counselor report for institution id : {}", institutionId);
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = administrationService.getCounsellorReportByInstitutionId(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/campus/specific/student/activity/report")
	public ResponseEntity<Map<String, Object>> loadAllCampusSpcificStudentActivityReport(
			@RequestBody MarketingUserAnalyticsDTO marketingAnalyticsDTO) throws IOException {
		log.info("REST request to get the campus specific student activity report for institution id : {}",
				marketingAnalyticsDTO);
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = administrationService
				.getCampusSpecificStudentActivityReport(marketingAnalyticsDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
