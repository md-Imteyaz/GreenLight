package com.gl.platform.web.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.ShareStatusQueryService;
import com.gl.platform.service.ShareStatusService;
import com.gl.platform.service.dto.ShareStatusCriteria;
import com.gl.platform.service.dto.ShareStatusDTO;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for ShareStatus.
 */
@RestController
@RequestMapping("/api")
public class ShareStatusResource {
	
	private final Logger log = LoggerFactory.getLogger(ShareStatusResource.class);

	private final ShareStatusService shareStatusService;
	
	private final ShareStatusQueryService shareStatusQueryService;

	public ShareStatusResource(ShareStatusService shareStatusService, ShareStatusQueryService shareStatusQueryService) {
		this.shareStatusService = shareStatusService;
		this.shareStatusQueryService = shareStatusQueryService;
	}
	
	@PostMapping("/share-status")
	@Timed
	public ResponseEntity<ShareStatusDTO> createShareStatus(@RequestBody ShareStatusDTO shareStatusDTO){
		log.debug("Request to Create share status: {}", shareStatusDTO);
		ShareStatusDTO result = shareStatusService.save(shareStatusDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PutMapping("/share-status")
	@Timed
	public ResponseEntity<ShareStatusDTO> updateShareStatus(@RequestBody ShareStatusDTO shareStatusDTO){
		log.debug("Request to update share status : {}", shareStatusDTO);
		ShareStatusDTO result = shareStatusService.save(shareStatusDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@GetMapping("/share-status/{id}")
	@Timed
	public ResponseEntity<ShareStatusDTO> getShareStatusById(@PathVariable Long id){
		log.debug("Request to get share status by id : {}", id);
		Optional<ShareStatusDTO> shareStatusDTO = shareStatusService.findOne(id);
		return ResponseUtil.wrapOrNotFound(shareStatusDTO);
	}

	@GetMapping("/share-status")
	@Timed
	public ResponseEntity<?> getShareStatusByCriteria(ShareStatusCriteria criteria){
		log.debug("Request to get share status by criteria : {}", criteria);
		List<ShareStatusDTO> shareStatusDTOList = shareStatusQueryService.findByCriteria(criteria);
		return new ResponseEntity<>(shareStatusDTOList, HttpStatus.OK);
	}

}
