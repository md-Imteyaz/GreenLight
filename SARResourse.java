package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.SARService;
import com.gl.platform.service.dto.CredentialDTO;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class SARResourse {

	private final Logger log = LoggerFactory.getLogger(SARResourse.class);

	@Autowired
	private SARService sarService;

	@PostMapping("/sar/upload")
	@Timed
	public @ResponseBody ResponseEntity<Object> uploadSarFile(@RequestParam("userId") Long userId,
			@RequestParam("sarName") String sarName, @RequestParam("file") MultipartFile sarFile)
			throws IOException, NotFoundException {

		log.debug("REST Request to upload SAR file");
		CredentialDTO response = sarService.uploadSarFile(userId, sarFile, sarName);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/sar/{userId}")
	@Timed
	public ResponseEntity<?> getSarDetails(@PathVariable Long userId) {
		log.debug("REST request to get sar reports by userId: {}", userId);
		List<CredentialDTO> listResult = sarService.validateAndGetSarReports(userId);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@DeleteMapping("/sar/delete/{id}")
	public ResponseEntity<?> deleteSarFile(@PathVariable Long id) {

		log.debug("REST request to soft delete SAR reports by id : {}", id);
		Map<String, Object> response = sarService.activateOrDeactivate(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
