package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.GlStudentSurveyService;
import com.gl.platform.service.dto.GlStudentSurveyDTO;

@RestController
@RequestMapping("/api")
public class GlStudentSurveyResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlStudentSurveyService glStudentSurveyService;

	@PostMapping("/survey")
	public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody List<GlStudentSurveyDTO> glStudentSurveyDTOs) {
		log.info("REST request to create the survey for user : {}", glStudentSurveyDTOs.get(0).getUserId());
		Map<String, Object> response = glStudentSurveyService.createSurvey(glStudentSurveyDTOs);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/survey/{userId}")
	public ResponseEntity<Map<String, Object>> getSurvey(@PathVariable Long userId) {
		log.info("REST request to get the survey for user : {}", userId);
		Map<String, Object> response = glStudentSurveyService.getSurvey(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/survey/export")
	public ResponseEntity<Map<String, Object>> getSurveyExport(
			@RequestParam(value = "institutionId", required = false) Long institutionId,
			@RequestParam(value = "isShortDescription", required = false) Boolean isShortDescription)
			throws IOException {
		log.info("REST request to get the survey export : {}", institutionId);
		Map<String, Object> response = glStudentSurveyService.getSurveyExport(institutionId, isShortDescription);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
