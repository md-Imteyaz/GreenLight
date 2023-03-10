package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AlertNotificationService;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.dto.InstitutionAlertNotificationDTO;
import com.gl.platform.service.dto.StudentAlertNotificationDTO;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class AlertNotificationResource {

	private final Logger log = LoggerFactory.getLogger(AlertNotificationResource.class);

	@Autowired
	private AlertNotificationService alertNotificationService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/institution/alert/notification")
	public ResponseEntity<InstitutionAlertNotificationDTO> createInstitutionAlertNotification(
			@RequestBody InstitutionAlertNotificationDTO instAlertNotificationDTO)
			throws NotFoundException, URISyntaxException, BadRequestException {
		log.debug("REST request to create the institution alert notification : {}", instAlertNotificationDTO);

		if (!authorizationService.ownedByInstitution(instAlertNotificationDTO.getInstitutionId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (instAlertNotificationDTO.getId() != null) {
			throw new BadRequestException("id must be null in POST");
		}

		InstitutionAlertNotificationDTO response = alertNotificationService
				.saveInstAlertNotification(instAlertNotificationDTO);
		return ResponseEntity.created(new URI("/api/institution/alert/notification")).body(response);
	}

	@PostMapping("/student/alert/notification")
	public ResponseEntity<StudentAlertNotificationDTO> createStudentAlertNotification(
			@RequestBody StudentAlertNotificationDTO studentAlertNotificationDTO)
			throws NotFoundException, URISyntaxException, BadRequestException {
		log.debug("REST request to create the stduent alert notification : {}", studentAlertNotificationDTO);

		if (!authorizationService.ownedByUser(studentAlertNotificationDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (studentAlertNotificationDTO.getId() != null) {
			throw new BadRequestException("id must be null in POST");
		}

		StudentAlertNotificationDTO response = alertNotificationService
				.saveStudentAlertNotification(studentAlertNotificationDTO);
		return ResponseEntity.created(new URI("/api/student/alert/notification")).body(response);
	}

	@GetMapping("/institution/alert/notification/{institutionId}")
	public ResponseEntity<InstitutionAlertNotificationDTO> getInstAlertNotification(@PathVariable Long institutionId)
			throws NotFoundException {
		log.debug("REST request to get the institution alert notification with institution id : {}", institutionId);

		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException("not the owner");
		}
		InstitutionAlertNotificationDTO response = alertNotificationService.getInstAlertNotification(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/student/alert/notification/{userId}")
	public ResponseEntity<StudentAlertNotificationDTO> getStudentAlertNotification(@PathVariable Long userId)
			throws NotFoundException {
		log.debug("REST request to get the stduent alert notification with user id : {}", userId);

		if (!authorizationService.ownedByUser(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		StudentAlertNotificationDTO response = alertNotificationService.getStudentAlertNotificationDTO(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/institution/alert/notification")
	public ResponseEntity<InstitutionAlertNotificationDTO> updateInstitutionAlertNotification(
			@RequestBody InstitutionAlertNotificationDTO instAlertNotificationDTO)
			throws NotFoundException, BadRequestException {
		log.debug("REST request to update the institution alert notification : {}", instAlertNotificationDTO);

		if (!authorizationService.ownedByInstitution(instAlertNotificationDTO.getInstitutionId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (instAlertNotificationDTO.getId() == null) {
			throw new BadRequestException("id must not be null in PUT");
		}

		InstitutionAlertNotificationDTO response = alertNotificationService
				.saveInstAlertNotification(instAlertNotificationDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/student/alert/notification")
	public ResponseEntity<StudentAlertNotificationDTO> updateStudentAlertNotification(
			@RequestBody StudentAlertNotificationDTO studentAlertNotificationDTO)
			throws NotFoundException, BadRequestException {
		log.debug("REST request to update the stduent alert notification : {}", studentAlertNotificationDTO);

		if (!authorizationService.ownedByUser(studentAlertNotificationDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		if (studentAlertNotificationDTO.getId() == null) {
			throw new BadRequestException("id must not be null in PUT");
		}

		StudentAlertNotificationDTO response = alertNotificationService
				.saveStudentAlertNotification(studentAlertNotificationDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
