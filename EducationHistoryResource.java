package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.EducationHistory;
import com.gl.platform.service.EducationHistoryService;
import com.gl.platform.service.dto.EducationHistoryDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class EducationHistoryResource {
	
	private final Logger log = LoggerFactory.getLogger(EducationHistoryResource.class);
	
	private static final String ENTITY_NAME = EducationHistory.class.getSimpleName().toLowerCase();
	
	@Autowired
	private EducationHistoryService educationHistoryService;
	
	@GetMapping("/education/history/{userId}")
	@Timed
	public ResponseEntity<?> generateHistoryEntries(@PathVariable Long userId) 
			throws IOException, NotFoundException, URISyntaxException{
		log.debug("REST request to get educaiton history entries");
		List<EducationHistoryDTO> response = educationHistoryService.getEducationHistory(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@PostMapping("/education/history")
	@Timed
	public ResponseEntity<?> createEducationHistory(@RequestBody EducationHistoryDTO historyDTO) {
		log.debug("REST request to get educaiton history entries");
		if (historyDTO.getId() != null) {
			throw new BadRequestAlertException("A new history entry cannot already have an ID", ENTITY_NAME, "idexists");
		}
		EducationHistoryDTO response = educationHistoryService.save(historyDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PutMapping("/education/history")
	@Timed
	public ResponseEntity<?> updateEducationHistory(@RequestBody EducationHistoryDTO eduHistoryDTO) {
		log.debug("REST request to update education history entries");
		if(eduHistoryDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		
		EducationHistoryDTO response = educationHistoryService.save(eduHistoryDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
