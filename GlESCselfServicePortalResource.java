package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.service.GlEscSelfServicePortalService;
import com.gl.platform.service.dto.SFTPUserDetailsDTO;
import com.gl.platform.service.dto.SftpPublicKeyDTO;
import com.jcraft.jsch.JSchException;


@RestController
@RequestMapping("/api/sftp")
public class GlESCselfServicePortalResource {

	private final Logger log = LoggerFactory.getLogger(GlESCselfServicePortalResource.class);

	@Autowired
	private GlEscSelfServicePortalService selfServicePortalService;
	
	@PostMapping("/create-user")
	@Timed
	public ResponseEntity<Map<String,String>> generateSFTPuser(@RequestBody SFTPUserDetailsDTO creationDTO ) throws IOException, JSchException {
		log.info("Rest request to create new sftp user");
		Map<String,String> response =  selfServicePortalService.createUser(creationDTO);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	

	@GetMapping("/users")
	@Timed
	public ResponseEntity<Page<SFTPUserDetailsDTO>> getAllSftpUsers(@RequestParam(value = "institutionId", required = false)  Long institutionId, Pageable pageable) {
		log.debug("REST request to get sftp users list");
		return new ResponseEntity<>(selfServicePortalService.getEscSftpUser(institutionId,pageable), HttpStatus.OK);
	}
	
	
	@DeleteMapping("/users/remove/{id}")
	@Timed
	public ResponseEntity<SFTPUserDetailsDTO> getSudentType(@PathVariable Long id) {
		log.debug("REST request to get sftp users list");
		return new ResponseEntity<>(selfServicePortalService.removeSftpAccess(id), HttpStatus.OK);
	}
	@PostMapping("/add/publickey")
	@Timed
	public ResponseEntity<Map<String,String>> addPublickeyForSFTPuser(@RequestBody SftpPublicKeyDTO creationDTO ) throws IOException, JSchException {
		log.info("Rest request to create new sftp user");
		Map<String,String> response =  selfServicePortalService.addPublicKey(creationDTO);
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}