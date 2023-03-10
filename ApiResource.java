package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.TranscriptsService;
import com.gl.platform.service.dto.TranscriptVerification;
import com.gl.platform.service.dto.VerificationResult;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.google.common.hash.Hashing;

@RestController
@RequestMapping("/api/v1")
public class ApiResource {

	private final Logger log = LoggerFactory.getLogger(ApiResource.class);

	@Autowired
	private TranscriptsService transcriptsService;

	private static final String TRANSCRIPT_ENTITY_NAME = "transcripts";

	@PostMapping("/verifycredentials")
	@Timed
	public @ResponseBody ResponseEntity<VerificationResult> verifyCredentials(
			@RequestParam("files") MultipartFile files) throws URISyntaxException, ParseException {

		log.debug("Came to Transcript Upload Method");

		if (files == null) {
			throw new BadRequestAlertException(" Transcript File missing", TRANSCRIPT_ENTITY_NAME, "idnull");
		} else {
			log.debug("Input Parameters: files list:{}", files.getSize());
		}

		log.info("Files Came through : {}", files.getSize());

		TranscriptVerification transcriptVerification = new TranscriptVerification();

		VerificationResult verificationResult = new VerificationResult();

		try {

			String transcript = IOUtils.toString(files.getInputStream(), StandardCharsets.UTF_8);

			String transcriptSha256hex = Hashing.sha256().hashString(transcript, StandardCharsets.UTF_8).toString();
			transcriptVerification = transcriptsService.validateTranscript(transcriptSha256hex);

			verificationResult.setResult(transcriptVerification.isVerified());
			verificationResult.setCredentialType(transcriptVerification.getType());
			verificationResult.setIssuedBy(transcriptVerification.getUniversityName());
			verificationResult.setIssuedTo(
					transcriptVerification.getStudentFirstName() + " " + transcriptVerification.getStudentLastName());
			verificationResult.setRecipientEmail(transcriptVerification.getShareRecipientEmailAddress());
			verificationResult.setRecipientName(transcriptVerification.getShareRecipientName());
			verificationResult.setSharedDate(transcriptVerification.getShareDate().toString());

		} catch (IOException e) {
			log.error("Error occured access the file", e);
		}

		return ResponseEntity.created(new URI("/api/v1/verifyCredentials")).body(verificationResult);
	}
}
