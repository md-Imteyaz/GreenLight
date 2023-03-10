package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.TcbStudentSurveyService;
import com.gl.platform.service.dto.TcbStudentSurveyDTO;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class TcbStudentSurveyResource {

	private final Logger log = LoggerFactory.getLogger(TcbStudentSurveyResource.class);

	private static final String ENTITY_NAME = "tcbStudentSurvey";

	@Autowired
	private TcbStudentSurveyService tcbStudentSurveyService;

	@PostMapping("/tcb/student/survey")
	public ResponseEntity<TcbStudentSurveyDTO> createTcbStudentSurvey(
			@RequestBody TcbStudentSurveyDTO tcbStudentSurveyDTO)
			throws BadRequestException, NotFoundException, URISyntaxException {
		log.info("REST request to save the tcb student survey : {}", tcbStudentSurveyDTO);
		if (tcbStudentSurveyDTO.getId() != null) {
			throw new BadRequestException("id must be null in POST call");
		}
		TcbStudentSurveyDTO response = tcbStudentSurveyService.save(tcbStudentSurveyDTO);

		return ResponseEntity.created(new URI("/api/tcb/student/survey" + response.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, response.getId().toString())).body(response);
	}

	@PutMapping("/tcb/student/survey")
	public ResponseEntity<TcbStudentSurveyDTO> updateTcbStudentSurvey(
			@RequestBody TcbStudentSurveyDTO tcbStudentSurveyDTO) throws BadRequestException, NotFoundException {
		log.info("REST request to update the tcb student survey : {}", tcbStudentSurveyDTO);
		if (tcbStudentSurveyDTO.getId() == null) {
			throw new BadRequestException("id must not be null in PUT call");
		}
		TcbStudentSurveyDTO response = tcbStudentSurveyService.save(tcbStudentSurveyDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/student/survey/{userId}")
	public ResponseEntity<TcbStudentSurveyDTO> getTcbStudentSurvey(@PathVariable Long userId) throws NotFoundException {
		log.info("REST request to get the tcb student survey with userId : {}", userId);
		TcbStudentSurveyDTO response = tcbStudentSurveyService.getTcbStudentSurvey(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/tcb/survey/report")
	public ResponseEntity<List<Map<String, Object>>> getTcbSurveyReports(
			@RequestParam("institutionId") String institution,
			@RequestParam("prefersTranscript") String prefersTranscript) {
		log.info("REST request to get the tcb survey reports for institution : {}", institution);
		List<Map<String, Object>> response = tcbStudentSurveyService.getTcbSurveyReportsWithCriteria(institution,
				prefersTranscript);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
