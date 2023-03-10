package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.GroupMembershipService;
import com.gl.platform.service.dto.GroupMembershipDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

@RestController
@RequestMapping("/api")
public class GroupMembershipResource {

private final Logger log = LoggerFactory.getLogger(GroupMembershipResource.class);
	
	private static final String ENTITY_NAME = GroupMembershipResource.class.getSimpleName().toLowerCase();
	
	@Autowired
	private GroupMembershipService groupMembershipService;

	@PostMapping("/group/membership")
	public ResponseEntity<?> createGroupMembership(GroupMembershipDTO groupMembershipDTO) throws URISyntaxException {
		
		log.debug("REST request to create an group membership : {}", groupMembershipDTO);
		if (groupMembershipDTO.getId() != null) {
			throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, "idexists");
		}
		
		GroupMembershipDTO result = groupMembershipService.save(groupMembershipDTO);
		
		return ResponseEntity.created(new URI("/api/group/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}
}
