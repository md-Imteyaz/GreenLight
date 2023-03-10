package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sns.model.NotFoundException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gl.platform.service.ShareService;
import com.gl.platform.service.dto.AccomplishmentSharedRequestDTO;
import com.gl.platform.service.dto.CertificateSharedRequestDTO;
import com.gl.platform.service.dto.CredentialShareDTO;
import com.gl.platform.service.dto.OtherCredentialsRequestDTO;
import com.gl.platform.service.dto.RecomLetterShareRequestDTO;
import com.gl.platform.service.dto.ResumeSharedRequestDTO;
import com.gl.platform.service.dto.SelfUploadedTranscriptRequestDTO;
import com.gl.platform.service.dto.ShareDTO;
import com.gl.platform.service.dto.Shares;
import com.gl.platform.service.dto.sar.SarSharedRequestDTO;
import com.gl.platform.web.rest.errors.AccomplishmentShareFailedExcecption;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.CertificateSharedFailedException;
import com.gl.platform.web.rest.errors.RecommendationLetterShareFailedException;
import com.gl.platform.web.rest.errors.ResumeSharedFailedException;
import com.gl.platform.web.rest.errors.SarShareFailedException;
import com.gl.platform.web.rest.util.HeaderUtil;

/**
 * REST controller for Share.
 */
@RestController
@RequestMapping("/api")
public class ShareResource {

	private final Logger log = LoggerFactory.getLogger(ShareResource.class);

	@Autowired
	private final ShareService shareService;

	public ShareResource(ShareService shareService) {
		this.shareService = shareService;
	}

	@GetMapping("/share/user/{id}")
	@Timed
	public ResponseEntity<?> getShareDetailsByUserId(@PathVariable Long id) {
		log.debug("REST request to get share details by userId : {}", id);
		Map<String, Object> response = shareService.getShareDetailsByUserId(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/share/institution/{id}")
	@Timed
	public ResponseEntity<?> getShareDetailsByInstitutionId(@PathVariable Long id) {
		log.debug("REST request to get share details by institution by id : {}", id);
		List<Object> response = shareService.getSharedDetailsByInstitutionId(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/institutions/{id}/transcriptshares")
	@Timed
	public ResponseEntity<List<CredentialShareDTO>> getSharesByInstitutionId(@PathVariable Long id,
			@RequestParam(value = "searchTerm", required = false) String searchTerm,
			@RequestParam(value = "searchType", required = true) String searchType,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		log.debug(
				"REST request to search institution getSharesByInstitutionId(id = {},searchTerm = {},startDate = {},endDate = {})",
				id, searchTerm, startDate, endDate);
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		List<CredentialShareDTO> response = shareService.getCredentialSharesByInstitutionId(searchType, id, searchTerm,
				start, end);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/institutions/{id}/receivedcredentials")
	@Timed
	public ResponseEntity<Shares> getRecievedByInstitutionId(@PathVariable Long id,
			@RequestParam(value = "studentSearchTerm", required = false) String studentSearchTerm,
			@RequestParam(value = "transcriptType", required = false) String transcriptType,
			@RequestParam(value = "issuingInstitutionId", required = false) Long issuingInstitutionId,
			@RequestParam(value = "receivingInstitutionId", required = false) Long receivingInstitutionId,
			@RequestParam(value = "destinationInstitutionId", required = false) Long destinationInstitutionId,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {

		log.debug(
				"REST request to search institution getRecievedByInstitutionId(id = {},searchTerm = {},startDate = {},endDate = {})",
				id, studentSearchTerm, startDate, endDate);
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (receivingInstitutionId == null) {
			receivingInstitutionId = destinationInstitutionId;
		}

		Shares response = shareService.getReceivedCredentialStatsByInstitutionId(id, studentSearchTerm,
				issuingInstitutionId, receivingInstitutionId, transcriptType, start, end);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping("/institutions/{id}/credentialshares")
	@Timed
	public ResponseEntity<Shares> getCredentialSharesStatsByInstitutionId(@PathVariable Long id,
			@RequestParam(value = "studentSearchTerm", required = false) String studentSearchTerm,
			@RequestParam(value = "sentToType", required = false) String sentToType,
			@RequestParam(value = "sentToEmailAddress", required = false) String sentToEmailAddress,
			@RequestParam(value = "sentToInstitutionId", required = false) Long sentToInstitutionId,
			@RequestParam(value = "sentFromInstitutionId", required = false) Long sentFromInstitutionId,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate,
			@RequestParam(value = "individualStudentShareActivity", required = false) boolean individualStudentShareActivity) {

		// my transcript that is shared

		log.debug(
				"REST request to search institution getCredentialSharesStatsByInstitutionId(id = {},searchTerm = {},startDate = {},endDate = {})",
				id, studentSearchTerm, startDate, endDate);
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}
		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		Shares response = shareService.getCredentialSharesStatsByInstitutionId(id, studentSearchTerm,
				sentFromInstitutionId, sentToType, sentToInstitutionId, sentToEmailAddress, start, end,
				individualStudentShareActivity);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping("/institutions/{id}/certificateshares")
	@Timed
	public ResponseEntity<Shares> getCertificateSharesStatsByInstitutionId(@PathVariable Long id,
			@RequestParam(value = "studentSearchTerm", required = false) String studentSearchTerm,
			@RequestParam(value = "sentToType", required = false) String sentToType,
			@RequestParam(value = "sentToEmailAddress", required = false) String sentToEmailAddress,
			@RequestParam(value = "sentToInstitutionId", required = false) Long sentToInstitutionId,
			@RequestParam(value = "sentFromInstitutionId", required = false) Long sentFromInstitutionId,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate,
			@RequestParam(value = "individualStudentShareActivity", required = false) boolean individualStudentShareActivity) {

		// my certificate that is shared

		log.debug(
				"REST request to search institution getCertificateSharesStatsByInstitutionId(id = {},searchTerm = {},startDate = {},endDate = {})",
				id, studentSearchTerm, startDate, endDate);
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}
		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		Shares response = shareService.getCertificateSharesStatsByInstitutionId(id, studentSearchTerm,
				sentFromInstitutionId, sentToType, sentToInstitutionId, sentToEmailAddress, start, end,
				individualStudentShareActivity);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping("share/{id}")
	@Timed
	public ResponseEntity<?> getSharesByInstitutionId(@PathVariable Long id) {
		List<Map<String, Object>> response = shareService.getSharedDetailsByInstitutionIdOptimezed(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/share/limited/user/{userId}")
	@Timed
	public ResponseEntity<?> getTranscriptShare(@PathVariable Long userId) {
		log.debug("Request to get limited share details by userID");
		List<Map<String, Object>> result = shareService.getLimitedShareDetailsByUserId(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/recieved/archive")
	@Timed
	public ResponseEntity<?> archiveShares(@RequestBody List<Long> idList) {
		log.debug("REST request to archive shares : {}", idList);
		shareService.archiveShares(idList);
		return ResponseEntity.ok().headers(HeaderUtil.createAlert("Successfully archived", null)).build();

	}

	@GetMapping("/receiver/process/{id}")
	@Timed
	public ResponseEntity<?> processRecivedCredential(@PathVariable Long id) {

		log.debug("REST request to process share record with id : {}", id);
		Optional<ShareDTO> response = shareService.findOne(id);
		if (response.isPresent()) {
			response.get().setUser(null);
			response.get().setStatus("PROCESSED");
			if (response.get().getIsOpened() == null || !response.get().getIsOpened()) {
				response.get().setIsOpened(true);
				response.get().setDateOpened(Instant.now());
			}
			shareService.save(response.get());
		} else {
			throw new NotFoundException("Record not found");
		}
		return new ResponseEntity<>(response.get(), HttpStatus.OK);
	}

	@GetMapping("/receiver/viewed/{id}")
	@Timed
	public ResponseEntity<?> updateVieiwDate(@PathVariable Long id) {

		log.debug("REST request to process share record with id : {}", id);
		Optional<ShareDTO> response = shareService.findOne(id);
		if (response.isPresent()) {
			if ((response.get().getIsOpened() == null || !response.get().getIsOpened())) {
				response.get().setIsOpened(true);
				response.get().setDateOpened(Instant.now());
				shareService.save(response.get());
			}
		} else {
			throw new NotFoundException("Record not found");
		}
		return new ResponseEntity<>(response.get(), HttpStatus.OK);
	}

	@PostMapping("/reccommendationletter/share")
	@Timed
	public ResponseEntity<RecomLetterShareRequestDTO> createBadgeShared(
			@Valid @RequestBody RecomLetterShareRequestDTO sharedRequestDTO) throws URISyntaxException,
			NotFoundException, javassist.NotFoundException, RecommendationLetterShareFailedException {
		log.debug("REST request to save sharedRequestDTO : {}", sharedRequestDTO);
		if (sharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new recommendationLetterShared cannot already have an ID",
					"sharedRequestDTO", "idexists");
		}
		RecomLetterShareRequestDTO result = shareService.saveRecomLetterShare(sharedRequestDTO);
		return ResponseEntity.created(new URI("/api/reccommendationletter/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/certificate/share")
	@Timed
	public ResponseEntity<CertificateSharedRequestDTO> createCertifateShared(
			@Valid @RequestBody CertificateSharedRequestDTO certSharedRequestDTO)
			throws URISyntaxException, CertificateSharedFailedException, javassist.NotFoundException, IOException {

		log.debug("REST request to save certSharedRequestDTO : {}", certSharedRequestDTO);

		if (certSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new certificateShared cannot already have an ID",
					"certSharedRequestDTO", "idexists");
		}
		CertificateSharedRequestDTO result = shareService.saveCertificateShare(certSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/certificate/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/resume/share")
	@Timed
	public ResponseEntity<ResumeSharedRequestDTO> createResumeShared(
			@Valid @RequestBody ResumeSharedRequestDTO resumeSharedRequestDTO)
			throws URISyntaxException, ResumeSharedFailedException, javassist.NotFoundException {

		log.debug("REST request to save resumeSharedRequestDTO : {}", resumeSharedRequestDTO);

		if (resumeSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new resumeShared cannot already have an ID", "resumeSharedRequestDTO",
					"idexists");
		}
		ResumeSharedRequestDTO result = shareService.saveResumeShare(resumeSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/resume/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/other/credential/share")
	@Timed
	public ResponseEntity<OtherCredentialsRequestDTO> createOtherCredentialShared(
			@Valid @RequestBody OtherCredentialsRequestDTO otherCredSharedRequestDTO)
			throws URISyntaxException, ResumeSharedFailedException, javassist.NotFoundException {

		log.debug("REST request to save otherCredSharedRequestDTO : {}", otherCredSharedRequestDTO);

		if (otherCredSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new otherCredShared cannot already have an ID",
					"otherCredSharedRequestDTO", "idexists");
		}
		OtherCredentialsRequestDTO result = shareService.saveOtherCredentialsShare(otherCredSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/other/credential/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/selfupload/transcript/share")
	@Timed
	public ResponseEntity<SelfUploadedTranscriptRequestDTO> createTranscriptShared(
			@Valid @RequestBody SelfUploadedTranscriptRequestDTO selfUploadTranscriptDTO)
			throws URISyntaxException, ResumeSharedFailedException, javassist.NotFoundException {

		log.debug("REST request to save selfUploadTranscriptDTO : {}", selfUploadTranscriptDTO);

		if (selfUploadTranscriptDTO.getId() != null) {
			throw new BadRequestAlertException("A new transcriptShared cannot already have an ID",
					"selfUploadTranscriptDTO", "idexists");
		}
		SelfUploadedTranscriptRequestDTO result = shareService.saveTranscriptShare(selfUploadTranscriptDTO);
		return ResponseEntity.created(new URI("/api/selfupload/transcript/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/sar/share")
	@Timed
	public ResponseEntity<SarSharedRequestDTO> createSarShared(@Valid @RequestBody SarSharedRequestDTO sharedRequestDTO)
			throws URISyntaxException, SarShareFailedException, javassist.NotFoundException {
		log.debug("REST request to save sharedRequestDTO : {}", sharedRequestDTO);
		if (sharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new certificateShared cannot already have an ID", "sharedRequestDTO",
					"idexists");
		}
		SarSharedRequestDTO result = shareService.saveSarShare(sharedRequestDTO);
		return ResponseEntity.created(new URI("/api/sar/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@PostMapping("/accomplishment/share")
	@Timed
	public ResponseEntity<AccomplishmentSharedRequestDTO> createAccomplishmnentShared(
			@Valid @RequestBody AccomplishmentSharedRequestDTO accomplishmentSharedRequestDTO)
			throws URISyntaxException, javassist.NotFoundException, AccomplishmentShareFailedExcecption, IOException {
		log.debug("REST request to save sharedRequestDTO : {}", accomplishmentSharedRequestDTO);
		if (accomplishmentSharedRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new accomplishmentShared cannot already have an ID",
					"accomplishmentSharedRequestDTO", "idexists");
		}
		AccomplishmentSharedRequestDTO result = shareService.saveAccomplishmentShare(accomplishmentSharedRequestDTO);
		return ResponseEntity.created(new URI("/api/accomplishment/share" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert("", result.getId().toString())).body(result);
	}

	@GetMapping("/share/download")
	@Timed
	public ResponseEntity<?> getSharePdf(@RequestParam String type, @RequestParam String trackId)
			throws JsonParseException, JsonMappingException, IOException {
		log.debug("Request to get limited share details by userID");
		Map<String, Object> result = shareService.getDownloadablePdfsForShare(trackId, type);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("check/share/download")
	@Timed
	public ResponseEntity<?> checkEmailBlackList(@RequestParam String email) {
		log.debug("Request to check email black list: {}", email);
		Map<String, Object> result = shareService.checkEmailBlackList(email);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/download/shares/{institutionId}")
	@Timed
	public ResponseEntity<Map<String, Object>> downloadSharesZipFile(@PathVariable Long institutionId,
			@Valid @RequestBody List<Long> shareIds)
			throws javassist.NotFoundException, IOException, InterruptedException {
		log.debug("REST request to get shares zip file for institution id : {}", institutionId);
		Map<String, Object> result = shareService.processZipFileFromShareIds(shareIds, institutionId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

//	@PostMapping("/multiple/credential/share")
//	@Timed
//	public ResponseEntity<Map<String, Object>> createBulkShare(
//			@Valid @RequestBody MultipleCredentialSharedRequestDTO multipleCredentialSharedRequestDTO)
//			throws URISyntaxException, javassist.NotFoundException, AccomplishmentShareFailedExcecption, IOException,
//			ResumeSharedFailedException, CertificateSharedFailedException, RecommendationLetterShareFailedException,
//			BadgeShareFailedException, SarShareFailedException {
//		log.debug("REST request to save sharedRequestDTO : {}", multipleCredentialSharedRequestDTO);
//		Map<String, Object> result = shareService.saveMultipleCredentialsShare(multipleCredentialSharedRequestDTO);
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}

}
