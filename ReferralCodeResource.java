package com.gl.platform.web.rest;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.ReferralCode;
import com.gl.platform.repository.JhiUserRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.ReferralCodeService;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.ReferalRegistrationDTO;
import com.gl.platform.service.dto.ReferralCodeCriteria;
import com.gl.platform.service.dto.ReferralCodeDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.EmailAndUsernameAlreadyUsedException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class ReferralCodeResource {

	private final Logger log = LoggerFactory.getLogger(ReferralCodeResource.class);

	private static final String ENTITY_NAME = ReferralCode.class.getSimpleName().toLowerCase();

	@Autowired
	private ReferralCodeService referralCodeService;

	@Autowired
	private GlUserService glUserService;

	@Autowired
	private JhiUserRepository userRepository;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/generate/codes")
	public ResponseEntity<?> generateReferralCodes(
			@RequestParam(value = "institutionId", required = true) Long institutionId,
			@RequestParam(value = "value", required = true) Integer value,
			@RequestParam(value = "generatedFor", required = true) String generatedFor,
			@RequestParam(value = "isDistributed", required = false) Boolean isDistributed,
			@RequestParam(value = "id", required = false) Long id) throws URISyntaxException {
		log.debug("REST request to generate referral codes for employer : {} ", institutionId);

		if (id != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		if (value == null) {
			throw new BadRequestAlertException("mandatory fields are missing", ENTITY_NAME, "fieldsMissing");
		}

		Map<String, Object> response = referralCodeService.save(institutionId, value, generatedFor, isDistributed);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/getcodes/{institutionId}")
	@Timed
	public ResponseEntity<?> getSudetnType(@PathVariable Long institutionId) throws BadRequestException {
		log.debug("Request to get list of referral codes with an institution id : {}", institutionId);
		if (institutionId == null) {
			throw new BadRequestException("Institution Id is null");
		}
		List<ReferralCodeDTO> response = referralCodeService.getLisinstitutionIdtCodes(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/referral/codes")
	@Timed
	public ResponseEntity<Page<ReferralCodeDTO>> getSudetnType(ReferralCodeCriteria criteria, Pageable pageable)
			throws BadRequestException {
		log.debug("Request to get list of referral codes with criteria : {}", criteria);
		if (criteria.getInstitutionId() == null) {
			throw new BadRequestException("Institution Id is null");
		}
		Page<ReferralCodeDTO> response = referralCodeService.getListCodes(criteria, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/exportall/referral/codes/{institutionId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getAllReferralCodes(@PathVariable Long institutionId)
			throws IOException {
		log.debug("Request to get list of referral codes for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = referralCodeService.getAllListCodes(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/validate/referal/{referralCode}")
	public ResponseEntity<?> validateReferralCode(@PathVariable String referralCode) throws BadRequestException {

		log.debug("REST request to validate the referral code : {}", referralCode);

		if (referralCode == null) {
			throw new BadRequestException("referral code is null");
		}

		Map<String, Object> response = referralCodeService.validateCode(referralCode);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/registar/referal")
	public ResponseEntity<?> registerReferalUser(@RequestBody ReferalRegistrationDTO referalDTO)
			throws NotFoundException, BadRequestException {

		if (referalDTO.getRefferalId() == null) {
			throw new BadRequestException("referal Id is null");
		}
		if (userRepository.findOneByLogin(referalDTO.getUsername().toLowerCase()).isPresent()
				&& userRepository.findOneByEmailIgnoreCase(referalDTO.getEmail()).isPresent()) {
			throw new EmailAndUsernameAlreadyUsedException();
		}
		userRepository.findOneByLogin(referalDTO.getUsername().toLowerCase()).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});
		userRepository.findOneByEmailIgnoreCase(referalDTO.getEmail()).ifPresent(u -> {
			throw new EmailAlreadyUsedException();
		});
		GlUserDTO glUser = glUserService.saveReferralUser(referalDTO);

		if (glUser.getId() != null) {
			referralCodeService.setStatusToReedem(referalDTO.getRefferalId(), glUser,
					referalDTO.getLastSchoolAttended());
		}
		return new ResponseEntity<>(glUser, HttpStatus.OK);
	}

	@GetMapping("/distribute/referal/{id}")
	public ResponseEntity<?> updateReferal(@PathVariable Long id) throws NotFoundException, BadRequestException {

		if (id == null) {
			throw new BadRequestException("referal Id is null");
		}

		ReferralCodeDTO ref = referralCodeService.distributeReferal(id);

		return new ResponseEntity<>(ref, HttpStatus.OK);
	}

}
