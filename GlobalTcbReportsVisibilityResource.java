package com.gl.platform.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.domain.GlobalTcbReportsVisibility;
import com.gl.platform.service.GlobalTcbReportsVisibilityService;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class GlobalTcbReportsVisibilityResource {

	private final Logger log = LoggerFactory.getLogger(GlobalTcbReportsVisibilityResource.class);

	private static final String ENTITY_NAME = "globalTcbReportsVisibilityResource";

	@Autowired
	private GlobalTcbReportsVisibilityService globalTcbReportsVisivilityService;

	@GetMapping("/global/tcbreport/visibility")
	public ResponseEntity<GlobalTcbReportsVisibility> getGlobalTcbReportVisibility() {
		log.info("REST request to get the global tcb reports visibility");
		GlobalTcbReportsVisibility response = globalTcbReportsVisivilityService.getGlobalTcbreportVisibility();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/global/tcbreport/visibility")
	public ResponseEntity<GlobalTcbReportsVisibility> updateGlobalTcbReportVisibility(
			@RequestBody GlobalTcbReportsVisibility globalTcbReportsVisibility) throws BadRequestException {
		log.info("REST request to update the global tcb reports visibility : {}", globalTcbReportsVisibility);
		if (globalTcbReportsVisibility.getId() == null) {
			throw new BadRequestException("id should not be null in PUT call");
		}
		GlobalTcbReportsVisibility response = globalTcbReportsVisivilityService.save(globalTcbReportsVisibility);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
