package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.service.MarketPlaceInvitationQueryService;
import com.gl.platform.service.MarketPlaceInvitationService;
import com.gl.platform.service.dto.EducationOpportunityRequestDTO;
import com.gl.platform.service.dto.MarketPlaceInstitutionDownloadsDTO;
import com.gl.platform.service.dto.MarketPlaceInvitationDTO;
import com.gl.platform.service.dto.MarketPlaceInvitationsDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.swagger.annotations.ApiImplicitParam;
import javassist.NotFoundException;

/**
 * REST controller for managing Transcripts.
 */
@RestController
@RequestMapping("/api")
public class MarketPlaceInvitationResource {

	private final Logger log = LoggerFactory.getLogger(MarketPlaceInvitationResource.class);

	private static final String ENTITY_NAME = "marketplace";

	private final MarketPlaceInvitationService marketPlaceInvitationService;

	public MarketPlaceInvitationResource(MarketPlaceInvitationService marketPlaceInvitationService,
			MarketPlaceInvitationQueryService marketPlaceInvitationQueryService) {
		this.marketPlaceInvitationService = marketPlaceInvitationService;
	}

	/**
	 * POST /marketplaceinvitations : Create a new marketplace invitation.
	 *
	 * @param marketplaceInvitationDTO the marketplaceInvitationDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         marketplaceInvitationDTO, or with status 400 (Bad Request) if the
	 *         marketplaceinvitation has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException
	 * @throws NotFoundException
	 */
	@PostMapping("/marketplaceinvitations")
	@Timed
	public ResponseEntity<List<MarketPlaceInvitationDTO>> createMarketPlaceInvitation(
			@Valid @RequestBody MarketPlaceInvitationsDTO marketplaceInvitationsDTO)
			throws URISyntaxException, IOException, NotFoundException {
		log.debug("REST request to save MarketPlaceInvitationDTO : {}", marketplaceInvitationsDTO);

		List<MarketPlaceInvitationDTO> results = new ArrayList<>();

		if (marketplaceInvitationsDTO.isInviteAll() != null && marketplaceInvitationsDTO.isInviteAll()) {

			marketPlaceInvitationService.inviteAll(marketplaceInvitationsDTO);

		} else {
			for (MarketPlaceInvitationDTO invitation : marketplaceInvitationsDTO.getInvitations()) {
				if (invitation.getId() != null) {
					throw new BadRequestAlertException("A new marketplace invitation cannot already have an ID",
							ENTITY_NAME, "idexists");
				}

				if (invitation.getJobId() == null && invitation.getCriteriaId() == null) {
					throw new BadRequestAlertException("A new marketplace invitation must have an JobId or CriteriaId",
							ENTITY_NAME, "referenceIdNull");
				}

				if (invitation.getMessage() == null) {
					invitation.setMessage(marketplaceInvitationsDTO.getMessage());
				}

				MarketPlaceInvitationDTO result = marketPlaceInvitationService.save(invitation);
				if (result != null) {
					results.add(result);
				}
			}
		}
		return ResponseEntity.created(new URI("/api/marketplaceinvitations/"))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, "")).body(results);

	}

	/**
	 * PUT /marketplaceinvitations : Updates an existing marketplaceinvitation.
	 *
	 * @param marketplaceInvitationDTO the marketplaceInvitationDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         marketplaceInvitationDTO, or with status 400 (Bad Request) if the
	 *         marketplaceInvitationDTO is not valid, or with status 500 (Internal
	 *         Server Error) if the marketplaceInvitationDTO couldn't be updated
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws JsonProcessingException
	 * @throws NotFoundException
	 */
	@PutMapping("/marketplaceinvitations")
	@Timed
	public ResponseEntity<MarketPlaceInvitationDTO> updateMarketPlaceInvitation(
			@Valid @RequestBody MarketPlaceInvitationDTO marketplaceInvitationDTO)
			throws URISyntaxException, JsonProcessingException, NotFoundException {
		log.debug("REST request to update marketplaceInvitation : {}", marketplaceInvitationDTO);
		if (marketplaceInvitationDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}

		MarketPlaceInvitationDTO result = marketPlaceInvitationService.save(marketplaceInvitationDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
				.body(result);

	}

	/**
	 * GET /marketplaceinvitations : Updates an existing marketplaceinvitation.
	 *
	 * @param candidateId
	 * 
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@GetMapping("/marketplaceinvitations")
	@Timed
	public ResponseEntity<List<MarketPlaceInvitationDTO>> getMarketplaceInvitations(
			@RequestParam("candidateId") Long candidateId,
			@RequestParam(name = "invitationType", required = false) String invitationType) throws URISyntaxException {

		log.debug(
				"REST request to get MarketPlace Invitations getMarketplaceInvitations(candidateId = {},invitationType = {})",
				candidateId, invitationType);

		List<String> list = new ArrayList<>();

		list.add(MarketPlaceInvitationDTO.SENT);

		List<MarketPlaceInvitationDTO> invitatoins = marketPlaceInvitationService
				.findInvitationsByCandidateId(candidateId, invitationType, list);

		return ResponseEntity.created(new URI("/api/marketplaceinvitations")).body(invitatoins);
	}

	@PostMapping("/marketplaceinvitations/educationopportunity")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Map<String, Object>> sendEducationOpportunityEmail(
			@RequestBody EducationOpportunityRequestDTO educationOpportunityDTO)
			throws URISyntaxException, NotFoundException {

		log.debug("REST request to send MarketPlace Education Opportunity Invitations from institution : {}",
				educationOpportunityDTO.getInstitutionId());

		marketPlaceInvitationService.sendEducationOppurtunityEmail(educationOpportunityDTO);

		Map<String, Object> response = new HashMap<>();

		response.put("message", "Email sent successfully");

		return ResponseEntity.created(new URI("/marketplaceinvitations/educationopportunity")).body(response);

	}

	@PostMapping("/marketplace/downloads")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Map<String, String>> saveUserslist(@RequestBody MarketPlaceInstitutionDownloadsDTO mpidDTO)
			throws URISyntaxException, NotFoundException {
		log.debug("REST request to save MarketPlace downloaded users from institution : {}",
				mpidDTO.getInstitutionId());
		Map<String, String> response = marketPlaceInvitationService.saveDownloads(mpidDTO);
		return ResponseEntity.created(new URI("/api/marketplace/downloads")).body(response);
	}
}
