package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.PromiseChecklistService;
import com.gl.platform.service.dto.PromiseChecklistDTO;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class PromiseChecklistResource {

	private final Logger log = LoggerFactory.getLogger(PromiseChecklistResource.class);

	@Autowired
	private PromiseChecklistService promiseChecklistServce;

	@PostMapping("/promise/checklist")
	public ResponseEntity<PromiseChecklistDTO> createPromiseChecklist(
			@RequestBody PromiseChecklistDTO promiseChecklistDTO) throws URISyntaxException, BadRequestException {

		log.info("REST request to create the promise checklist : {}", promiseChecklistDTO);
		if (promiseChecklistDTO.getId() != null) {
			throw new BadRequestException("id should be null in POST call");
		}
		PromiseChecklistDTO response = promiseChecklistServce.save(promiseChecklistDTO);

		return ResponseEntity.created(new URI("/api/create/promise/checklist")).body(response);
	}

	@GetMapping("/promise/checklist/{userId}")
	public ResponseEntity<PromiseChecklistDTO> getPromiseChecklist(@PathVariable Long userId) throws NotFoundException {
		log.info("REST request to get the promise checklist info with userId : {}", userId);
		PromiseChecklistDTO response = promiseChecklistServce.getPromiseChacklist(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/promise/checklist")
	public ResponseEntity<PromiseChecklistDTO> updatePromiseChecklist(
			@RequestBody PromiseChecklistDTO promiseChecklistDTO) throws URISyntaxException, BadRequestException {

		log.info("REST request to update the promise checklist : {}", promiseChecklistDTO);
		if (promiseChecklistDTO.getId() == null) {
			throw new BadRequestException("id should not be null in PUT call");
		}
		PromiseChecklistDTO response = promiseChecklistServce.save(promiseChecklistDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
