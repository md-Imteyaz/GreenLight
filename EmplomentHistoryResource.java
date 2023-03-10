package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.EmploymentHistory;
import com.gl.platform.service.EmploymentHistoryService;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

@RestController
@RequestMapping("/api")
public class EmplomentHistoryResource {

	private final Logger log = LoggerFactory.getLogger(EmplomentHistoryResource.class);

	private static final String ENTITY_NAME = EmplomentHistoryResource.class.getSimpleName().toLowerCase();

	@Autowired
	private EmploymentHistoryService empHistoryService;

	@PostMapping("/employment/history")
	public ResponseEntity<EmploymentHistory> createEmploymentHistory(@RequestBody EmploymentHistory empHistory) throws AccessDeniedException, URISyntaxException {
		log.debug("REST request to save employment history {}:", empHistory);

		if (empHistory.getId() != null) {
			throw new BadRequestAlertException("An employment history cannot already have an ID", ENTITY_NAME,
					"Idexists");
		}

		EmploymentHistory result = empHistoryService.save(empHistory);

		return ResponseEntity.created(new URI("/api/employment/history" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
	@GetMapping("/employment/history/{userId}")
	public ResponseEntity<List<EmploymentHistory>> getEmploymentHistory(@PathVariable long userId) {
		log.debug("REST request to get employment history with user ID", userId);

		List<EmploymentHistory> result = empHistoryService.getEmploymentHistory(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PutMapping("/employment/history")
	@Timed
	public ResponseEntity<?> updateEmploymentHistory(@RequestBody EmploymentHistory empHistory) throws AccessDeniedException {
		log.debug("REST request to update employment history entries");
		
		if(empHistory.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		
		EmploymentHistory response = empHistoryService.save(empHistory);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
