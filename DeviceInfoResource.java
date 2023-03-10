package com.gl.platform.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.DeviceInfoService;
import com.gl.platform.service.dto.DeviceInfoDTO;

@RestController
@RequestMapping("/api")
public class DeviceInfoResource {

	private final Logger log = LoggerFactory.getLogger(DeviceInfoResource.class);
	private static final String ENTITY_NAME = "deviceInfo";

	@Autowired
	private DeviceInfoService deviceInfoService;

	@Autowired
	private AuthorizationService authorizationService;

	@PostMapping("/device/info")
	public ResponseEntity<DeviceInfoDTO> saveDeviceInfo(@RequestBody DeviceInfoDTO deviceInfoDTO) {
		log.debug("REST request to save the device info : {}", deviceInfoDTO);

		if (!authorizationService.ownedByUserOnly(deviceInfoDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}
		DeviceInfoDTO response = deviceInfoService.save(deviceInfoDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/device/info")
	public ResponseEntity<DeviceInfoDTO> deleteDeviceInfo(@RequestBody DeviceInfoDTO deviceInfoDTO) {
		log.debug("REST request to delete the device info with device info: {}", deviceInfoDTO);

		if (!authorizationService.ownedByUserOnly(deviceInfoDTO.getUserId())) {
			throw new AccessDeniedException("not the owner");
		}

		DeviceInfoDTO response = deviceInfoService.save(deviceInfoDTO);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

//	@GetMapping("/get/device")
//	public ResponseEntity<?> getMessage() {
//		String response = deviceInfoService.sendPushNotifications(8l, "Hello");
//		return new ResponseEntity<>(response, HttpStatus.OK);
//	}
}
