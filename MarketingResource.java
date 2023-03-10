package com.gl.platform.web.rest;

import java.io.IOException;
import java.time.LocalDate;
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
import com.gl.platform.repository.AwardRepository;
import com.gl.platform.repository.FourYearDegreeAwardRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.MarketingService;
import com.gl.platform.service.dto.MarketingUserAnalyticsDTO;
import com.gl.platform.service.dto.MarketplaceConsentCampaignCriteria;
import com.gl.platform.service.dto.MarketplaceConsentCampaignDTO;
import com.gl.platform.service.dto.StudentEmailCampaignResponseDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;

import io.swagger.annotations.ApiImplicitParam;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class MarketingResource {

	private final Logger log = LoggerFactory.getLogger(MarketingResource.class);

	@Autowired
	private MarketingService marketingService;

	@Autowired
	private AwardRepository awardRepository;

	@Autowired
	private FourYearDegreeAwardRepository fourYeaDegreeAwardRepo;

	@Autowired
	private AuthorizationService authorizationService;

	@GetMapping("/marketing/registrations")
	@Timed
	public ResponseEntity<?> getStudentsByUserId(@RequestParam(value = "fromInsId") Long fromInsId,
			@RequestParam(value = "toInsId", required = false) Long toInsId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "isRegistered") boolean isRegistered) {
		log.debug("REST request to get Registrations by institutionId {}", fromInsId);

		LocalDate dateFrom = startDate != null ? GlConstraints.localDateFunction.apply(startDate) : null;
		LocalDate dateTo = endDate != null ? GlConstraints.localDateFunction.apply(endDate) : null;

		List<Map<String, Object>> resultStudents = marketingService.getStudentBy(fromInsId, toInsId, dateFrom, dateTo,
				isRegistered);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/marketing/student/emails/{instId}")
	@Timed
	public ResponseEntity<?> getStudentEmails(@PathVariable Long instId) {
		log.debug("REST request to get student Emails by institutionId {}", instId);

		List<Map<String, Object>> resultStudents = marketingService.getStudentEmailsByInstitution(instId);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/dropdown/associates")
	public ResponseEntity<?> getAssociatesDropdown() {
		log.debug("REST request to get associates dropdown");

		List<String> response = awardRepository.findByDegree();

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/dropdown/honors")
	public ResponseEntity<?> getHonors() {
		log.debug("REST request to get honors dropdown");

		List<String> response = fourYeaDegreeAwardRepo.getAllHonors();

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/marketing/analytics/report")
	@Timed
	public ResponseEntity<?> getAnalyticsReport(@RequestParam(value = "institutionId", required = false) String institutionId,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate) throws NotFoundException {
		

		LocalDate dateFrom = startDate != null ? GlConstraints.localDateFunction.apply(startDate) : null;
		LocalDate dateTo = endDate != null ? GlConstraints.localDateFunction.apply(endDate) : null;

		log.debug("REST request to get Analytics report by institutionId {}", institutionId);

		List<Map<String, Object>> resultStudents = marketingService.getAnalyticsReports(institutionId,dateFrom,dateTo);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/marketing/analytics/dashboard")
	@Timed
	public ResponseEntity<?> getAnalyticsReport() {
		log.debug("REST request to get marketing dashboard report");

		Map<String, Object> resultStudents = marketingService.getDashboardReport();
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@PostMapping("/marketing/analytics/users")
	@Timed
	public ResponseEntity<?> getUserAnalyticsReport(@RequestBody MarketingUserAnalyticsDTO dto) {
		log.debug("REST request to get marketing users report");

		Object resultStudents = marketingService.getUserAnalyticsReport(dto);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/marketing/global/analytics/users")
	@Timed
	public ResponseEntity<?> getUserGlobalAnalyticsReport(@RequestParam(value = "fromDate", required = false) String startDate,
			@RequestParam(value = "toDate", required = false) String endDate) {
		log.debug("REST request to get marketing users report(api/marketing/global/analytics/users) by startDate: {} , endDate: {}",startDate,endDate);
		
		LocalDate dateFrom = startDate != null ? GlConstraints.localDateFunction.apply(startDate) : null;
		LocalDate dateTo = endDate != null ? GlConstraints.localDateFunction.apply(endDate) : null;
		List<Map<String, Object>> resultStudents = marketingService.getUserGlobalAnalyticsReport(dateFrom,dateTo);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}
	
	@GetMapping("/marketing/counselors/analytics")
	@Timed
	public ResponseEntity<?> getCouncellorsAnalyticsReport() {
		log.debug("REST request to get marketing councellors report(api/marketing/councellors/analytics)");
		
		List<Map<String, Object>> resultStudents = marketingService.getCouncellorsAnalyticsReport();
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@PostMapping("/marketing/user/login/activity")
	@Timed
	public ResponseEntity<?> getUserLoginActivityReport(@RequestBody MarketingUserAnalyticsDTO dto) {
		log.debug("REST request to get  user login activity report");

		List<Map<String, Object>> resultStudents = marketingService.getUserLoginAnalyticsReport(dto);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/marketing/institution/student/activity")
	@Timed
	public ResponseEntity<?> studentActivityByInstitution() {
		log.debug("REST request to get  user login activity report");

		List<Map<String, Object>> resultStudents = marketingService.getStudentActivityByInstitution();
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/marketplace/consent/campaign")
	@Timed
	public ResponseEntity<Page<MarketplaceConsentCampaignDTO>> getMarketingCampaignList(
			MarketplaceConsentCampaignCriteria criteria, Pageable pageable) {
		log.debug("REST request to get marketing campaign list");
		Page<MarketplaceConsentCampaignDTO> resultStudents = marketingService
				.getMarketplaceConsentCampaignList(criteria, pageable);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@PutMapping("/marketplace/consent/campaign")
	@Timed
	public ResponseEntity<MarketplaceConsentCampaignDTO> updateMarketingCampaign(
			@RequestBody MarketplaceConsentCampaignDTO dto) {
		log.debug("REST request to update marketing campaign list");

		if (!"Processed".equals(dto.getProcessingStatus())) {
			throw new BadRequestAlertException("Only processed request allowed", "marketplaceConsentCampaign",
					dto.toString());
		}

		MarketplaceConsentCampaignDTO resultStudents = marketingService.updateMarketingCampaign(dto);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@GetMapping("/student/email/campaign")
	@Timed
	public ResponseEntity<Page<StudentEmailCampaignResponseDTO>> getStudentEmailCampaigns(
			@RequestParam(value = "institutionId", required = false) Long institutionId, Pageable pageable) {
		log.debug("REST request to get student email campaign list");

		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		Page<StudentEmailCampaignResponseDTO> resultStudents = marketingService.getStudentEmailCampaigns(institutionId,
				pageable);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}

	@PutMapping("/student/email/campaign")
	@Timed
	public ResponseEntity<Map<String, String>> updateEmailCampagn(
			@RequestBody StudentEmailCampaignResponseDTO studentEmailCampaignDTO) {
		log.debug("REST request to update as processed for criteria id : {}", studentEmailCampaignDTO.getId());

		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, String> result = marketingService.updateStudentEmailCampaignStatus(studentEmailCampaignDTO);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@DeleteMapping("/student/email/campaign/{id}")
	@Timed
	public ResponseEntity<Map<String, String>> deleteEmailCampagn(@PathVariable Long id) {
		log.debug("REST request to update as processed for criteria id : {}", id);

		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, String> result = marketingService.deleteStudentEmailCampaignStatus(id);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/export/qualified/students/{criteriaId}")
	@Timed
	public ResponseEntity<Map<String, Object>> getCriteriaBasedStudents(@PathVariable Long criteriaId)
			throws IOException {
		log.debug("REST request to get all the qualified student list of criteria id : {}", criteriaId);

		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		Map<String, Object> resultStudents = marketingService.getQualifiedStudentsDownloadUrl(criteriaId);
		return new ResponseEntity<>(resultStudents, HttpStatus.OK);
	}
	
	@GetMapping("/received/credentials/report")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public ResponseEntity<List<Map<String, Object>>> getInboundReportsWithCriteria(
			@RequestParam(value = "emailDomain", required = false) String emailDomain,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "institutionId", required = false) Long institutionId
	) {
		log.info("REST request to get Received Credentials Report (inbound)");
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		List<Map<String, Object>> response = marketingService.getInboudCredentialReport(institutionId,startDate,endDate,emailDomain);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
