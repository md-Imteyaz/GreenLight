package com.gl.platform.web.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gl.platform.domain.IntendedMajor;
import com.gl.platform.service.HighSchoolTranscriptService;
import com.gl.platform.service.NSAPIService;
import com.gl.platform.service.dto.ScholarshipActivityDTO;
import com.gl.platform.service.dto.StatisticsRequestDTO;
import com.gl.platform.service.dto.nsapi.NSAPIRequestObject;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class ScholarshipResource {

	private final Logger log = LoggerFactory.getLogger(ScholarshipResource.class);

	@Autowired
	private NSAPIService nsapiService;

	@Autowired
	private HighSchoolTranscriptService highschoolTranscriptService;

	@GetMapping("/scholarsnapp/redirecturl")
	@Timed
	public ResponseEntity<?> getScholarsnapp() throws UnsupportedEncodingException {
		log.debug("REST request to get sar scholarsnapp :");
		Map<String, String> listResult = nsapiService.getScholarsnappRedirectUrl();
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@PostMapping("/scholarsnapp/profile")
	@Timed
	public ResponseEntity<?> getScholarsnappInfoAndStore(@RequestBody String code) {
		log.debug("REST request to import sar scholarsnapp :");
		Map<String, String> listResult = nsapiService.getScholarsnappProfileInfoAndStore(code);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@GetMapping("/scholarsnapp/profile")
	@Timed
	public ResponseEntity<?> getScholarsnappProfile() throws NotFoundException {
		log.debug("REST request to get sar scholarsnapp :");
		Map<String, Object> listResult = nsapiService.getScholarsnappProfileData();
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@PostMapping("/nsapi/scholarships")
	@Timed
	public ResponseEntity<?> getNSAPIDetails(@RequestBody NSAPIRequestObject criteria) throws JsonProcessingException {
		log.debug("REST request to get NSAPI scholarship Details using criteria:");
		Map<String, Object> listResult = nsapiService.getScholarShipsByUsing(criteria);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@GetMapping("/scholarship/criteria/{userId}")
	@Timed
	public ResponseEntity<?> getScholarshipCriteria(@PathVariable Long userId) throws NotFoundException, IOException {
		log.debug("REST request to get scholarship Criteria Details :");
		NSAPIRequestObject listResult = nsapiService.getRequestObject(userId);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@GetMapping("/nsapi/scholarships/{userId}")
	@Timed
	public ResponseEntity<?> getScholarships(@PathVariable Long userId) throws NotFoundException, IOException {
		log.debug("REST request to get scholarship Criteria Details :");
		Map<String, Object> listResult = nsapiService.getMyscholarships(userId);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@PostMapping("/scholarship/activity")
	@Timed
	public ResponseEntity<?> createScholarshipActivity(@RequestBody ScholarshipActivityDTO activity) {
		log.debug("REST request to create scholarship acitivity using: {}", activity);
		ScholarshipActivityDTO result = nsapiService.createActivity(activity);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/scholarship/analytics/report")
	@Timed
	public ResponseEntity<?> scholarshipActivityReport(
			@RequestParam(value = "sponsorName", required = false) String sponsorName,
			@RequestParam(value = "scholarshipName", required = false) String scholarshipName,
			@RequestParam(value = "institutionId", required = false) Long institutionId,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate) throws NotFoundException {
		log.debug("REST request to get scholarship acitivity report by logged in user type");
		Map<String, Object> result = nsapiService.getAnalyticsReport(institutionId, sponsorName, scholarshipName,
				fromDate, toDate);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping("/scholarship/analytics/campus/report")
	@Timed
	public ResponseEntity<?> scholarshipActivityReportByCampus(@RequestBody StatisticsRequestDTO statisTicsRequestDTO) {
		log.debug("REST request to get scholarship acitivity report by campus ");
		Map<String, Object> result = nsapiService.getAnalyticsReportByCampus(statisTicsRequestDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/scholarship/profile/{userId}")
	@Timed
	public ResponseEntity<?> getProfileDetailsForScholarshipCriteria(@PathVariable Long userId) {
		log.debug("REST request to get scholarship Criteria Details :");
		Map<String, Object> listResult = highschoolTranscriptService.getProfileDetails(userId);
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@GetMapping("/scholarship/providers")
	@Timed
	public ResponseEntity<?> getScholarshipProviders() {
		log.debug("REST request to get scholarship provid	er Details");
		List<Map<String, Object>> listResult = nsapiService.getScholarshipProviderDetails();
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

	@GetMapping("/scholarship/major")
	@Timed
	public ResponseEntity<?> getScholarshipMajorsList() {
		log.debug("REST request to get scholarship major drop down Details :");
		List<IntendedMajor> listResult = nsapiService.getMajorsList();
		return new ResponseEntity<>(listResult, HttpStatus.OK);
	}

}
