package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.domain.GlStaff;
import com.gl.platform.domain.GlUser;
import com.gl.platform.domain.JhiUser;
import com.gl.platform.repository.GlUserRepository;
import com.gl.platform.repository.JhiUserRepository;
import com.gl.platform.service.EdreadyService;
import com.gl.platform.service.dto.GlStaffRegistrationDTO;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.edready.StudentAPIWithGroupsDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;

import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class EdreadyResource {

	private final Logger log = LoggerFactory.getLogger(EdreadyResource.class);

	@Autowired
	private EdreadyService edreadyService;

	@Autowired
	private JhiUserRepository userRepository;

	@Autowired
	private GlUserRepository glUserRepository;

	@GetMapping("/edready/courses/{userId}")
	@Timed
	public ResponseEntity<StudentAPIWithGroupsDTO> getAssessmentsFrom(@PathVariable Long userId)
			throws NotFoundException, IOException {
		return edreadyService.getStudentDetails(userId);
	}

	@GetMapping("/redirect/{userId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getRedirectUrl(@PathVariable Long userId)
			throws JsonProcessingException, NotFoundException {
		log.debug("REST request to get edreay redirect URL : {}", userId);
		Map<String, Object> response = edreadyService.getRedirectUrl(userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/course/catalog/{userId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getCourseCatalog(@PathVariable Long userId) {
		log.debug("REST request to get edreay redirect URL : {}", userId);
		Map<String, Object> response = edreadyService.getCourseCatalog(userId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/edready/staff/details")
	@Timed
	public ResponseEntity<GlStaff> getStaffDetails(@RequestParam Long institutionId, @RequestParam String lastName,
			@RequestParam String email) {
		log.debug("REST request to get edreay staff deatails with insId : {}, lastName : {} , email : {}",
				institutionId, lastName, email);
		Optional<GlStaff> response = edreadyService.getStaffDetailsBy(institutionId, lastName, email);
		if (response.isPresent() && response.get().getUser() != null) {
			Optional<GlUser> glUser = glUserRepository.findById(response.get().getUser().getId());
			if (glUser.isPresent()) {
				Optional<JhiUser> jhiUser = userRepository.findById(glUser.get().getUser().getId());
				if (jhiUser.isPresent()) {
					if (jhiUser.get().isActivated()) {
						throw new BadRequestAlertException("Already registered, please login.", "userManagement",
								"Already Registered");
					}
				}
			}
		}
		return ResponseUtil.wrapOrNotFound(response);
	}

	@PostMapping("/staff/registration")
	@Timed
	public ResponseEntity<GlUserDTO> getStaffDetails(@Valid @RequestBody GlStaffRegistrationDTO glStaff)
			throws NotFoundException {
		log.info("REST request to register staff registration");
		GlUserDTO glUser = null;
		if (userRepository.findOneByLogin(glStaff.getUserName().toLowerCase()).isPresent()) {
			throw new LoginAlreadyUsedException();
		} else if (userRepository.findOneByEmailIgnoreCase(glStaff.getEmail()).isPresent()) {
			throw new EmailAlreadyUsedException();
		} else {
			glUser = edreadyService.createEdreadyStaffProfile(glStaff);
		}
		return ResponseEntity.ok(glUser);
	}

	@PutMapping("/staff/registration")
	@Timed
	public ResponseEntity<GlUserDTO> updateStaffDetails(@Valid @RequestBody GlStaffRegistrationDTO glStaff)
			throws NotFoundException {
		log.info("REST request to update staff registration : {}", glStaff);
		GlUserDTO glUser = edreadyService.updateEdreadyStaffProfile(glStaff);
		return ResponseEntity.ok(glUser);
	}

	@GetMapping("/edready/staff/reports")
	@Timed
	public ResponseEntity<Map<String, Object>> getStaffReportsDetails() throws NotFoundException, IOException {
		log.debug("REST request to get edreay staff reports");
		Map<String, Object> response = edreadyService.getStaffDetailsBy();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/edready/cert/reports/{userId}")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getStudentCertStatusReport(@PathVariable Long userId,
			@RequestParam(value = "certificateIssued", required = true) Boolean certificateIssued)
			throws NotFoundException {
		log.info("REST request to get the student cert reports with staff user id : {}", userId);
		List<Map<String, Object>> response = edreadyService.getStudentcertStatus(userId, certificateIssued);
		return ResponseEntity.ok(response);
	}
}
