package com.gl.platform.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.InstitutionRestriction;
import com.gl.platform.service.InstitutionRestrictionService;
import com.gl.platform.service.dto.InstitutionRestrictionDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

@RestController
@RequestMapping("/api")
public class InstitutionRestrictionResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionRestrictionResource.class);

	@Autowired
	private InstitutionRestrictionService institutionRestrictionService;

	private static final String ENTITY_NAME = InstitutionRestriction.class.getSimpleName().toLowerCase();

	@PostMapping("/institution/restriction")
	@Timed
	public ResponseEntity<Object> createInstitutionRestriction(@RequestBody InstitutionRestrictionDTO iRestrictionDTO) {
		log.debug("REST request to create instituion restriction : {}", iRestrictionDTO);
		iRestrictionDTO = institutionRestrictionService.save(iRestrictionDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, iRestrictionDTO.toString())).build();

	}

	@PutMapping("/institution/restriction")
	@Timed
	public ResponseEntity<Object> updateInstitutionRestriction(@RequestBody InstitutionRestrictionDTO iRestrictionDTO) {
		log.debug("REST request to update instituion restriction : {}", iRestrictionDTO);

		if (iRestrictionDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		iRestrictionDTO = institutionRestrictionService.save(iRestrictionDTO);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, iRestrictionDTO.toString()))
				.build();

	}

	@GetMapping("institution/{id}/restriction")
	@Timed
	public ResponseEntity<Object> getInstitutionRestriction(@PathVariable Long id) {
		InstitutionRestrictionDTO iRestrictionDTO = institutionRestrictionService
				.getRestrictionDetailsByInstituionId(id);
		if (iRestrictionDTO == null) {
			iRestrictionDTO = new InstitutionRestrictionDTO();
		}
		return new ResponseEntity<>(iRestrictionDTO, HttpStatus.OK);
	}

	@GetMapping("institution/restriction")
	@Timed
	public ResponseEntity<?> getAllInstitutionRestrictions() {
		List<InstitutionRestrictionDTO> iRestrictionDTO = institutionRestrictionService.findAll();
		return new ResponseEntity<>(iRestrictionDTO, HttpStatus.OK);
	}

}
