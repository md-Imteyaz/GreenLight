package com.gl.platform.web.rest;

import java.net.URI;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.GroupMembershipRequestsDTO;
import com.gl.platform.service.InstitutionRegistrationRequestService;
import com.gl.platform.service.dto.EmployerRegistartionRequestDTO;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class InstitutionRegisterRequestResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String ENTITY_NAME = EmployerRegistartionRequestDTO.class.getSimpleName().toLowerCase();

	@Autowired
	private InstitutionRegistrationRequestService instRegisterrequestService;

	@PostMapping("/employer/register/request")
	public ResponseEntity<EmployerRegistartionRequestDTO> createInstitutionRegisterRequest(
			@RequestBody EmployerRegistartionRequestDTO employerRequestDTO) throws URISyntaxException {
		log.info("REST request to create an employer reqgistration request : {}", employerRequestDTO);
		EmployerRegistartionRequestDTO result = instRegisterrequestService.registerEmployer(employerRequestDTO);
		return ResponseEntity.created(new URI("/api/employer/register/request/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/employer/register/requests")
	public ResponseEntity<List<EmployerRegistartionRequestDTO>> getAllEmployersReqisterrequests() {
		log.info("REST request to create an employer reqgistration request");
		List<EmployerRegistartionRequestDTO> result = instRegisterrequestService.getRequestedEmployers();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PutMapping("/employer/register/request/{status}")
	public ResponseEntity<Map<String, Object>> updateEmployerStatus(@PathVariable String status,
			@RequestBody GroupMembershipRequestsDTO groupMembershipRequestsDTO) throws NotFoundException {
		log.info("REST request to create an employer reqgistration request");
		Map<String, Object> result = instRegisterrequestService.updateEmployerStatus(status,
				groupMembershipRequestsDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
