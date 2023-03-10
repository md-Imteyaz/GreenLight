package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.CertificateUploadService;
import com.gl.platform.service.dto.CertificateCsvImportDTO;
import com.gl.platform.service.dto.CertificateUploadDTO;
import com.gl.platform.service.dto.EdreadyRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.swagger.annotations.ApiImplicitParam;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class CertificateUploadResource {

	private final Logger log = LoggerFactory.getLogger(CertificateUploadResource.class);

	private static final String ENTITY_NAME = "certificateUpload";

	@Autowired
	private CertificateUploadService certificateService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/upload/certificate")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<CertificateUploadDTO> uploadCertificate(@RequestParam("issuerName") String name,
			@RequestParam("userId") Long userId, @RequestParam("certificateName") String certificateName,
			@RequestParam(value = "message", required = false) String message,
			@RequestParam("files") MultipartFile file, @RequestParam(value = "id", required = false) Long id)
			throws URISyntaxException, IOException {

		log.debug("Rest request to upload certificate");

		if (id != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (file == null || name == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}
		log.debug("REST request to save certificates ");

		CertificateUploadDTO response = certificateService.uploadCertificate(file, name, certificateName, userId,
				message, id);

		return ResponseEntity.created(new URI("/api/upload/certificate/")).body(response);

	}

	@GetMapping("/{userId}/certificate")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Page<CertificateUploadDTO>> getStudentCertificate(@PathVariable Long userId,
			Pageable pageable) throws IOException {

		log.debug("REST request to get the certificates for user Id : {}", userId);
		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}
		Page<CertificateUploadDTO> response = certificateService.getUserCertificate(userId, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/upload/certificate/update")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<CertificateUploadDTO> updateCertificate(@RequestParam("issuerName") String name,
			@RequestParam("userId") Long userId, @RequestParam("certificateName") String certificateName,
			@RequestParam(value = "message", required = false) String message,
			@RequestParam("files") MultipartFile file, @RequestParam(value = "id", required = false) Long id)
			throws IOException {

		log.debug("Rest request to upload certificate");

		if (id == null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		if (file == null || name == null || userId == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}
		log.debug("REST request to update certificates ");

		CertificateUploadDTO response = certificateService.uploadCertificate(file, name, certificateName, userId,
				message, id);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/certificate/{id}")
	@Timed
	public ResponseEntity<CertificateUploadDTO> deleteStudentCertificate(@PathVariable Long id)
			throws NotFoundException {

		log.debug("REST request to delete certificate for id : {}", id);

		certificateService.deleteCertificate(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/import/certificates")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<List<CertificateCsvImportDTO>> bulkUploadCertificate(
			@RequestParam(value = "files", required = true) MultipartFile file,
			@RequestParam(value = "universityId", required = true) Long institutionId)
			throws URISyntaxException, IOException {
		log.debug("Rest request to upload bulk load certificate");
		List<CertificateCsvImportDTO> response = certificateService.uploadBulkLoadCertificates(file, institutionId);
		return ResponseEntity.created(new URI("/api/import/certificates")).body(response);

	}

	@PostMapping("/claim/certificate")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<CertificateUploadDTO> claimCertificateAndView(@RequestBody List<Long> certificateIds)
			throws IOException, NotFoundException {
		log.info("REST request to claim the certificate with certificateId : {}", certificateIds);
		CertificateUploadDTO response = !certificateIds.isEmpty() ? certificateService.claimCertificate(certificateIds)
				: new CertificateUploadDTO();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/claim/certificate/{certificateId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<CertificateUploadDTO> claimSingleCertificateAndView(@PathVariable Long certificateId)
			throws IOException, NotFoundException {
		log.info("REST request to claim the certificate with certificateId : {}", certificateId);
		CertificateUploadDTO response = certificateService.claimSingleCertificate(certificateId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/import/edready/certificates")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Map<String, Object>> uploadEdreadyCertificates(
			@RequestParam(value = "files", required = true) MultipartFile file,
			@RequestParam(value = "universityId", required = true) Long institutionId)
			throws IOException, BadRequestException {
		log.debug("Rest request to upload bulk load certificate");
		Map<String, Object> response = certificateService.provisionCertificates(file, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/student/register/v1")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Map<String, Object>> sendRedirectUrl(
			@RequestBody EdreadyRequestDTO edreadyRequestDTO) throws BadRequestException {
		log.debug("Rest request to register the student with request : {}", edreadyRequestDTO);
		Map<String, Object> response = certificateService.validateObject(edreadyRequestDTO);
		if (!response.isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		response = certificateService.createOrUpdateEdreadyStudentData(edreadyRequestDTO);
		if (response.containsKey("errorCode")) {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
