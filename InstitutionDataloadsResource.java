package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.multipart.MultipartFile;

import com.gl.platform.domain.InstitutionDataloads;
import com.gl.platform.service.InstitutionDataloadsService;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import javassist.NotFoundException;

@RestController
@RequestMapping("/api")
public class InstitutionDataloadsResource {

	private final Logger log = LoggerFactory.getLogger(InstitutionDataloadsResource.class);

	private static final String ENTITY_NAME = InstitutionDataloadsResource.class.getSimpleName().toLowerCase();

	@Autowired
	private InstitutionDataloadsService instDataloadsService;

	@PostMapping("/inst/dataload/paths")
	public ResponseEntity<InstitutionDataloads> createInstitutionDataload(
			@RequestBody InstitutionDataloads instDataloads) throws URISyntaxException, NotFoundException {

		log.debug("REST request to create an Institution data load paths : {}", instDataloads);
		if (instDataloads.getId() != null) {
			throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, "idexists");
		}

		InstitutionDataloads result = instDataloadsService.save(instDataloads);

		return ResponseEntity.created(new URI("/api/inst/dataload/path" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/inst/dataload/paths/{institutionId}")
	public ResponseEntity<InstitutionDataloads> getInstitutionDataloadPaths(@PathVariable Long institutionId)
			throws NotFoundException {
		InstitutionDataloads response = instDataloadsService.getInstDataloads(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/inst/load/data/{institutionId}")
	public ResponseEntity<Map<String, Object>> loadInstitutionData(@PathVariable Long institutionId)
			throws NotFoundException, IOException {
		Map<String, Object> response = instDataloadsService.loadDataForInstitution(institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/upload/students/affiliation")
	public ResponseEntity<Map<String, Object>> loadAffiliationsForStudents(@RequestParam("template") MultipartFile file,
			@RequestParam(value = "affiliation", required = true) String affiliation,
			@RequestParam(value = "institutionId", required = true) Long institutionId)
			throws IOException, NotFoundException {
		log.info("REST request to assign the affiliations for students with affiliation : {}", affiliation);
		Map<String, Object> response = instDataloadsService.assignAffiliationsForStudents(file, affiliation,
				institutionId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
