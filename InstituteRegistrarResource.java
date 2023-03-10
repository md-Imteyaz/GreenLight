package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.InstituteRegistrarService;
import com.gl.platform.service.UserActivityService;
import com.gl.platform.service.dto.InstitutionRegistrarDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

/**
 * REST controller for managing Transcripts.
 */
@RestController
@RequestMapping("/api")
public class InstituteRegistrarResource {

    private static final String ENTITY_NAME = "Institute_Registrar";
    private final Logger log = LoggerFactory.getLogger(InstituteRegistrarResource.class);
    private final InstituteRegistrarService instituteRegistrarService;
    
    @Autowired
	private UserActivityService userActivityService;

    public InstituteRegistrarResource(InstituteRegistrarService instituteRegistrarService) {
        this.instituteRegistrarService = instituteRegistrarService;
    }

    /**
     * POST /institutions : Create a new institution.
     *
     * @param instituteRegistrarDTO the instituteRegistrarDTO to create./mv
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new instituteRegistrarDTO, or with status 400 (Bad Request) if the
     * institution has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/institute-registrar")
    @Timed
    public ResponseEntity<InstitutionRegistrarDTO> createInstituteRegistrar(@Valid @RequestBody InstitutionRegistrarDTO instituteRegistrarDTO)
        throws URISyntaxException {
        log.debug("REST request to save Institution : {}", instituteRegistrarDTO);
        if (instituteRegistrarDTO.getId() != null) {
            throw new BadRequestAlertException("A new institution cannot already have an ID", ENTITY_NAME, "idexists");
        }		
		
       // instituteRegistrarDTO.setac
        InstitutionRegistrarDTO result = instituteRegistrarService.saveRegistrar(instituteRegistrarDTO);
        userActivityService.audit("Registrar", "New registrar added");
        return ResponseEntity.created(new URI("/api/institute_registrar/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
    }

      
    @GetMapping("/institute-registrar/full/{id}")
    @Timed
    public ResponseEntity<?> getRegistrarInfo(@PathVariable Long id) {
    	log.debug("REST request to get registrar data by id : {}",id);
        List<InstitutionRegistrarDTO> registrar = instituteRegistrarService.getRegistrarInfo(id);
        return ResponseEntity.ok().body(registrar);
    }
    
	/**
	 * PUT /institute-registrar : Updates an existing address.
	 *
	 * @param institutionRegistrarDTO the institutionRegistrarDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         institutionRegistrarDTO, or with status 400 (Bad Request) if the institutionRegistrarDTO is not
	 *         valid, or with status 500 (Internal Server Error) if the institutionRegistrarDTO
	 *         couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/institute-registrar")
	@Timed
	public ResponseEntity<InstitutionRegistrarDTO> updateRegistrar(@Valid @RequestBody InstitutionRegistrarDTO institutionRegistrarDTO) throws URISyntaxException {
		log.debug("REST request to update InstitutionRegistrar");
		if (institutionRegistrarDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		InstitutionRegistrarDTO result = instituteRegistrarService.saveRegistrar(institutionRegistrarDTO);
		userActivityService.audit("Registrar", "Modified registrar details");
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, institutionRegistrarDTO.getId().toString()))
				.body(result);
	}
	/**
	 * DELETE /registrar/:id : delete the "id" registrar.
	 *
	 * @param id the id of the registrar to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/registrar/{id}")
	@Timed
	public ResponseEntity<Void> deleteEnrollments(@PathVariable Long id) {
		log.debug("REST request to delete Enrollments : {}", id);
		instituteRegistrarService.disableRegistser(id);
		userActivityService.audit("Registrar", "Deleted registrar");
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}

 
}
