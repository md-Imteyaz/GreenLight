package com.gl.platform.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.gl.platform.domain.Country;
import com.gl.platform.service.CountryService;
import com.gl.platform.service.dto.CountryDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.gl.platform.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Country.
 */
@RestController
@RequestMapping("/api")
public class CountryResource {

	private final Logger log = LoggerFactory.getLogger(CountryResource.class);

	private static final String ENTITY_NAME = Country.class.getSimpleName().toLowerCase();

	private final CountryService countryService;

	public CountryResource(CountryService countryService) {
		this.countryService = countryService;
	}

	/**
	 * GET /countries : get all the countries.
	 *
	 * @return the ResponseEntity with status 200 (OK) and the list of countries in
	 *         body
	 */
	@GetMapping("/countries")
	@Timed
	public List<CountryDTO> getAllCountries() {
		log.debug("REST request to get all Countries");
		return countryService.findAll();
	}

	/**
	 * GET /countries/:id : get the "id" country.
	 *
	 * @param id the id of the countryDTO to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the countryDTO,
	 *         or with status 404 (Not Found)
	 */
	@GetMapping("/countries/{id}")
	@Timed
	public ResponseEntity<CountryDTO> getCountry(@PathVariable Long id) {
		log.debug("REST request to get Country : {}", id);
		Optional<CountryDTO> countryDTO = countryService.findOne(id);
		return ResponseUtil.wrapOrNotFound(countryDTO);
	}

	@GetMapping("/countries-list")
	@Timed
	public ResponseEntity<List<CountryDTO>> getCountryList() {
		log.debug("REST request to get Country List ");
		List<CountryDTO> countryList = countryService.getCountries();
		return new ResponseEntity<>(countryList,HttpStatus.OK);
	}
	
}
