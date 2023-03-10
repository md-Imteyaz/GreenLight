package com.gl.platform.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.domain.FileUploadHistory;
import com.gl.platform.service.FileUploadHistoryService;

/**
 * REST controller for managing File Upload History.
 */
@RestController
@RequestMapping("/api")
public class FileUploadHistoryResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FileUploadHistoryService fileUploadHistoryService;

	@GetMapping("/file/upload/history/{institutionId}")
	public ResponseEntity<List<FileUploadHistory>> getSudentType(@PathVariable Long institutionId) {
		log.debug("REST request to get all the file upload history for institutio id : {}", institutionId);
		List<FileUploadHistory> response = fileUploadHistoryService.getFileUploadHistory(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
