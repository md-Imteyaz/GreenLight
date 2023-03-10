package com.gl.platform.web.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.StatisticsService;
import com.gl.platform.service.dto.StatisticsRequestDTO;
import com.gl.platform.service.util.GlConstraints;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api/analytics")
public class StatisticsResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private StatisticsService statisticsService;

	public static final String OWNER = "not the owner";

	@GetMapping("/dashboard/{institutionId}")
	public ResponseEntity<List<Map<String, Object>>> getCounsellorStudentsStatistics(@PathVariable Long institutionId)
			throws InterruptedException {
		log.info("REST request to get the statistics for institution : {}", institutionId);
		if (!authorizationService.isSiteAdmin()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = statisticsService.getDashBoardAnalytics(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/participation/report")
	public ResponseEntity<List<Map<String, Object>>> getStudentParticipationReport(
			@RequestParam(value = "institutionId", required = false) Long institutionId,
			@RequestParam(value = "childInstitutionId", required = false) Long childInstitutionId,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate,
			@RequestParam(value = "isCurrentlyEnrolled", required = false) Boolean isCurrentlyEnrolled) {
		log.info("REST request to get the stduent participation statistics for institution : {}", institutionId);
		if (!authorizationService.isMarketingUser() && !authorizationService.isCounsellor()) {
			throw new AccessDeniedException(OWNER);
		}
		isCurrentlyEnrolled = isCurrentlyEnrolled == null ? Boolean.FALSE : isCurrentlyEnrolled;
		List<Map<String, Object>> response = statisticsService.getStudentParticipationReport(institutionId,
				childInstitutionId, fromDate, toDate, isCurrentlyEnrolled);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/students/jobs/report")
	public ResponseEntity<List<Map<String, Object>>> getStudentsAppliedJobsReport(
			@RequestParam(value = "institutionId", required = false) String institutionId,
			@RequestParam(value = "childInstitutionId", required = false) String childInstitutionId,
			@RequestParam(value = "employerId", required = false) String employerId,
			@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate) {
		log.info("REST request to get the stduents jobs report statistics for institution : {}", institutionId);
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = statisticsService.getStudentsAppliedJobsReport(institutionId,
				childInstitutionId, employerId, fromDate, toDate, status);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/students/resgistartion/report")
	public ResponseEntity<Map<String, Object>> getStudentsRegistartionReport(
			@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.info("REST request to get the stdudents registartion report for institution : {}",
				statisTicsRequestDTO.getInstitutionId());
		if (!authorizationService.ownedByInstitution(Long.parseLong(statisTicsRequestDTO.getInstitutionId()))) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = statisticsService.getStudentsRegistrationStatusReport(statisTicsRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/overall/resgistration/report")
	public ResponseEntity<Map<String, Object>> getOverallStudentsRegistartionReport(
			@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.info("REST request to get the stdudents overall registartion report for institution : {}",
				statisTicsRequestDTO.getInstitutionId());
		if (!authorizationService.ownedByInstitution(Long.parseLong(statisTicsRequestDTO.getInstitutionId()))) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = statisticsService.getOverAllStudentsRegistrationReport(statisTicsRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/marketing/job/postings/report")
	public ResponseEntity<List<Map<String, Object>>> getJobPostingsReportMarketing(
			@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.info("REST request to get the stduents job postings report for marketing user");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = statisticsService.getJobPostingsReportForMarketing(statisTicsRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/marketing/applicant/job/status/report")
	public ResponseEntity<List<Map<String, Object>>> getApplicantJobStatusMarketing(
			@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.info("REST request to get the stduents job status report for marketing user");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = statisticsService
				.getApplicantJobStatusReportForMarketing(statisTicsRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/marketing/greenlight/job/analytics")
	public ResponseEntity<Map<String, Object>> getGreenLightJobAnalytics(
			@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.info("REST request to get the stduents job status report for marketing user");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		Map<String, Object> response = statisticsService.getGreenLightJobAnalytics(statisTicsRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/marketing/employer/demographics/report")
	public ResponseEntity<List<Map<String, Object>>> getEmployerDemographicsReport(
			@RequestParam(value = "postedJobsInGreenLight", required = true) Boolean postedJobsInGreenLight,
			@RequestParam(value = "employerId", required = true) String employerid,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate) throws NotFoundException {
		log.info("REST request to get all the employers demographics report");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(OWNER);
		}
		List<Map<String, Object>> response = statisticsService.getAllEmployersDemographics(postedJobsInGreenLight,
				employerid, startDate, endDate);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
