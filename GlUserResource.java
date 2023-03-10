package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.GlUser;
import com.gl.platform.repository.UserRepository;
import com.gl.platform.security.AuthoritiesConstants;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlUserQueryService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.dto.GlUserCriteria;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.util.RandomUtil;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.InvalidPasswordException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.google.zxing.WriterException;

import javassist.NotFoundException;

/**
 * REST controller for managing GlUser.
 */
@RestController
@RequestMapping("/api")
public class GlUserResource {

	private final Logger log = LoggerFactory.getLogger(GlUserResource.class);

	private static final String ENTITY_NAME = GlUser.class.getSimpleName().toLowerCase();

	private final GlUserService glUserService;

	private final GlUserQueryService glUserQueryService;

	private final UserRepository userRepository;

	@Autowired
	private AuthorizationService authorizationService;

	public GlUserResource(GlUserService glUserService, GlUserQueryService glUserQueryService,
			UserRepository userRepository) {
		this.glUserService = glUserService;
		this.glUserQueryService = glUserQueryService;
		this.userRepository = userRepository;

	}
	/**
	 * PUT /gl-users : Updates an existing glUser.
	 *
	 * @param glUserDTO the glUserDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         glUserDTO, or with status 400 (Bad Request) if the glUserDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the glUserDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/gl-users")
	@Timed
	public ResponseEntity<?> updateGlUser(@Valid @RequestBody GlUserDTO glUserDTO) throws URISyntaxException {
		log.debug("REST request to update GlUser : {}", glUserDTO);
		if (glUserDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}

		if (!authorizationService.ownedByUserOnly(glUserDTO.getId()) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("You don't have permission to edit other user's information");
		}
		GlUserDTO glUserOldDTO = glUserService.findOne(glUserDTO.getId());

		if (glUserDTO.getJhiPassword() == null) {
			glUserDTO.setJhiPassword(glUserOldDTO.getJhiPassword());
		}

		if (!glUserOldDTO.getEmail().equalsIgnoreCase(glUserDTO.getEmail()) && !authorizationService.isSupport()) {
			
			
			userRepository.findOneByEmailIgnoreCase(glUserDTO.getEmail()).ifPresent(u -> {
				throw new EmailAlreadyUsedException();
			});
			String newEmail = glUserDTO.getEmail();
			glUserDTO.setEmail(glUserOldDTO.getEmail());
			glUserDTO.setNewEmail(newEmail);
			glUserDTO.setEmailVerificationKey(RandomUtil.generateActivationKey());
			glUserDTO.setEmailModifiedDate(Instant.now());
			glUserService.updateNewEmail(glUserDTO);
		}else if (!glUserOldDTO.getEmail().equalsIgnoreCase(glUserDTO.getEmail()) && authorizationService.isSupport()) {
			glUserDTO.setEmail(glUserDTO.getEmail());
		}
		glUserDTO.setLastChangeDate(Instant.now());
		glUserDTO.setCreatedDate(glUserOldDTO.getCreatedDate());
		if (glUserDTO.getCreatedUser() == null) {
			glUserDTO.setCreatedUser(glUserOldDTO.getCreatedUser());
		}

		GlUserDTO result = glUserService.saveUpdate(glUserDTO);

		result.setJhiPassword(null);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * PUT /gl-user/two-factor/{userId}/{auth}: changes the current user's password
	 *
	 * @param userId : is the login id of the gl user * @param auth : sends the
	 *               boolean value
	 * @throws InvalidPasswordException 400 (Bad Request) if the glUser Object is
	 *                                  not present
	 * 
	 */
	@PutMapping("/gl-user/two-factor/{userId}/{auth}")
	@Timed
	public ResponseEntity<?> changeUserTwoFactor(@PathVariable Long userId, @PathVariable Boolean auth) {
		Map<String, String> response = glUserService.setUserTwoFactor(userId, auth);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * GET /gl-users : get all the glUsers.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of glUsers in
	 *         body
	 */
	@GetMapping("/gl-users")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.GREENLIGHT_SUPER_ADMIN })
	public ResponseEntity<?> getAllGlUsers(GlUserCriteria criteria) {
		log.debug("REST request to get GlUsers by criteria: {}", criteria);
		List<GlUserDTO> page = glUserQueryService.findByCriteria(criteria);
		List<Object> response = glUserService.getListOfMapObject(page);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * GET /gl-users/:id : get the "id" glUser.
	 *
	 * @param id the id of the glUserDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the glUserDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/gl-users/{id}")
	@Timed
	public ResponseEntity<?> getGlUser(@PathVariable Long id) {
		log.debug("REST request to get GlUser : {}", id);

		GlUserDTO glUserDTO = glUserService.findOne(id);
		glUserDTO.setJhiPassword(null);

		return new ResponseEntity<>(glUserDTO, HttpStatus.OK);
	}

	/**
	 * DELETE /gl-users/:id : delete the "id" glUser.
	 *
	 * @param id the id of the glUserDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/gl-users/{id}")
	@Timed
	public ResponseEntity<Void> deleteGlUser(@PathVariable Long id) {
		log.debug("REST request to delete GlUser : {}", id);
		glUserService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	@GetMapping("/validatemail")
	@Timed
	public void validateEmail(@RequestParam(value = "key") String key) throws NotFoundException {
		glUserService.updateNewEmailWithKey(key);
	}

	@GetMapping("/forgotusername/{email}")
	@Timed
	public ResponseEntity<Void> forgotUsername(@PathVariable String email) throws NotFoundException {
		glUserService.forgotusername(email);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/g-auth/generate")
	@Timed
	public ResponseEntity<?> createGoolgeCredential() throws NotFoundException, WriterException, IOException {

		Map<String, Object> response = glUserService.getUserTwoFactOption();
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/two-fact/options")
	@Timed
	public ResponseEntity<?> getTwoFactOptions() throws WriterException, IOException {

		Map<String, Object> response = glUserService.getUserTwoFactOptionsForUser();
		return ResponseEntity.ok().body(response);
	}

	@PutMapping("/two-fact/options/{type}")
	@Timed
	public ResponseEntity<?> updateTwoFactorOption(@PathVariable String type)
			throws NotFoundException, WriterException, IOException {
		Map<String, Object> response = glUserService.setUserTwoFactorOptions(type);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
