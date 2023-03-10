package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.InstitutionGroupService;
import com.gl.platform.service.dto.GroupMembershipDTO;
import com.gl.platform.service.dto.GroupMembershipRequestDTO;
import com.gl.platform.service.dto.InstitutionGroupDTO;
import com.gl.platform.service.dto.InstitutionGroupRequestDTO;
import com.gl.platform.service.util.GlConstraints;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class InstitutionGroupResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionGroupResource.class);

	private static final String ENTITY_NAME = InstitutionGroupResource.class.getSimpleName().toLowerCase();

	@Autowired
	private InstitutionGroupService instGroupService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/employ/group")
	public ResponseEntity<?> createInstitutionGroup(@RequestBody InstitutionGroupDTO instGroupDTO)
			throws URISyntaxException {

		log.debug("REST request to create an Institution Group : {}", instGroupDTO);
		if (instGroupDTO.getId() != null) {
			throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, "idexists");
		}
		
		if(instGroupDTO.getShowMatchesToEmployers() == null) {
			instGroupDTO.setShowMatchesToEmployers(false);
		}
		
		instGroupDTO.setCreatedDate(LocalDate.now());
		InstitutionGroupDTO result = instGroupService.save(instGroupDTO);

		return ResponseEntity.created(new URI("/api/institution/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/employ/groups/{instId}")
	public ResponseEntity<?> getInstitutionGroups(@PathVariable Long instId) {
		List<InstitutionGroupDTO> instGroups = instGroupService.getGroupsByInstitutionId(instId);

		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@PutMapping("/employ/group")
	public ResponseEntity<?> updateInstitutionGroup(@RequestBody InstitutionGroupDTO instGroupDTO)
			throws URISyntaxException, NotFoundException {

		log.debug("REST request to create an Institution Group : {}", instGroupDTO);
		if (instGroupDTO.getId() == null) {
			throw new BadRequestAlertException("institution must have an ID", ENTITY_NAME, "idnotexists");
		}
		
		if(instGroupDTO.getShowMatchesToEmployers() == null) {
			instGroupDTO.setShowMatchesToEmployers(false);
		}

		InstitutionGroupDTO result = instGroupService.update(instGroupDTO);

		return ResponseEntity.created(new URI("/api/institution/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@DeleteMapping("/employ/groups/{groupId}")
	public ResponseEntity<?> deleteInstitutionGroups(@PathVariable Long groupId) throws URISyntaxException {
		instGroupService.deleteGroupsByGroupId(groupId);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/employ/all/groups/{instId}")
	public ResponseEntity<?> getInstitutionGroupsFor(@PathVariable Long instId) throws URISyntaxException {
		List<InstitutionGroupDTO> instGroups = instGroupService.getAllGroupsForInstitutionId(instId);

		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/employ/all/groups")
	public ResponseEntity<?> getAllInstitution() throws URISyntaxException {
		List<String> instGroups = instGroupService.getAllGroupsForSuperAdmin();
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@PostMapping("/employ/member/request")
	public ResponseEntity<?> createMemberRequest(@RequestBody GroupMembershipRequestDTO membershipRequestDTO)
			throws URISyntaxException, NotFoundException {

		log.debug("REST request to create an GroupMembershipRequest : {}", membershipRequestDTO);
		if (membershipRequestDTO.getId() != null) {
			throw new BadRequestAlertException("A new request cannot already have an ID", ENTITY_NAME, "idexists");
		}

		membershipRequestDTO.setRequestedDate(LocalDate.now());
		GroupMembershipRequestDTO result = instGroupService.saveMembershipRequest(membershipRequestDTO);

		return ResponseEntity.created(new URI("/api/institution/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@PutMapping("/employ/member/request")
	public ResponseEntity<?> updateMemberRequest(@RequestBody GroupMembershipRequestDTO membershipRequestDTO)
			throws URISyntaxException, NotFoundException {

		log.debug("REST request to create an GroupMembershipRequest : {}", membershipRequestDTO);
		if (membershipRequestDTO.getId() == null) {
			throw new BadRequestAlertException("Update request must have an ID", ENTITY_NAME, "idnotexists");
		}

		membershipRequestDTO.setRequestedDate(LocalDate.now());
		GroupMembershipRequestDTO result = instGroupService.saveMembershipRequest(membershipRequestDTO);

		return ResponseEntity.created(new URI("/api/institution/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/employ/all/requests/{instId}")
	public ResponseEntity<?> getInstitutionGroupRequestFor(@PathVariable Long instId) {
		List<GroupMembershipRequestDTO> instGroups = instGroupService.getAllGroupsMemberRequests(instId);

		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/employ/members/{groupId}")
	public ResponseEntity<?> getMembersOfGroup(@PathVariable Long groupId) throws NotFoundException {
		List<GroupMembershipDTO> instGroups = instGroupService.getAllGroupsMembers(groupId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/employ/all/requestsby/{instId}")
	public ResponseEntity<?> getInstitutionGroupRequestBy(@PathVariable Long instId) {
		List<GroupMembershipRequestDTO> instGroups = instGroupService.getAllGroupsMemberRequestsRequestedBy(instId);

		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/group/{groupId}/requests")
	public ResponseEntity<?> getGroupRequestsFor(@PathVariable Long groupId) throws NotFoundException {
		List<GroupMembershipRequestDTO> instGroups = instGroupService.getGroupRequests(groupId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/employ/memberships/{institutionId}")
	public ResponseEntity<?> getAllGroupMembershipsByInstitution(@PathVariable Long institutionId) {
		log.debug("REST request to get all memberships of institution by insId : {}", institutionId);

		List<GroupMembershipDTO> instGroups = instGroupService.getMembershipListOfInstitution(institutionId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@DeleteMapping("/group/{membershipId}/leave")
	public ResponseEntity<?> leaveGroupMembershipFor(@PathVariable Long membershipId) {
		log.debug("REST request to leave a GroupMembership : {}", membershipId);
		instGroupService.leaveGroupMembership(membershipId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/group/{membershipId}/remove")
	public ResponseEntity<?> remembershipGroupMembershipFor(@PathVariable Long membershipId) {
		log.debug("REST request to remove a GroupMembership : {}", membershipId);
		instGroupService.removeGroupMembership(membershipId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/employ/groups/associated/{institutionId}")
	public ResponseEntity<?> getAllAssociatedGroupsByInstitution(@PathVariable Long institutionId) {
		log.debug("REST request to get all memberships of institution by insId : {}", institutionId);

		List<Map<String, Object>> instGroups = instGroupService.getAssociatedMembershipListOfInstitution(institutionId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@PostMapping("/institution/membership")
	public ResponseEntity<Map<String, Object>> createInstitutionMembership(
			@RequestBody InstitutionGroupRequestDTO institutionGroupRequestDTO) throws URISyntaxException {

		log.debug("REST request to create an institution group membership request : {}", institutionGroupRequestDTO);

		Map<String, Object> result = instGroupService.saveInstitutionGroupRequest(institutionGroupRequestDTO);

		return ResponseEntity
				.created(new URI("/api/institution/membership")).headers(HeaderUtil
						.createEntityCreationAlert(ENTITY_NAME, result.get("requestedInstitutionId").toString()))
				.body(result);
	}

	@GetMapping("/institution/group/membership/{institutionId}")
	public ResponseEntity<List<Map<String, Object>>> getInstitutionMembership(@PathVariable Long institutionId) {
		log.debug("REST request to get all memberships of institution by insId : {}", institutionId);

		List<Map<String, Object>> instGroups = instGroupService.getAllGroupsForInstitution(institutionId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}

	@GetMapping("/employer/job/posted/groups")
	public ResponseEntity<List<InstitutionGroupDTO>> getAllEmployerJobPostedGroups(
			@RequestParam(value = "institutionId", required = false) String institutionId) {
		if (!authorizationService.isMarketingUser()) {
			throw new AccessDeniedException(GlConstraints.OWNER);
		}
		log.debug("REST request to get all the groups by employer jobs posted by insId : {}", institutionId);
		List<InstitutionGroupDTO> instGroups = instGroupService.getEmployerGroupNames(institutionId);
		return new ResponseEntity<>(instGroups, HttpStatus.OK);
	}
}
