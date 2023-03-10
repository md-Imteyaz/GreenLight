package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.Institution;
import com.gl.platform.repository.InstitutionRepository;
import com.gl.platform.repository.StatisticsRepository;
import com.gl.platform.security.AuthoritiesConstants;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.InstitutionConnectorService;
import com.gl.platform.service.InstitutionQueryService;
import com.gl.platform.service.InstitutionService;
import com.gl.platform.service.dto.CampusCustomDTO;
import com.gl.platform.service.dto.EmployerStatisticsDTO;
import com.gl.platform.service.dto.InstEdiFieldDTO;
import com.gl.platform.service.dto.InstitutionConnectorDTO;
import com.gl.platform.service.dto.InstitutionCriteria;
import com.gl.platform.service.dto.InstitutionDTO;
import com.gl.platform.service.dto.InstitutionResponseDTO;
import com.gl.platform.service.dto.Registrations;
import com.gl.platform.service.dto.StudentCredentialVisibilityDTO;
import com.gl.platform.service.dto.UniverisityStatisticsDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.CampusNameAlreadyUsedException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.vm.UniversityDashboard;

import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiImplicitParam;
import javassist.NotFoundException;

/**
 * REST controller for managing Institution.
 */
@RestController
@RequestMapping("/api")
public class InstitutionResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionResource.class);

	private static final String ENTITY_NAME = Institution.class.getSimpleName().toLowerCase();

	private static final String ID_EXISTS = "idexists";

	private final InstitutionService institutionService;

	private final InstitutionQueryService institutionQueryService;

	@Autowired
	private InstitutionRepository institutionRepository;

	@Autowired
	private StatisticsRepository statisticsRepository;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private InstitutionConnectorService institutionConnectorService;

	public InstitutionResource(InstitutionService institutionService, InstitutionQueryService institutionQueryService) {
		this.institutionService = institutionService;
		this.institutionQueryService = institutionQueryService;
	}

	/**
	 * POST /institutions : Create a new institution.
	 *
	 * @param institutionDTO the institutionDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         institutionDTO, or with status 400 (Bad Request) if the institution
	 *         has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/institutions")
	@Timed
	// @RolesAllowed({"ROLE_GL_SU_ADMIN"})
	public ResponseEntity<InstitutionDTO> createInstitution(@Valid @RequestBody InstitutionDTO institutionDTO)
			throws URISyntaxException {
		log.debug("REST request to create new Institution : {}", institutionDTO);
		if (institutionDTO.getId() != null) {
			throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, ID_EXISTS);
		}
		institutionDTO.setActive(true);
		InstitutionDTO result = institutionService.save(institutionDTO);
		return ResponseEntity.created(new URI("/api/institutions/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PostMapping("/institutions/full")
	@Timed
	public ResponseEntity<InstitutionDTO> createInstitutionWithChilds(
			@Valid @RequestBody CampusCustomDTO campusCustomDTO) throws URISyntaxException {
		log.debug("REST request to create institution with child: {}", campusCustomDTO);
		if (campusCustomDTO.getId() != null) {
			throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, ID_EXISTS);
		}
		institutionRepository.findOneByNameIgnoreCase(campusCustomDTO.getName()).ifPresent(u -> {
			throw new CampusNameAlreadyUsedException();
		});
		InstitutionDTO result = institutionService.saveFull(campusCustomDTO);
		return ResponseEntity.created(new URI("/api/institutions/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PutMapping("/institutions/full")
	@Timed
	public ResponseEntity<CampusCustomDTO> updateCampusData(@Valid @RequestBody CampusCustomDTO campusCustomDTO) {
		institutionRepository.findExistingByName(campusCustomDTO.getName(), campusCustomDTO.getId()).ifPresent(u -> {
			throw new CampusNameAlreadyUsedException();
		});

		log.debug("REST request to update Institution : {}", campusCustomDTO);
		CampusCustomDTO campusDTO = institutionService.updateFull(campusCustomDTO);
		return new ResponseEntity<>(campusDTO, HttpStatus.OK);
	}

	@GetMapping("/institutions/full/{id}")
	@Timed
	public ResponseEntity<CampusCustomDTO> getCampusData(@PathVariable Long id) {

		log.debug("REST request to get InstitutionId : {}", id);
		CampusCustomDTO campusDTO = institutionService.getFull(id);
		return new ResponseEntity<>(campusDTO, HttpStatus.OK);
	}

	@PutMapping("/institution/connector")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.SITEADMIN, AuthoritiesConstants.UNIVERSITY_SUPER_ADMIN,
			AuthoritiesConstants.UNIVERSITY_OTHER_SUPER_ADMIN, AuthoritiesConstants.EMPLOYEE_SUPER_ADMIN })
	public ResponseEntity<InstitutionConnectorDTO> updateConnection(
			@RequestBody InstitutionConnectorDTO instituteConector) throws URISyntaxException {
		log.debug("REST request to save Institution : {}", instituteConector);
		if (instituteConector.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		InstitutionConnectorDTO result = institutionService.saveConnectors(instituteConector);
		return ResponseEntity.created(new URI("/api/institution/connector/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);

	}

	@PostMapping("/institution/connector")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.SITEADMIN, AuthoritiesConstants.UNIVERSITY_SUPER_ADMIN,
			AuthoritiesConstants.UNIVERSITY_OTHER_SUPER_ADMIN, AuthoritiesConstants.EMPLOYEE_SUPER_ADMIN })
	public ResponseEntity<InstitutionConnectorDTO> createConnection(
			@RequestBody InstitutionConnectorDTO instituteConectorDTO) throws URISyntaxException {

		log.debug("REST request to save Institution : {}", instituteConectorDTO);

		if (instituteConectorDTO.getId() != null) {
			throw new BadRequestAlertException("A new connector cannot already have an ID", ENTITY_NAME, ID_EXISTS);
		}
		InstitutionConnectorDTO result = institutionService.saveConnectors(instituteConectorDTO);

		return ResponseEntity.created(new URI("/api/institution/connector/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);

	}

	@GetMapping("/institution/connector/{universityId}")
	@Timed
	@RolesAllowed({ AuthoritiesConstants.SITEADMIN, AuthoritiesConstants.UNIVERSITY_SUPER_ADMIN,
			AuthoritiesConstants.UNIVERSITY_OTHER_SUPER_ADMIN, AuthoritiesConstants.EMPLOYEE_SUPER_ADMIN })
	public ResponseEntity<InstitutionConnectorDTO> getConnectorConfig(@PathVariable Long universityId) {
		log.debug("REST request to get connector config universityId : {}", universityId);
		InstitutionConnectorDTO connector = institutionConnectorService.getConnectorDetailsByUniversityId(universityId);
		return ResponseEntity.ok().body(connector);
	}

	/**
	 * PUT /institutions : Updates an existing institution.
	 *
	 * @param institutionDTO the institutionDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         institutionDTO, or with status 400 (Bad Request) if the
	 *         institutionDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the institutionDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/institutions")
	@Timed
	public ResponseEntity<InstitutionDTO> updateInstitution(@Valid @RequestBody InstitutionDTO institutionDTO)
			throws URISyntaxException {
		log.debug("REST request to update Institution : {}", institutionDTO);
		if (institutionDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		InstitutionDTO result = institutionService.save(institutionDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, institutionDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /institutions : get all the institutions.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of institutions
	 *         in body
	 */
	@GetMapping("/institutions")
	@Timed
	// @RolesAllowed({"ROLE_GL_SU_ADMIN"})
	public ResponseEntity<List<InstitutionResponseDTO>> getAllInstitutions(InstitutionCriteria criteria) {
		log.debug("REST request to get Institutions by criteria: {}", criteria);
		List<InstitutionResponseDTO> institutions = institutionQueryService.getInstitutionsByCritera(criteria);
		return ResponseEntity.ok().body(institutions);
	}

	/**
	 * GET /institutions : get all the institutions.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of institutions
	 *         in body
	 */
	@GetMapping("/institutions/dropdownlist")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getAllInstitutionsDropdownList() {
		log.debug("REST request to get Institutions dropdownList");
		List<Map<String, Object>> list = institutionService.getAllInstitutionsDropdownList();
		return ResponseEntity.ok().body(list);
	}

	@GetMapping("/institutions/tcb/dropdownlist")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getAllTCBInstitutionsDropdownList() {
		log.debug("REST request to get TCB Institutions dropdownList");
		List<Map<String, Object>> list = institutionService.getAllTCBInstitutionsDropdownList();
		return ResponseEntity.ok().body(list);
	}

	@GetMapping("/institutions/dropdownlist/{type}")
	@Timed
	public ResponseEntity<Object> getAllInstitutionsDropdownListByType(@PathVariable String type,
			@RequestParam(value = "institutionId", required = false) Long institutionId) {
		log.debug("REST request to get Institutions dropdownList by type: {}", type);
		List<Map<String, Object>> list = institutionService.getAllInstitutionsDropdownListByType(type, institutionId);
		return ResponseEntity.ok().body(list);
	}

	@GetMapping("/institutions/employerlist")
	@Timed
	public ResponseEntity<List<String>> getAllEmployersDropdownList() {
		log.debug("REST request to get Institutions employer list by type");
		List<String> list = institutionService.getAllEmployerList();
		return ResponseEntity.ok().body(list);
	}

	@GetMapping("/institution/dashboard/{id}")
	@Timed
	public ResponseEntity<UniversityDashboard> getDashBoard(@PathVariable Long id) {
		log.debug("REST request to get dashboard id : {}", id);
		UniversityDashboard dashBoard = institutionService.getDashBoardView(id);
		return ResponseEntity.ok().body(dashBoard);
	}

	@GetMapping("/institutions/{institutionId}/registrations")
	@Timed
	public ResponseEntity<Registrations> getRegistrations(@PathVariable Long institutionId,
			@RequestParam(value = "studentSearchTerm", required = false) String studentSearchTerm,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {

		log.debug("REST request to get registrations : {}", institutionId);
		LocalDate start = null;
		LocalDate end = null;
		if (startDate != null) {
			start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (endDate != null) {
			end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		Registrations registrations = institutionService.getRegistrations(institutionId, studentSearchTerm, start, end);

		return ResponseEntity.ok().body(registrations);
	}

	/**
	 * GET /institutions/:id : get the "id" institution.
	 *
	 * @param id the id of the institutionDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         institutionDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/institutions/{id}")
	@Timed
	public ResponseEntity<InstitutionDTO> getInstitution(@PathVariable Long id) {
		log.debug("REST request to get Institution : {}", id);
		Optional<InstitutionDTO> institutionDTO = institutionService.findOne(id);
		return ResponseUtil.wrapOrNotFound(institutionDTO);
	}

	@GetMapping("/institutions/childs/{parentId}")
	@Timed
	public ResponseEntity<List<Object>> getChildInstitutes(@PathVariable Long parentId) {
		log.debug("REST request to get child Institutions : {}", parentId);
		List<Object> institutes = institutionService.getChildInstitutes(parentId);
		return ResponseEntity.ok().body(institutes);
	}

	@GetMapping("/institution/{parentId}/campuses")
	@Timed
	public ResponseEntity<Object> getChildCampuses(@PathVariable Long parentId) {
		log.debug("REST request to get Institution : {}", parentId);
		Map<String, Object> institutes = institutionService.getCampusesOfInstitution(parentId);
		return ResponseEntity.ok().body(institutes);
	}

	/**
	 * DELETE /institutions/:id : delete the "id" institution.
	 *
	 * @param id the id of the institutionDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/institutions/{id}")
	@Timed
	public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
		log.debug("REST request to delete Institution : {}", id);
		institutionService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

	@GetMapping("/statistics/employer/{institutionId}")
	@Timed
	public ResponseEntity<EmployerStatisticsDTO> getEmployerStatistics(@PathVariable Long institutionId) {
		log.debug("REST request to get employer statistics institutionId : {}", institutionId);

		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}

		EmployerStatisticsDTO response = statisticsRepository.getStatisticByEmployerId(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/statistics/university/{institutionId}")
	@Timed
	public ResponseEntity<UniverisityStatisticsDTO> getUniversityStatistics(@PathVariable Long institutionId) {
		log.debug("REST request to get university statistics institutionId : {}", institutionId);

		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		UniverisityStatisticsDTO response = statisticsRepository.getStatisticByUniversityId(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/institution/change-status/{id}")
	@Timed
	public ResponseEntity<Object> updateInstitutionStatus(@PathVariable Long id) {
		log.debug("REST request to update the institution status : {}", id);
		Map<String, Object> response = institutionService.updateStatus(id);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PutMapping("/institution/{id}/update/{propertyName}/{propertyValue}")
	@Timed
	public ResponseEntity<Map<String, Object>> updateInstitutionFlag(@PathVariable Long id,
			@PathVariable String propertyName, @PathVariable boolean propertyValue) throws NotFoundException {
		log.debug("REST request to update the institution column:{}  with id: {}, value: {}", propertyName, id,
				propertyValue);
		if (propertyName == null) {
			throw new BadRequestAlertException("column name must not be null", ENTITY_NAME, "nameMustNotbeNull");
		}
		Map<String, Object> response = institutionService.updateInstitutionWithPropertyName(id, propertyName,
				propertyValue);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/credential/visibility")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public @ResponseBody ResponseEntity<StudentCredentialVisibilityDTO> createVisibilityInfo(
			@RequestBody StudentCredentialVisibilityDTO studentCredVisbilityDTO) {

		log.debug("REST request to create student credential visibility for institution id : {}",
				studentCredVisbilityDTO.getInstitutionId());

		if (studentCredVisbilityDTO.getId() != null) {
			throw new BadRequestAlertException("POST method cannot contain id", ENTITY_NAME, "idMustNull");
		}

		StudentCredentialVisibilityDTO response = institutionService.saveVisibility(studentCredVisbilityDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/credential/visibility/institution/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<StudentCredentialVisibilityDTO> getVisibilityInfo(@PathVariable Long institutionId)
			throws NotFoundException {
		log.debug("REST request to get the student credential visibility ");
		StudentCredentialVisibilityDTO response = institutionService.getVisibilityInfo(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/credential/visibility")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public @ResponseBody ResponseEntity<StudentCredentialVisibilityDTO> updateVisibilityInfo(
			@RequestBody StudentCredentialVisibilityDTO studentCredVisbilityDTO) {

		log.debug("REST request to update student credential visibility info for institution id : {}",
				studentCredVisbilityDTO.getInstitutionId());

		if (studentCredVisbilityDTO.getId() == null) {
			throw new BadRequestAlertException("PUT method cannot contain id empty", ENTITY_NAME, "idMustExists");
		}

		StudentCredentialVisibilityDTO response = institutionService.saveVisibility(studentCredVisbilityDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/covid/vaccine/report/{institutionId}")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getUniversityCovidVaccineStatistics(
			@PathVariable Long institutionId, @RequestParam(value = "startDate", required = false) String fromDate,
			@RequestParam(value = "endDate", required = false) String toDate) {
		log.debug("REST request to get university covid vaccine statistics institutionId : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		LocalDate dateFrom = fromDate != null ? GlConstraints.localDateFunction.apply(fromDate) : null;
		LocalDate dateTo = toDate != null ? GlConstraints.localDateFunction.apply(toDate) : null;
		List<Map<String, Object>> response = statisticsRepository.getCovidVaccineReportByUniversityId(institutionId,
				dateFrom, dateTo);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/institution/details/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Map<String, Object>> getInstInfo(@PathVariable Long institutionId) {
		log.debug("REST request to get the required institution details by user id and institutionId : {}",
				institutionId);
		Map<String, Object> response = institutionService.getInstitutionDetailsByUser(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/vjfEmployer/details")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getVgfEmployersList() {
		log.debug("REST request to get all Vfj EMployers list");
		List<Map<String, Object>> response = institutionService.getAllVjfEmployersList();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/edready/token")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	public @ResponseBody ResponseEntity<Map<String, Object>> generateEdreadyRedirectToken(
			@RequestParam(value = "institutionId", required = true) Long institutionId) throws NotFoundException {
		log.debug("REST request to generate edready redirect credential token for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = institutionService.generateEdreadyRedirectCredential(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/edready/token/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<Map<String, Object>> getEdreadyRedirectToken(@PathVariable Long institutionId) {
		log.debug("REST request to get the edready redirect token for institution id : {}", institutionId);
		if (!authorizationService.ownedByInstitution(institutionId)) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		Map<String, Object> response = institutionService.getEdreadyRedirectToken(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/institution/{id}/enable/certificate/{enablePath}")
	@Timed
	public ResponseEntity<Object> updateInstitutionEnableCertificatePath(@PathVariable Long id,
			@PathVariable boolean enablePath,
			@RequestParam(value = "certificatePath", required = true) String certificatePath) {
		log.debug("REST request to update the institution enable certificate path: {}, value: {}", id, enablePath);
		Map<String, Object> response = institutionService.updateEnableCertificatePath(id, enablePath, certificatePath);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/institution/edi/patch")
	@Timed
	public ResponseEntity<Object> updateInstitution(@RequestBody InstEdiFieldDTO instEdiFieldDTO) {
		log.debug("REST request to update the institution edi termintors");
		Map<String, Object> response = institutionService.updateEDITerminators(instEdiFieldDTO.getInstitutionId(),
				instEdiFieldDTO.getEdiFieldTerminator(), instEdiFieldDTO.getEdiSegmentTerminator());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/institution/counsellors/{institutionId}")
	@ApiImplicitParam(name = "Authorization", required = true, paramType = "header", dataType = "string", value = "authorization header", defaultValue = "Bearer ")
	@Timed
	public ResponseEntity<List<Map<String, Object>>> getCounsellorsList(@PathVariable Long institutionId)
			throws NotFoundException {
		log.debug("REST request to get counsellors list for institution id : {}", institutionId);
		if (!authorizationService.isSupport()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		List<Map<String, Object>> response = institutionService.getCounsellorsList(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
