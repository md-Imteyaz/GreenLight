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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.SuperAdminService;
import com.gl.platform.service.util.GlConstraints;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiImplicitParam;

/**
 * REST controller for managing super admin.
 */
@RestController
@RequestMapping("/api/su-admin")
public class SuperAdminResource {

	@Autowired
	private SuperAdminService superAdminService;

	@Autowired
	private AuthorizationService authorizationService;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/statistics")
	@Timed
	public ResponseEntity<Object> getStatistics() {
		Map<String, Object> response = superAdminService.getstatistics();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/reports")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getTcbReportsWithCriteria(
			@RequestParam(value = "studentIdorEmail", required = false) String studentIdorEmail,
			@RequestParam(value = "institutionId", required = true) String institutionId,
			@RequestParam(value = "fromDate", required = true) Long fromDate,
			@RequestParam(value = "toDate", required = true) Long toDate,
			@RequestParam(value = "reportType", required = true) String reportType,
			@RequestParam(value = "registeredStatus", required = true) String registeredStatus) {
		log.info("REST request to get tcb reports for institution id : {}", institutionId);
		List<Map<String, Object>> response = superAdminService.getTcbReports(institutionId, fromDate, toDate,
				registeredStatus, reportType, studentIdorEmail);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/ss/reports")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getTcbSSReportsWithCriteria(
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "institutionId", required = false) Long institutionId,
			@RequestParam(value = "includeAll", required = false) boolean includeAll,
			@RequestParam(value = "haveTranscript", required = false) boolean haveTranscript

	) {
		log.info("REST request to get tcb ss reports : {}", type);
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException("not the owner");
		}
		List<Map<String, Object>> response = superAdminService.getTcbSSReports(type, startDate, endDate, institutionId,
				includeAll, haveTranscript);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/isd/reports")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getTcbReportsByInstitution() {
		log.info("REST request to get tcb isd reports for institutions");
		List<Map<String, Object>> response = superAdminService.getTcbReportsByInstitutions();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/global/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<Map<String, Object>> getGlobalTcbReports() {
		log.info("REST request to get tcb reports for institutions");
		Map<String, Object> response = superAdminService.getGlobalTcbReports();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/student/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getGlobalTcbReportsByStudent() {
		log.info("REST request to get tcb reports for institutions");
		List<Map<String, Object>> response = superAdminService.getGlobalTcbReportsByStudents();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/school/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getSchoolTcbReport(
			@RequestParam(value = "institutionId", required = true) Long institutionId) {
		log.info("REST request to get tcb school reports for institutionId: {}", institutionId);
		List<Map<String, Object>> response = superAdminService.getTcbSchoolReport(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/dashboard/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getSuperAdminDashboardReport() {
		log.info("REST request to get dashboard Metricsreports");
		List<Map<String, Object>> response = superAdminService.getDashboardReport();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/greenlight/weekly/status/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<Map<String, Object>> getGreenLightWeeklyStatusReport() {
		log.info("REST request to get dashboard Metricsreports");
		Map<String, Object> response = superAdminService.getGreenLightWeeklyStats();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/institutions")
	public ResponseEntity<List<Map<String, Object>>> getTcbInstitutions() {
		log.info("REST request to get all the tcb institutions");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		List<Map<String, Object>> response = superAdminService.getTcbInstitutionsList();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}