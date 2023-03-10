package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.ExpandedCredential;
import com.gl.platform.repository.ExpandedCredentialRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.ExpandedCredentialQueryService;
import com.gl.platform.service.ExpandedCredentialService;
import com.gl.platform.service.S3StorageService;
import com.gl.platform.service.UserActivityService;
import com.gl.platform.service.dto.ExpandedCredentialCriteria;
import com.gl.platform.service.dto.ExpandedCredentialDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.service.filter.BooleanFilter;

@RestController
@RequestMapping("/api")
public class ExpandedCredentialsResource {

	
	public ExpandedCredentialService getExpandedCredentialsServices() {
		return expandedCredentialsServices;
	}

	public ExpandedCredentialRepository getExpandedCredentialRepository() {
		return expandedCredentialRepository;
	}
	
	@Autowired
	private UserActivityService userActivityService;
	
	@Autowired
	private S3StorageService s3StorageService; 
	
	
	@Autowired
	private AuthorizationService authorizationService;

	private final Logger log = LoggerFactory.getLogger(ExpandedCredentialsResource.class);

	private static final String ENTITY_NAME = ExpandedCredential.class.getSimpleName().toLowerCase();

	private final ExpandedCredentialService expandedCredentialsServices;
	
	private final ExpandedCredentialQueryService expandedCredentialQueryService; 

	private final ExpandedCredentialRepository expandedCredentialRepository;

	public ExpandedCredentialsResource(ExpandedCredentialService expandedCredentialsService,
			ExpandedCredentialRepository expandedCredentialRepository,
			ExpandedCredentialQueryService expandedCredentialQueryService) {
		this.expandedCredentialRepository = expandedCredentialRepository;
		this.expandedCredentialsServices = expandedCredentialsService;
		this.expandedCredentialQueryService=expandedCredentialQueryService;
	}

	/**
	 * POST /expanded-credentials : Create a new expanded credential.
	 *
	 * @param expandedCredentialDTO the expandedCredentialDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         expandedCredentialDTO, or with status 400 (Bad Request) if the
	 *         expandedCredential has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws IOException 
	 */
	@PostMapping("/expanded-credentials")
	@Timed
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT')")
	public ResponseEntity<ExpandedCredentialDTO> createExpandedCredentials(
			@RequestBody ExpandedCredentialDTO expandedCredentialDTO) throws URISyntaxException, IOException {

		log.debug("REST request to save Accomplishment Portfolio : {}", expandedCredentialDTO);
		ExpandedCredentialDTO result = expandedCredentialsServices.generatePDF(expandedCredentialDTO);
		userActivityService.audit("AccomplishmentPortfolio", "Added Accomplishment Portfolio");		
		return ResponseEntity.created(new URI("/api/expanded-credentials/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	@GetMapping("/expanded-credentials")
	@Timed
	public ResponseEntity<?> getExpandedCredential(ExpandedCredentialCriteria criteria) {
		log.debug("REST request to get Accomplishment Portfolio : {}", criteria);
		BooleanFilter statusFilter = new BooleanFilter();
		statusFilter.setEquals(true);
		criteria.setActive(statusFilter);
		List<ExpandedCredentialDTO> expandedCredentialDTO = expandedCredentialQueryService.findByCriteria(criteria);

		return new ResponseEntity<>(expandedCredentialDTO, HttpStatus.OK);
	}

	@GetMapping("/expanded-credentials/{id}")
	@Timed
	public ResponseEntity<ExpandedCredentialDTO> getExpandedCredential(@PathVariable Long id) {
		ExpandedCredentialDTO result=new ExpandedCredentialDTO();
		
		Optional<ExpandedCredentialDTO> expandedCredentialDTO=expandedCredentialsServices.findOne(id);
				
		if(expandedCredentialDTO.isPresent()) {
			log.debug("Request to get the ExpandedCredential object"+expandedCredentialDTO.get());
			
			if(!authorizationService.ownedByUserOnly(expandedCredentialDTO.get().getUserId())) {
				throw new AccessDeniedException("not the owner");
			}	
			
			if(expandedCredentialDTO.get().getActive()) {
				result=expandedCredentialDTO.get();
				String s3Url = "accomplishment/"+result.getId()+"/pdf_accomplishment";
				expandedCredentialDTO.get().setAccompViewUrl(s3StorageService.generatePresignedUrl(s3Url));
			}
		}
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}	
		
	/*
	 * @PutMapping("/expanded-credentials")
	 * 
	 * @Timed
	 * 
	 * @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_STUDENT')") public
	 * ResponseEntity<ExpandedCredentialDTO> updateExpandedCredential(@RequestBody
	 * ExpandedCredentialDTO expandedCredentialDTO) {
	 * 
	 * if (expandedCredentialDTO.getId() == null) { throw new
	 * BadRequestAlertException("To update Accomplishment Portfolio object should have an ID"
	 * , ENTITY_NAME, "idnotexists"); } ExpandedCredentialDTO
	 * result=expandedCredentialsServices.updateExpandedCredential(
	 * expandedCredentialDTO); userActivityService.audit("AccomplishmentPortfolio",
	 * "Updated Accomplishment Portfolio"); return new
	 * ResponseEntity<>(result,HttpStatus.OK); }
	 * 
	 * @DeleteMapping("/expanded-credentials/{id}")
	 * 
	 * @Timed public ResponseEntity<Void> deleteExpandedCredential(@PathVariable
	 * Long id) { log.debug("REST request to delete ExpandedCredential : {}", id);
	 * expandedCredentialsServices.delete(id); return
	 * ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
	 * id.toString())).build(); }
	 */

	
	/**
	 * DELETE /expanded-credentials/flag/:id : change the flag value to false  the "id" expandedCredential.
	 *
	 * @param id the id of the ExpandedCredentialDTO to soft delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/accomplishment/{id}")
	@Timed
	public ResponseEntity<Void> activateOrDeactivate(@PathVariable Long id) {
		log.debug("REST request to soft delete ExpandedCredential : {}", id);
		expandedCredentialsServices.activateOrDeactivate(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
	
}
