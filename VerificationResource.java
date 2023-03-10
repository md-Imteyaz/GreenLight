package com.gl.platform.web.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gl.platform.service.VerificationService;
import com.gl.platform.service.dto.DegreeVerificationDTO;
import com.gl.platform.service.dto.edready.StudentAPIWithGroupsDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class VerificationResource {
	
	private final Logger log = LoggerFactory.getLogger(VerificationResource.class);
	@Autowired
	private VerificationService verificationService; 
	
	@PostMapping("/degree/verification")
	@Timed
	public ResponseEntity<?> verifyDegreeOfTheStudent(@RequestBody DegreeVerificationDTO requestDTO) throws IOException, ParseException {
		log.debug("REST request to get degree by studentDetials: {}", requestDTO);
		Map<String, Object> response = verificationService.findByStudentDetails(requestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@GetMapping("/payment")
	@Timed
	public ResponseEntity<?> paymentImplementation() throws IOException, ParseException {
		return verificationService.getAllLocations();
	}
	
	@GetMapping("/checkout")
	@Timed
	public ResponseEntity<?> checkoutAPI() throws IOException, ParseException {
		Map<String,Object> response =  verificationService.checkoutAPI(null, null, null, null);
				
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("verification/{checkoutId}")
	@Timed
	public ResponseEntity<?> getCertificate(@PathVariable String checkoutId) throws BadRequestException, JsonParseException, JsonMappingException, IOException {
		log.debug("REST request to get certificate through id : ",checkoutId);
		if(checkoutId == null) {
			throw new BadRequestException("Miisding the id");
		}
		Map<String,Object> response =  verificationService.getPdfUrl(checkoutId);
				
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
