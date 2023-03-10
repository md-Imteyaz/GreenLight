package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.RecommendationLetter;
import com.gl.platform.repository.JhiUserRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.RecommendationLetterService;
import com.gl.platform.service.dto.RecommendationLetterDTO;
import com.gl.platform.service.dto.RecommendationRequestDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;

import io.undertow.util.BadRequestException;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class RecommendationLetterResource {

	private final Logger log = LoggerFactory.getLogger(RecommendationLetterResource.class);

	private static final String ENTITY_NAME = RecommendationLetter.class.getSimpleName().toLowerCase();

	@Autowired
	private RecommendationLetterService recommendationLetterService;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private JhiUserRepository userRepository;

	@PostMapping("/upload/recommendationLetter")
	@Timed
	public @ResponseBody ResponseEntity<Map<String, String>> uploadResume(@RequestParam("key") String key,
			@RequestParam(value = "message", required = false) String message, @RequestParam("file") MultipartFile file)
			throws IOException, URISyntaxException, BadRequestException {

		log.debug("Came to recommendationLetter Upload Method");

		if (file == null || key == null) {
			throw new BadRequestAlertException("RecommendationLetter file missing", ENTITY_NAME, "keynull");
		}
		String fileName = file.getOriginalFilename();
		if (fileName != null) {
			fileName = fileName.trim();
			fileName = fileName.substring(fileName.length() - 4);
			if (!".pdf".contentEquals(fileName)) {
				throw new BadRequestException("Only pdf files are accepted to upload");
			}
		}
		Map<String, String> response = recommendationLetterService.uploadLetter(file, key, message);
		return ResponseEntity.created(new URI("/api/upload/recommendationLetter/")).body(response);
	}

	@PostMapping("/recommender/upload/recommendationLetter")
	@Timed
	public @ResponseBody ResponseEntity<Map<String, String>> uploadResume(@RequestParam("userId") Long userId,
			@RequestParam("key") String key, @RequestParam(value = "message", required = false) String message,
			@RequestParam("file") MultipartFile file) throws IOException, BadRequestException {

		log.debug("REST request to recommendationLetter Upload Method");

		if (!authorizationService.ownedByUserOnly(userId)) {
			throw new AccessDeniedException("not the owner");
		}
		String fileName = file.getOriginalFilename();
		if (fileName != null) {
			fileName = fileName.trim();
			fileName = fileName.substring(fileName.length() - 4);
			if (!".pdf".contentEquals(fileName)) {
				throw new BadRequestException("Only pdf files are accepted to upload");
			}
		}
		Map<String, String> response = recommendationLetterService.uploadLetter(file, key, message);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/recommendationLetter/{userId}")
	@Timed
	public ResponseEntity<List<RecommendationLetterDTO>> getRecommendationLetterDetailsByInstitutionId(
			@PathVariable Long userId) {
		log.debug("REST request to get recommendationLetter details by userID : {}", userId);

		if (!authorizationService.ownedByUserOnly(userId) && !authorizationService.isSupport()) {
			throw new AccessDeniedException("not the owner");
		}

		List<RecommendationLetterDTO> response = recommendationLetterService.findByUserId(userId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/recommender/details")
	public ResponseEntity<Map<String, Object>> getRecommenderDetailsByKey(@RequestParam("key") String reqKey)
			throws NotFoundException, InvalidActivityException {
		log.debug("REST request to get recommender details by request key : {}", reqKey);
		Map<String, Object> response = recommendationLetterService.getRecommenderDetails(reqKey);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/recommender/register")
	public ResponseEntity<Map<String, Object>> registerRecommenderWithDetails(
			@RequestParam(value = "key", required = true) String reqKey,
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password)
			throws NotFoundException, BadRequestException {
		log.debug("REST request to register the recommender by request key : {}", reqKey);
		userRepository.findOneByLogin(username).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});
		Map<String, Object> response = recommendationLetterService.createRecommender(reqKey, password, username);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/recommendations/{userId}")
	public ResponseEntity<Page<RecommendationRequestDTO>> getRecommendationsByUserId(@PathVariable Long userId,
			@RequestParam(value = "filterType", required = true) String filterType, Pageable pageable)
			throws NotFoundException {
		log.debug("REST request to get all the recommendations by userId : {}", userId);
		Page<RecommendationRequestDTO> response = recommendationLetterService.getRecommenderRequestsByUserId(userId,
				filterType, pageable);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
