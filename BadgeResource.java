package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.drew.imaging.png.PngProcessingException;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.BadgeService;
import com.gl.platform.service.dto.BadgeCriteria;
import com.gl.platform.service.dto.BadgeDTO;
import com.gl.platform.service.dto.CertificateUploadDTO;

import io.github.jhipster.service.filter.BooleanFilter;
import io.swagger.annotations.ApiImplicitParam;
import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class BadgeResource {

	private final Logger log = LoggerFactory.getLogger(BadgeResource.class);

	@Autowired
	private BadgeService badgeService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/upload/badges")
	@Timed
	public @ResponseBody ResponseEntity<Object> uploadBadges(@RequestParam("institute_id") Long instituteId,
			@RequestParam("file") MultipartFile badgefile) throws IOException {

		log.debug("REST Request to upload badges");
		BadgeDTO badgeDTO = badgeService.saveBadgeFlyingSaucer(badgefile.getInputStream(), instituteId);
		badgeDTO.setBlockchainHash(null);

		return new ResponseEntity<>(badgeDTO, HttpStatus.OK);
	}

	/**
	 * GET /badges/:id : get the "id" badge.
	 *
	 * @param id the id of the badgeDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the addressDTO,
	 *         or with status 404 (Not Found)
	 * @throws JSONException
	 * @throws NotFoundException
	 */
	@GetMapping("/badge/{id}")
	@Timed
	public ResponseEntity<Object> getBadge(@PathVariable Long id) throws JSONException, NotFoundException {
		log.debug("REST request to get Badge : {}", id);
		Map<String, Object> result = badgeService.getBadgeDetails(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/badge")
	@Timed
	public ResponseEntity<Object> getBadge(BadgeCriteria criteria) throws JSONException {
		log.debug("REST request to get badge by criteria: {}", criteria);

		BooleanFilter activeFilter = new BooleanFilter();
		activeFilter.setEquals(true);
		criteria.setActive(activeFilter);
		List<Object> listResult = badgeService.getBadge(criteria);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@PostMapping("/badge/upload")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Object> uploadStudentBadges(@RequestParam("userId") Long userId,
			@RequestParam("file") MultipartFile badgefile, @RequestParam("type") String type)
			throws IOException, PngProcessingException {

		log.debug("REST Request to upload badges");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}

		BadgeDTO badgeDTO = null;
		if ("png".equalsIgnoreCase(type)) {
			badgeDTO = badgeService.readMetadata(badgefile.getInputStream(), true, null);
		} else if ("json".equalsIgnoreCase(type)) {
			badgeDTO = badgeService.processBadgeJson(badgefile.getInputStream(), true, null);
		}
		return new ResponseEntity<>(badgeDTO, HttpStatus.OK);
	}

	@DeleteMapping("/badge/{id}")
	@Timed
	public ResponseEntity<CertificateUploadDTO> deleteUserBadgeBy(@PathVariable Long id) throws NotFoundException {

		log.debug("REST request to delete Badge for id : {}", id);
		badgeService.deleteBadge(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("/v1/badge/upload")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Object> uploadStudentBadgesFromService(
			@RequestParam("file") MultipartFile badgefile, @RequestParam("type") String type)
			throws IOException, PngProcessingException {

		log.debug("REST Request to upload badges");

		BadgeDTO badgeDTO = null;
		if ("png".equalsIgnoreCase(type)) {
			badgeDTO = badgeService.readMetadata(badgefile.getInputStream(), false, null);
		} else if ("json".equalsIgnoreCase(type)) {
			badgeDTO = badgeService.processBadgeJson(badgefile.getInputStream(), false, null);
		}
		return new ResponseEntity<>(badgeDTO, HttpStatus.OK);
	}

	@GetMapping("/checkforbadges/{userId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Object> checkForBadges(@PathVariable Long userId) throws JSONException, NotFoundException {
		log.debug("REST request to check for  badges by userID: {}", userId);

		List<BadgeDTO> listResult = badgeService.checkForBadges(userId);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@PostMapping("/import/badge/demographics")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Object> uploadStudentBadgesFromService1(
			@RequestParam("files") MultipartFile badgefile, @RequestParam("universityId") Long institutionId)
			throws IOException, BadRequestException, NotFoundException {

		log.debug("REST Request to upload badges");

		log.debug("Rest request to upload bulk badge demographics load");
		Map<String, Object> response = badgeService.loadDemographicsFromFile(badgefile, institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/claim/badge/{badgeId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<BadgeDTO> claimingBadgeAndView(@PathVariable Long badgeId)
			throws IOException, NotFoundException, PngProcessingException {
		log.info("REST request to claim the badge with id : {}", badgeId);

		BadgeDTO response = badgeService.claimBadgeWithId(badgeId);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/bulkcsv/badge/upload")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public @ResponseBody ResponseEntity<Object> bulkBadgeUpload(
			@RequestParam(value = "userId", required = true) Long userId,
			@RequestParam(value = "files", required = true) MultipartFile badgefile,
			@RequestParam(value = "universityId", required = true) Long institutionId) throws IOException {

		log.debug("REST Request to upload bulk csv for badges");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		Map<String, Object> respone = badgeService.uploadBulkBadgesFromCsv(badgefile, institutionId, userId);
		return new ResponseEntity<>(respone, HttpStatus.OK);
	}
}
