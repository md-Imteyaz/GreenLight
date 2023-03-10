package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.gl.platform.domain.GlParentStudent;
import com.gl.platform.service.GlParentStudentQueryService;
import com.gl.platform.service.GlParentStudentService;
import com.gl.platform.service.dto.GlParentDTO;
import com.gl.platform.service.dto.GlParentStudentCriteria;
import com.gl.platform.service.dto.GlParentStudentDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing GlParentStudent.
 */
@RestController
@RequestMapping("/api")
public class GlParentStudentResource {

	private final Logger log = LoggerFactory.getLogger(GlParentStudentResource.class);

	private static final String ENTITY_NAME = GlParentStudent.class.getSimpleName().toLowerCase();

	private final GlParentStudentService glParentStudentService;
	
	private final GlParentStudentQueryService glParentStudentQueryService;

	public GlParentStudentResource(GlParentStudentService glParentStudentService,
			GlParentStudentQueryService glParentStudentQueryService) {
		this.glParentStudentService = glParentStudentService;
		this.glParentStudentQueryService = glParentStudentQueryService;
	}

	/**
	 * POST /gl-parent-students : Create a new glParentStudent.
	 *
	 * @param glParentStudentDTO the glParentStudentDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         glParentStudentDTO, or with status 400 (Bad Request) if the
	 *         glParentStudent has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/gl-parent-students")
	@Timed
	public ResponseEntity<GlParentStudentDTO> createGlParentStudent(
			@Valid @RequestBody GlParentStudentDTO glParentStudentDTO) throws URISyntaxException {
		log.debug("REST request to save GlParentStudent : {}", glParentStudentDTO);
		if (glParentStudentDTO.getId() != null) {
			throw new BadRequestAlertException("A new glParentStudent cannot already have an ID", ENTITY_NAME,
					"idexists");
		}
		GlParentStudentDTO result = glParentStudentService.save(glParentStudentDTO);
		return ResponseEntity.created(new URI("/api/gl-parent-students/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * PUT /gl-parent-students : Updates an existing glParentStudent.
	 *
	 * @param glParentStudentDTO the glParentStudentDTO to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         glParentStudentDTO, or with status 400 (Bad Request) if the
	 *         glParentStudentDTO is not valid, or with status 500 (Internal Server
	 *         Error) if the glParentStudentDTO couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/gl-parent-students")
	@Timed
	public ResponseEntity<GlParentStudentDTO> updateGlParentStudent(
			@Valid @RequestBody GlParentStudentDTO glParentStudentDTO) throws URISyntaxException {
		log.debug("REST request to update GlParentStudent : {}", glParentStudentDTO);
		if (glParentStudentDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		GlParentStudentDTO result = glParentStudentService.save(glParentStudentDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, glParentStudentDTO.getId().toString()))
				.body(result);
	}

	/**
	 * GET /gl-parent-students : get all the glParentStudents.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         glParentStudents in body
	 */
	@GetMapping("/gl-parent-students")
	@Timed
	public ResponseEntity<List<GlParentStudentDTO>> getAllGlParentStudents(GlParentStudentCriteria criteria,
			Pageable pageable) {
		log.debug("REST request to get GlParentStudents by criteria: {}", criteria);
		Page<GlParentStudentDTO> page = glParentStudentQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/gl-parent-students");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}
	
	
	
	/**
	 * GET /gl-parent-students : get all the glParentStudents.
	 *
	 * @param pageable the pagination information
	 * @param user Id to populate the list of parents for Student
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         glParentStudents in body
	 */
	@GetMapping("/gl-parent-student/{userId}")
	@Timed
	public ResponseEntity<?> getAllGlParentStudents(@PathVariable Long userId) {
		List<GlParentDTO> result = glParentStudentService.getParentsStudents(userId);
		return new ResponseEntity<>(result,HttpStatus.OK);
	}
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * GET /gl-parent-students/:id : get the "id" glParentStudent.
	 *
	 * @param id the id of the glParentStudentDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         glParentStudentDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/gl-parent-students/{id}")
	@Timed
	public ResponseEntity<GlParentStudentDTO> getGlParentStudent(@PathVariable Long id) {
		log.debug("REST request to get GlParentStudent : {}", id);
		Optional<GlParentStudentDTO> glParentStudentDTO = glParentStudentService.findOne(id);
		return ResponseUtil.wrapOrNotFound(glParentStudentDTO);
	}

	/**
	 * DELETE /gl-parent-students/:id : delete the "id" glParentStudent.
	 *
	 * @param id the id of the glParentStudentDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/gl-parent-students/{id}")
	@Timed
	public ResponseEntity<Void> deleteGlParentStudent(@PathVariable Long id) {
		log.debug("REST request to delete GlParentStudent : {}", id);
		glParentStudentService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
	
	
	@DeleteMapping("/gl-parent-student/{parentId}")
	@Timed
	public ResponseEntity<Void> deleteGlParentByParentId(@PathVariable Long parentId) {
		log.debug("REST request to delete GlParentStudent by Parent Id : {}", parentId);
	
		glParentStudentService.deleteByParentId(parentId);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, parentId.toString())).build();
	}
	
	
	
}
