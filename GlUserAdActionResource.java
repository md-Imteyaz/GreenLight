package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.Map;

import javax.activity.InvalidActivityException;

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

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlUserAdActionService;
import com.gl.platform.service.dto.GlAdViewedStatisticsDTO;
import com.gl.platform.service.dto.GlUserAdActionRequestDTO;
import com.gl.platform.service.util.GlConstraints;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class GlUserAdActionResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlUserAdActionService glUserAdActionService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/viewed/ads")
	public ResponseEntity<Map<String, Object>> saveUserViewedAds(
			@RequestBody GlUserAdActionRequestDTO gluserAdActionRequestDTO) throws NotFoundException {
		log.info("REST request to save the user viewed ads for user id : {}", gluserAdActionRequestDTO.getUserId());
		if (!authorizationService.isStudent()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = glUserAdActionService.saveAdsViewedByUser(gluserAdActionRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/click/ad")
	public ResponseEntity<Map<String, Object>> saveUserClickededAds(
			@RequestBody GlUserAdActionRequestDTO gluserAdActionRequestDTO) throws NotFoundException {
		log.info("REST request to save the user viewed ads for user id : {}", gluserAdActionRequestDTO.getUserId());
		if (!authorizationService.isStudent()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = glUserAdActionService.saveAdClickedByUser(gluserAdActionRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/click/ad/statistics")
	public ResponseEntity<Page<GlAdViewedStatisticsDTO>> getUserClickededAdsForMarketing(
			@RequestBody GlUserAdActionRequestDTO gluserAdActionRequestDTO, Pageable pageable) {
		log.info("REST request to get the user viewed ad stats for marketing");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Page<GlAdViewedStatisticsDTO> response = glUserAdActionService.getAdClickedStatistics(pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/click/ad/statistics/{institutionId}")
	public ResponseEntity<Page<GlAdViewedStatisticsDTO>> getUserClickededAdsForInstitution(
			@PathVariable Long institutionId, @RequestParam(value = "adId", required = false) Long adId,
			@RequestParam(value = "filterBy", required = false) String filterBy, Pageable pageable)
			throws NotFoundException {
		log.info("REST request to get the user viewed ad stats for institution : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Page<GlAdViewedStatisticsDTO> response = glUserAdActionService.getAdClickedStatistics(institutionId, adId,
				filterBy, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/report/ad/statistics/{institutionId}")
	public ResponseEntity<Map<String, Object>> getUserClickededAdsReportForInstitution(@PathVariable Long institutionId,
			@RequestParam(value = "adId", required = false) Long adId)
			throws NotFoundException, IOException, BadRequestException {
		log.info("REST request to get the user viewed ad report stats for institution : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = glUserAdActionService.getAdClickedStatistics(institutionId, adId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/ad/statistics/count/{institutionId}")
	public ResponseEntity<Map<String, Object>> getUserClickededAndViewedAdsCountForInstitution(
			@PathVariable Long institutionId, @RequestParam(value = "adId", required = true) Long adId)
			throws InvalidActivityException {
		log.info("REST request to get the user viewed ad stats for institution : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = glUserAdActionService.getUniqueStudentsAdViewedAndClickedCount(adId,
				institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
