package com.gl.platform.web.rest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.GlGlobalPermissionService;
import com.gl.platform.service.dto.GlGlobalPermissionDTO;

@RestController
@RequestMapping("/api")
public class GlGlobalPermissionResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlGlobalPermissionService glGlobalPermissionService;

	@PostMapping("/gl/global/permission")
	public ResponseEntity<GlGlobalPermissionDTO> createGlGlobalPermission(
			@RequestBody GlGlobalPermissionDTO glGlobalPermissionDTO) {
		log.info("REST request to create the Gl Global Permission : {}", glGlobalPermissionDTO);
		GlGlobalPermissionDTO response = glGlobalPermissionService.saveOrUpdate(glGlobalPermissionDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/gl/global/permissions")
	public ResponseEntity<Map<String, Object>> getAllGlGlobalPermissions() {
		log.info("REST request to get all the Gl Global Permissions");
		Map<String, Object> response = glGlobalPermissionService.getGlGlobalPermissions();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/gl/global/permission")
	public ResponseEntity<GlGlobalPermissionDTO> updateGlGlobalPermission(
			@RequestBody GlGlobalPermissionDTO glGlobalPermissionDTO) {
		log.info("REST request to update the Gl Global Permission : {}", glGlobalPermissionDTO);
		GlGlobalPermissionDTO response = glGlobalPermissionService.saveOrUpdate(glGlobalPermissionDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
