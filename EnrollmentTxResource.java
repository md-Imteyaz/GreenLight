package com.gl.platform.web.rest;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.EnrollmentTexasService;
import com.gl.platform.service.dto.EnrollTxApplicantDTO;
import com.gl.platform.service.dto.EnrollTxRequestDTO;
import com.gl.platform.web.rest.errors.CertificateSharedFailedException;
import com.gl.platform.web.rest.errors.ResumeSharedFailedException;
import com.gl.platform.web.rest.errors.SarShareFailedException;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class EnrollmentTxResource {

	private final Logger log = LoggerFactory.getLogger(EnrollmentTxResource.class);

	@Autowired
	private EnrollmentTexasService enrollmenttxService;

	@GetMapping("/enrollTx/eligiblity/{userId}")
	public ResponseEntity<Map<String, Object>> getEnrollTexasEligibility(@PathVariable Long userId)
			throws NotFoundException {
		log.info("REST request to get the enrollment texas eligibility for user id : {}", userId);
		Map<String, Object> response = enrollmenttxService.getEnrollTxEligibility(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/enrollTx/institutions/{userId}")
	public ResponseEntity<List<Map<String, Object>>> getEnrollTexasInstitutions(@PathVariable Long userId)
			throws IOException {
		log.info("REST request to get the enrollment texas institutions for user id : {}", userId);
		List<Map<String, Object>> response = enrollmenttxService.getEnrollTxAppliedDetails(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/apply/enrollTx/institutions")
	public ResponseEntity<List<EnrollTxApplicantDTO>> applyEnrollTexasInstitutions(
			@RequestBody List<EnrollTxRequestDTO> enrollTxRequestsDTO)
			throws IOException, NotFoundException, BadRequestException, URISyntaxException, ResumeSharedFailedException,
			SarShareFailedException, CertificateSharedFailedException {
		log.info("REST request to apply the enrollment texas with details : {}", enrollTxRequestsDTO);
		List<EnrollTxApplicantDTO> response = enrollmenttxService.applyEnrollTx(enrollTxRequestsDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/multishares/{institutionId}")
	public ResponseEntity<List<Map<String, Object>>> getGroupedShares(@PathVariable Long institutionId)
			throws IOException {
		log.info("REST request to get the enrollment texas eligibility for user id : {}", institutionId);
		List<Map<String, Object>> response = enrollmenttxService.getMultiShareList(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
