package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.gl.platform.repository.AwardRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.MarketPlaceSearchQueryService;
import com.gl.platform.service.MarketPlaceSearchService;
import com.gl.platform.service.dto.EmailCampaignRequestDTO;
import com.gl.platform.service.dto.MarketPlaceQualifiedCandidateDTO;
import com.gl.platform.service.dto.MarketPlaceQualifiedCandidatesDTO;
import com.gl.platform.service.dto.MarketPlaceSearchCriteria;
import com.gl.platform.service.dto.MarketPlaceSearchDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

/**
 * REST controller for managing Transcripts.
 */
@RestController
@RequestMapping("/api")
public class MarketPlaceSearchResource {

	private final Logger log = LoggerFactory.getLogger(MarketPlaceSearchResource.class);

	private static final String ENTITY_NAME = "marketplace_search";

	private final MarketPlaceSearchService marketPlaceSearchService;
	private final MarketPlaceSearchQueryService marketPlaceSearchQueryService;

	@Autowired
	private AwardRepository awardRepository;

	@Autowired
	private AuthorizationService authorizationService;

	public MarketPlaceSearchResource(MarketPlaceSearchService marketPlaceSearchService,
			MarketPlaceSearchQueryService marketPlaceSearchQueryService) {
		this.marketPlaceSearchService = marketPlaceSearchService;
		this.marketPlaceSearchQueryService = marketPlaceSearchQueryService;
	}

	@GetMapping("/certifications")
	@Timed
	public ResponseEntity<?> getCertificates() {
		log.debug("Rest Request to get the certifications list ");
		return ResponseEntity.ok().body(marketPlaceSearchService.getCertificates());
	}

	@GetMapping("/apcourses")
	@Timed
	public ResponseEntity<?> getAdvancesPlacementCourses() {
		log.debug("Rest Request list of advanced placement courses");

		return ResponseEntity.ok().body(marketPlaceSearchService.getAdvancePlacementCourses());

	}

	@GetMapping("/grauduatemajors")
	@Timed
	public ResponseEntity<?> getGraduateMajors() {
		log.debug("Rest Request to get list of graduate majors");

		return ResponseEntity.ok().body(marketPlaceSearchService.getGraduateMajors());

	}

	@GetMapping("/undergraduatemajors")
	@Timed
	public ResponseEntity<?> getUnderGraduateMajors() {
		log.debug("Rest Request to get list of under graduate majors");

		return ResponseEntity.ok().body(marketPlaceSearchService.getUnderGraduateMajors());

	}

	@GetMapping("/fieldofstudy")
	@Timed
	public ResponseEntity<?> getFosNames() {
		log.debug("Rest Request to get list of field of study");

		return ResponseEntity.ok().body(awardRepository.getAllFosNames());

	}

	/**
	 * POST /marketplacesearches : Create a new marketplace searchcriteria.
	 *
	 * @param marketplaceSearchCriteriaDTO the marketplaceSearchCriteriaDTO to
	 *                                     create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         marketplaceSearchCriteriaDTO, or with status 400 (Bad Request) if the
	 *         marketplace searchcriteria has already an ID
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws JsonProcessingException
	 * @throws NotFoundException 
	 */
	@PostMapping("/marketplacesearches")
	@Timed
	public ResponseEntity<MarketPlaceSearchDTO> createMarketPlaceSearch(
			@Valid @RequestBody MarketPlaceSearchDTO marketplaceSearchDTO)
			throws URISyntaxException, JsonProcessingException, NotFoundException {
		log.debug("REST request to save MarketPlaceSearchCriteria : {}", marketplaceSearchDTO);

		if (marketplaceSearchDTO.getId() != null) {
			throw new BadRequestAlertException("A new marketplace searchcriteria cannot already have an ID",
					ENTITY_NAME, "idexists");
		}

		MarketPlaceSearchDTO result = marketPlaceSearchService.save(marketplaceSearchDTO).get();

		return ResponseEntity.created(new URI("/api/marketsplacesearches/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /marketplacesearches : Updates an existing marketplace searchcriteria.
	 *
	 * @param marketplaceSearchCriteriaDTO the marketplaceSearchCriteriaDTO to
	 *                                     update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         marketplaceSearchCriteriaDTO, or with status 400 (Bad Request) if the
	 *         marketplaceSearchCriteriaDTO is not valid, or with status 500
	 *         (Internal Server Error) if the marketplaceSearchCriteriaDTO couldn't
	 *         be updated
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws JsonProcessingException
	 * @throws NotFoundException 
	 */
	@PutMapping("/marketplacesearches")
	@Timed
	public ResponseEntity<MarketPlaceSearchDTO> updateMarketPlaceSearch(
			@Valid @RequestBody MarketPlaceSearchDTO marketplaceSearchDTO)
			throws URISyntaxException, JsonProcessingException, NotFoundException {
		log.debug("REST request to update MarketplaceSearch : {}", marketplaceSearchDTO);
		if (marketplaceSearchDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}

		Optional<MarketPlaceSearchDTO> result = marketPlaceSearchService.save(marketplaceSearchDTO);

		if (result.isPresent()) {
			return ResponseEntity.ok()
					.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.get().getId().toString()))
					.body(result.get());
		} else {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
	}

	/**
	 * GET /marketplacesearches : Updates an existing marketplace searchcriteria.
	 *
	 * @param marketplaceSearchCriteriaDTO the marketplaceSearchCriteriaDTO to
	 *                                     update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         marketplaceSearchCriteriaDTO, or with status 400 (Bad Request) if the
	 *         marketplaceSearchCriteriaDTO is not valid, or with status 500
	 *         (Internal Server Error) if the marketplaceSearchCriteriaDTO couldn't
	 *         be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws NotFoundException 
	 */
	@GetMapping("/marketplacesearches/{id}/qualifiedcandidates")
	@Timed
	public ResponseEntity<MarketPlaceQualifiedCandidatesDTO> getQualifiedCandidates(@PathVariable Long id,
			@RequestParam boolean optIn, Pageable pageable) throws URISyntaxException, NotFoundException {

		log.debug("REST request to get MarketPlace Qualified Candidates by criteria: {}", id);

		MarketPlaceQualifiedCandidatesDTO candidates = marketPlaceSearchService.getQualifiedCandidates(id, pageable,
				optIn);
		return new ResponseEntity<>(candidates, HttpStatus.OK);

	}

	@GetMapping("/marketplacesearches/{instId}/exported")
	@Timed
	public ResponseEntity<Collection<MarketPlaceQualifiedCandidateDTO>> getDownloadedCandidates(
			@PathVariable Long instId) {

		log.debug("REST request to get MarketPlace Qualified Candidates by InstitutionId: {}", instId);

		Collection<MarketPlaceQualifiedCandidateDTO> candidates = marketPlaceSearchService
				.getDownloadedCandidates(instId);

		return new ResponseEntity<>(candidates, HttpStatus.OK);
	}

	/**
	 * GET /marketplacesearches/:id : get the "id" marketplacesearch.
	 *
	 * @param id the id of the marketplacesearcheDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         marketplacesearcheDTO, or with status 404 (Not Found)
	 * @throws JsonProcessingException
	 * @throws NotFoundException 
	 */
	@GetMapping("/marketplacesearches/{id}")
	@Timed
	public ResponseEntity<MarketPlaceSearchDTO> getMarketPlaceSearch(@PathVariable Long id)
			throws JsonProcessingException, NotFoundException {
		log.debug("REST request to get MarketPlaceSearch : {}", id);

		Optional<MarketPlaceSearchDTO> marketPlaceSearchDTO = marketPlaceSearchService.findOne(id);

		return ResponseUtil.wrapOrNotFound(marketPlaceSearchDTO);
	}

	/**
	 * DELETE /marketplacesearches/:id : Archive the "id" marketplacesearch.
	 *
	 * @param id the id of the marketplacesearcheDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         marketplacesearcheDTO, or with status 404 (Not Found)
	 * @throws JsonProcessingException
	 */
	@DeleteMapping("/marketplacesearches/{id}")
	@Timed
	public ResponseEntity<MarketPlaceSearchDTO> deleteMarketPlaceSearch(@PathVariable Long id) {
		log.debug("REST request to delete MarketPlaceSearch : {}", id);
		marketPlaceSearchService.deleteById(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	/**
	 * GET /marketplacesearches : Updates an existing marketplace searchcriteria.
	 *
	 * @param marketplaceSearchCriteriaDTO the marketplaceSearchCriteriaDTO to
	 *                                     update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         marketplaceSearchCriteriaDTO, or with status 400 (Bad Request) if the
	 *         marketplaceSearchCriteriaDTO is not valid, or with status 500
	 *         (Internal Server Error) if the marketplaceSearchCriteriaDTO couldn't
	 *         be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@GetMapping("/marketplacesearches")
	@Timed
	public ResponseEntity<List<MarketPlaceSearchDTO>> getMarketPlaceSearches(MarketPlaceSearchCriteria criteria)
			throws URISyntaxException {
		log.debug("REST request to get Addresses by criteria: {}", criteria);
		if (criteria.getInstitutionId() != null
				&& !authorizationService.ownedByInstitution(criteria.getInstitutionId().getEquals())) {
			throw new AccessDeniedException("not the owner");
		}

		StringFilter statusFilter = new StringFilter();

		List<String> status = new ArrayList<>();

		status.add("Active");
		status.add("Draft");
		statusFilter.setIn(status);
		criteria.setStatus(statusFilter);
		List<MarketPlaceSearchDTO> result = marketPlaceSearchQueryService.findByCriteria(criteria);
		return ResponseEntity.created(new URI("/api/marketplacesearches")).body(result);
	}

	@GetMapping("/marketplace/{institutionId}/sla")
	@Timed
	public ResponseEntity<Map<String, Object>> getMarketPlaceSLAdetails(@PathVariable Long institutionId)
			throws URISyntaxException {
		log.debug("REST request to get marketplace sla details by instId: {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException("not the owner");
		}
		Map<String, Object> result = marketPlaceSearchService.getSlaDetails(institutionId);

		return ResponseEntity.created(new URI("/api/marketplacesearches")).body(result);
	}

	@GetMapping("/consent/report/{criteriaId}")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getConsentReport(@PathVariable Long criteriaId)
			throws URISyntaxException {
		log.debug("REST request to get consent report details by criteria id : {}", criteriaId);
		List<Map<String, Object>> result = marketPlaceSearchService.getConsentReport(criteriaId);
		return ResponseEntity.created(new URI("/api/consent/report")).body(result);
	}

	@PutMapping("/marketplace/email/campaign/manager")
	@Timed
	public ResponseEntity<Map<String, Object>> updateEmailCampaignDetailsForCriteria(
			@RequestBody EmailCampaignRequestDTO emailCampaignRequestDTO) {
		log.debug("REST request to update MarketplaceSearch email content and subject for criterias : {}",
				emailCampaignRequestDTO.getCriteriaIds());
		if (!authorizationService.ownedByInstitution(emailCampaignRequestDTO.getInstitutionId())) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> result = marketPlaceSearchService.updateEmailContentForCriteria(emailCampaignRequestDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
