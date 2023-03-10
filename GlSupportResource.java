package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.GlSupportService;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import javassist.NotFoundException;

@RestController
@RequestMapping("api/support")
public class GlSupportResource {
	private final Logger log = LoggerFactory.getLogger(GlSupportResource.class);

	@Autowired
	private GlSupportService glSupportService;

	/**
	 * @param searchTerm
	 * @return
	 * @throws NotFoundException
	 * @throws IOException
	 */
	@GetMapping("/student/{searchTerm}")
	public ResponseEntity<Map<String, Object>> getAllDetailsByStudentId(@PathVariable String searchTerm)
			throws NotFoundException, IOException {
		log.debug("rest request to get details of {}", searchTerm);
		Map<String, Object> result = glSupportService.getStudentDetailsBySearchTerm(searchTerm);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * @param searchTerm
	 * @return
	 * @throws NotFoundException
	 * @throws IOException
	 */
	@GetMapping("/find")
	public ResponseEntity<Map<String, Object>> getAllDetailsByStudentIdAndEmail(
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "studentId", required = false) String studentId,
			@RequestParam(value = "institutionId", required = false) Long insId) throws NotFoundException, IOException {

		if (studentId != null && insId != null) {
			log.debug("rest request to get details of student with studentId {} and institutionId {}", studentId,
					insId);
			Map<String, Object> result = glSupportService.getStudentDetailsBy(studentId.trim(), insId);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		if (email != null) {
			Map<String, Object> result = glSupportService.getStudentDetailsBySearchTerm(email.trim());
			return new ResponseEntity<>(result, HttpStatus.OK);
		} 
		if(username !=null) {
			Map<String, Object> result = glSupportService.getStudentDetailsByUsername(username.trim());
			return new ResponseEntity<>(result, HttpStatus.OK);
			
		}
		throw new BadRequestAlertException("Badrequst, param values must not be null", "glStudent", "badrequest");
	}

	@GetMapping("/reset-password/{userId}")
	public ResponseEntity<Map<String, Object>> setStudentActive(@PathVariable Long userId) {
		log.debug("rest request to activate the student with userId {}", userId);
		Map<String, Object> result = glSupportService.resetPassword(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/activate/{userId}")
	public ResponseEntity<Map<String, Object>> setUserActive(@PathVariable Long userId) {
		log.debug("rest request to activate the student with userId {}", userId);
		Map<String, Object> result = glSupportService.activateUser(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
