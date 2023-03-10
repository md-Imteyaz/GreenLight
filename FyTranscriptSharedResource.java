package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.FyTranscriptSharedService;
import com.gl.platform.service.dto.TranscriptSharedRequestDTO;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class FyTranscriptSharedResource {
	
private final Logger log = LoggerFactory.getLogger(FyTranscriptSharedResource.class);
	
	@Autowired
	private FyTranscriptSharedService  transcriptSharedService;
	
	
	@PostMapping("/share/transcript")
	@Timed
	public ResponseEntity<TranscriptSharedRequestDTO> shareTranscript(
			@RequestBody TranscriptSharedRequestDTO transcriptSharedRequestdto) throws NotFoundException, IOException, URISyntaxException, BadRequestException {
		log.debug("Request to save transcript share :{}", transcriptSharedRequestdto);
		transcriptSharedRequestdto = transcriptSharedService.saveCredentialShare(transcriptSharedRequestdto);
		
		return ResponseEntity.ok(transcriptSharedRequestdto);
	}

}
