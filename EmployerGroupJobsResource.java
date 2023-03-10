package com.gl.platform.web.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gl.platform.domain.GlJobTemplateCsvPositioning;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.EmployerGroupJobsService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import io.swagger.annotations.ApiImplicitParam;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class EmployerGroupJobsResource {

	private final Logger log = LoggerFactory.getLogger(EmployerGroupJobsResource.class);

	@Autowired
	private EmployerGroupJobsService employJobService;

	@Autowired
	private AuthorizationService authorizationService;

	private static final String ENTITY_NAME = EmployerGroupJobsResource.class.getSimpleName().toLowerCase();

	@PostMapping("/jobs/template/upload")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<?> uploadJobsTemplate(@RequestParam("template") MultipartFile file,
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "userId", required = true) Long userId)
			throws URISyntaxException, IOException, BadRequestException {

		log.debug("REST request to upload jobs template in csv : {}", institutionId);

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (institutionId == null || file == null) {
			throw new BadRequestException("Please provide valid information");
		}

		File createFile = File.createTempFile("JobTemplate", ".csv");
		file.transferTo(createFile);
		createFile.deleteOnExit();

		Path myPath = Paths.get(createFile.getAbsolutePath());
		Map<String, String> response = new HashMap<>();

		try (BufferedReader br = Files.newBufferedReader(myPath, StandardCharsets.UTF_8)) {

			HeaderColumnNameMappingStrategy<GlJobTemplateCsvPositioning> strategy = new HeaderColumnNameMappingStrategy<>();
			strategy.setType(GlJobTemplateCsvPositioning.class);

			CsvToBean csvToBean = new CsvToBeanBuilder(br).withType(GlJobTemplateCsvPositioning.class)
					.withMappingStrategy(strategy).withIgnoreLeadingWhiteSpace(true).build();
			List<GlJobTemplateCsvPositioning> jobsTemplate = csvToBean.parse();

			response = employJobService.saveTemplate(jobsTemplate, institutionId, userId);
		}

		return ResponseEntity.created(new URI("/jobs/template/upload")).body(response);
	}

	@GetMapping("/employer/manage/jobs")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<?> getJobMontering(@RequestParam(value = "groupId", required = true) Long groupId,
			@RequestParam(value = "jobType", required = false) String jobType,
			@RequestParam(value = "employerId", required = false) Long employerId) {

		List<Map<String, Object>> response = employJobService.getJobsPosted(groupId, employerId, jobType);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/manage/jobs/summary")
	public ResponseEntity<?> getGroupJobSummary(@RequestParam(value = "groupId", required = true) Long groupId,
			@RequestParam(value = "jobType", required = false) String jobType,
			@RequestParam(value = "employerId", required = false) Long employerId) {

		List<Map<String, Object>> response = employJobService.getSummary(groupId, employerId, jobType);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/manage/jobs/applicants")
	public ResponseEntity<?> getGroupJobStatus(@RequestParam(value = "groupId", required = true) Long groupId,
			@RequestParam(value = "jobType", required = false) String jobType,
			@RequestParam(value = "employerId", required = false) Long employerId) throws IOException, NotFoundException {
		log.debug("REST request to get applicant status report by groupId: {}, jobType: {}, employerId:{}", groupId,jobType,employerId);
		List<Map<String, Object>> response = employJobService.getStatusReport(groupId, employerId, jobType);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/applicant/analytics")
	public ResponseEntity<?> getApplicantAnalytics(@RequestParam(value = "jobStatus", required = true) String jobStatus,
			@RequestParam(value = "zipCode", required = false) String zipCode,
			@RequestParam(value = "generatedFor", required = false) String generatedFor,
			@RequestParam(value = "ethnicity", required = false) String ethnicity,
			@RequestParam(value = "gender", required = false) String gender) {

		List<Map<String, Object>> response = employJobService.getAnalyticsOfApplicant(generatedFor, ethnicity, gender,
				zipCode, jobStatus);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/applicant/activity/report")
	public ResponseEntity<?> getApplicantActivityReport(@RequestParam(value = "institutionId", required = true) Long institutionId
			){

		List<Map<String, Object>> response = employJobService.getStudentActivityReport(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
