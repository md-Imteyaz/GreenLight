package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.GlParent;
import com.gl.platform.repository.UserRepository;
import com.gl.platform.service.GlParentQueryService;
import com.gl.platform.service.GlParentService;
import com.gl.platform.service.dto.GlParentCriteria;
import com.gl.platform.service.dto.GlParentDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.EmailAndUsernameAlreadyUsedException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;
import com.gl.platform.web.rest.errors.ParentLimitExceededException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;
import javassist.NotFoundException;

/**
 * REST controller for managing GlParent.
 */
@RestController
@RequestMapping("/api")
public class GlParentResource {

	private final Logger log = LoggerFactory.getLogger(GlParentResource.class);

	private static final String ENTITY_NAME = GlParent.class.getSimpleName().toLowerCase();

	private final GlParentService glParentService;

	private final GlParentQueryService glParentQueryService;

	@Autowired
	private UserRepository userRepository;

	public GlParentResource(GlParentService glParentService, GlParentQueryService glParentQueryService) {
		this.glParentService = glParentService;
		this.glParentQueryService = glParentQueryService;
	}

	/**
	 * POST /gl-parents : Create a new glParent.
	 *
	 * @param glParentDTO the glParentDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         glParentDTO, or with status 400 (Bad Request) if the glParent has
	 *         already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 */
	@PostMapping("/gl-parents")
	@Timed
	public ResponseEntity<GlParentDTO> createGlParent(@Valid @RequestBody GlParentDTO glParentDTO)
			throws URISyntaxException {
		log.debug("REST request to save GlParent : {}", glParentDTO);
		if (glParentDTO.getId() != null) {
			throw new BadRequestAlertException("A new glParent cannot already have an ID", ENTITY_NAME, "idexists");
		}
		GlParentDTO result = glParentService.save(glParentDTO);
		return ResponseEntity.created(new URI("/api/gl-parents/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * POST /gl-parents : Create a new glParent.
	 *
	 * @param glParentDTO the glParentDTO to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         glParentDTO, or with status 400 (Bad Request) if the glParent has
	 *         already an ID
	 * @return null then the limit is exceeded for the student of (3)
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws NotFoundException
	 */
	@PostMapping("/gl-parent")
	@Timed
	public ResponseEntity<GlParentDTO> createGlStudentsParent(@Valid @RequestBody GlParentDTO glParentDTO)
			throws URISyntaxException, NotFoundException {
		log.debug("REST request to save GlParent : {}", glParentDTO);
		/*
		 * if (glParentDTO.getId() != null) { throw new
		 * BadRequestAlertException("A new glParent cannot already have an ID",
		 * ENTITY_NAME, "idexists"); }
		 */

		if (glParentDTO.getStudentId() == null) {
			throw new BadRequestAlertException("Invalid student id", ENTITY_NAME, "studentidnull");
		}
		if (userRepository.findOneByLogin(glParentDTO.getUsername().toLowerCase()).isPresent()
				&& userRepository.findOneByEmailIgnoreCase(glParentDTO.getEmail()).isPresent()) {
			throw new EmailAndUsernameAlreadyUsedException();
		}
		userRepository.findOneByLogin(glParentDTO.getUsername().toLowerCase()).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});
		userRepository.findOneByEmailIgnoreCase(glParentDTO.getEmail()).ifPresent(u -> {
			throw new EmailAlreadyUsedException();
		});

		GlParentDTO result = glParentService.saveGlStudentsParent(glParentDTO);

		if (result == null) {
			throw new ParentLimitExceededException();
		}
		return ResponseEntity.created(new URI("/api/gl-parents/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString())).body(result);
	}

	/**
	 * GET /gl-parents : get all the glParents.
	 *
	 * @param pageable the pagination information
	 * @param criteria the criterias which the requested entities should match
	 * @return the ResponseEntity with status 200 (OK) and the list of glParents in
	 *         body
	 */
	@GetMapping("/gl-parents")
	@Timed
	public ResponseEntity<List<GlParentDTO>> getAllGlParents(GlParentCriteria criteria, Pageable pageable) {
		log.debug("REST request to get GlParents by criteria: {}", criteria);
		Page<GlParentDTO> page = glParentQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/gl-parents");
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /gl-parents/:id : get the "id" glParent.
	 *
	 * @param id the id of the glParentDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         glParentDTO, or with status 404 (Not Found)
	 */
	@GetMapping("/gl-parents/{id}")
	@Timed
	public ResponseEntity<GlParentDTO> getGlParent(@PathVariable Long id) {
		log.debug("REST request to get GlParent : {}", id);
		Optional<GlParentDTO> glParentDTO = glParentService.findOne(id);
		return ResponseUtil.wrapOrNotFound(glParentDTO);
	}

	/**
	 * DELETE /gl-parents/:id : delete the "id" glParent.
	 *
	 * @param id the id of the glParentDTO to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/gl-parents/{id}")
	@Timed
	public ResponseEntity<Void> deleteGlParent(@PathVariable Long id) {
		log.debug("REST request to delete GlParent : {}", id);
		glParentService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
	}
}
