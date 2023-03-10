package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.activity.InvalidActivityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.TrainingInstitutionService;
import com.gl.platform.service.dto.GlStudentDTO;

import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class TrainingInstitutionResource {

	private final Logger log = LoggerFactory.getLogger(TrainingInstitutionResource.class);

	@Autowired
	private TrainingInstitutionService trainingInstService;

	@PostMapping("/import/demographic")
	public ResponseEntity<Map<String, Object>> loadData(
			@RequestParam(value = "files", required = true) MultipartFile studentFile,
			@RequestParam(value = "universityId", required = true) Long institutionId)
			throws IOException, NotFoundException {
		log.info("REST request to load the demographics data for institution : {}", institutionId);
		Map<String, Object> response = trainingInstService.loadStudentFile(studentFile, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/training/student")
	@Timed
	public ResponseEntity<GlStudentDTO> getTrainingStudent(@RequestParam("studentId") String studentId,
			@RequestParam("email") String email, @RequestParam("lastName") String lastName,
			@RequestParam("institutionId") Long institutionId, @RequestParam("type") String type)
			throws InvalidActivityException, NotFoundException {

		if (studentId == null || email == null || institutionId == null || lastName == null || type == null) {
			throw new InvalidActivityException("Provide all the valid details");
		}
		log.debug("REST request to get an training student with id : {}", studentId);

		Optional<GlStudentDTO> response = trainingInstService.getTrainingStudent(studentId, email, lastName,
				institutionId, type);

		return ResponseUtil.wrapOrNotFound(response);
	}
}
