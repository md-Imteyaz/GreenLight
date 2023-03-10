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
import com.gl.platform.domain.Address;
import com.gl.platform.service.AddressQueryService;
import com.gl.platform.service.AddressService;
import com.gl.platform.service.dto.AddressCriteria;
import com.gl.platform.service.dto.AddressDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;
import com.gl.platform.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Address.
 */
@RestController
@RequestMapping("/api")
public class AddressResource {

	private final Logger log = LoggerFactory.getLogger(AddressResource.class);

	private static final String ENTITY_NAME = Address.class.getSimpleName().toLowerCase();

	private final AddressService addressService;

	private final AddressQueryService addressQueryService;

	public AddressResource(AddressService addressService, AddressQueryService addressQueryService) {
		this.addressService = addressService;
		this.addressQueryService = addressQueryService;
	}

	/**
	 * GET /addresses/:id : get the "id" address.
	 *
	 * @param id the id of the addressDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the addressDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/addresses/{id}")
	@Timed
	public ResponseEntity<AddressDTO> getAddress(@PathVariable Long id) {
		log.debug("REST request to get Address : {}", id);
		Optional<AddressDTO> addressDTO = addressService.findOne(id);
		return ResponseUtil.wrapOrNotFound(addressDTO);
	}
}
