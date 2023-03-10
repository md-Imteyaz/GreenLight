package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.BadgeShared;
import com.gl.platform.service.BadgeSharedService;
import com.gl.platform.service.ShareService;
import com.gl.platform.service.dto.BadgeShareRequestDTO;
import com.gl.platform.service.dto.BadgeSharedDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.BadgeShareFailedException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;


@RestController
@RequestMapping("/api")
public class BadgeSharedResource {
	
	private final Logger log = LoggerFactory.getLogger(BadgeSharedResource.class);
	
	private static final String ENTITY_NAME = BadgeShared.class.getSimpleName().toLowerCase();
	
	@Autowired
	private BadgeSharedService badgeSharedService;

	@Autowired
	private ShareService shareService;

	
	/**
	 * POST /badgeShared : Create a new badgeShared.
	 *
	 * @param badgeSharedDTO the badgeSharedDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         badgeSharedDTO, or with status 400 (Bad Request) if the badgeShared has already
	 *         an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws BadgeShareFailedException 
	 * @throws NotFoundException 
	 */
	@PostMapping("/badgeshared")
	@Timed
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT') or hasRole('ROLE_SUPPORT_USER')")
	public ResponseEntity<BadgeShareRequestDTO> createBadgeShared(@Valid @RequestBody BadgeShareRequestDTO badgeSharedDTO) throws URISyntaxException, BadgeShareFailedException, NotFoundException {
		log.debug("REST request to save BadgeShared : {}", badgeSharedDTO);
		if (badgeSharedDTO.getId() != null) {
			throw new BadRequestAlertException("A new badgeShared cannot already have an ID", ENTITY_NAME, "idexists");
		}
		BadgeShareRequestDTO result = badgeSharedService.save(badgeSharedDTO);
		return ResponseEntity.created(new URI("/api/badgeShared/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
	
	
	@GetMapping("/badgeshared/{id}")
	@Timed
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT') or hasRole('ROLE_SUPPORT_USER')")
	public ResponseEntity<BadgeShareRequestDTO> getBadgeShared(@PathVariable Long id){
		log.debug("REST request to get BadgeShared by id : {}", id);
		Optional<BadgeSharedDTO> badgeshare = badgeSharedService.findOne(id);
		BadgeShareRequestDTO badgeShareRequestDTO = new BadgeShareRequestDTO();
		if(badgeshare.isPresent()) {
			badgeShareRequestDTO = shareService.getBadgeShareRequestDTOfrom(badgeshare.get());
		}
		return new ResponseEntity<>(badgeShareRequestDTO, HttpStatus.OK);
	}
	

}
