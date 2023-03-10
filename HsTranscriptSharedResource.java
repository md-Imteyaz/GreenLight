package com.gl.platform.web.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.HsTranscriptSharedService;

@RestController
@RequestMapping("/api/hs")
public class HsTranscriptSharedResource {

	private final Logger log = LoggerFactory.getLogger(HsTranscriptSharedResource.class);
	@Autowired
	private HsTranscriptSharedService hsTranscriptSharedService;
	
	@GetMapping("/share/activity")
	@Timed
	public ResponseEntity<?> getAllShareActivity() {
		log.debug("Request to get share activity from parent:");

		List<Map<String, Object>> response = hsTranscriptSharedService.getShareActivityByParent();

		return ResponseEntity.ok(response);
	}
}
