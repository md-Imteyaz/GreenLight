package com.gl.platform.web.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gl.platform.domain.TeaSchoolCsvPositioning;
import com.gl.platform.domain.TeaStudentCsvPositioning;
import com.gl.platform.service.TeaFileVerifyService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class TeaFileVerifyResource {

	private final Logger log = LoggerFactory.getLogger(TeaFileVerifyResource.class);

	@Autowired
	private TeaFileVerifyService teaFildeVerifyService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/verify/studentfile")
	public ResponseEntity<?> verifyStudentTable(@RequestParam("file") MultipartFile file)
			throws URISyntaxException, IOException, BadRequestException {

		log.debug("REST request to validate the student file");

		if (file == null) {
			throw new BadRequestException("Please upload a valid file");
		}

		File createFile = File.createTempFile("TeaStudentFile", ".csv");
		file.transferTo(createFile);
		createFile.deleteOnExit();

		Path myPath = Paths.get(createFile.getAbsolutePath());
		Map<String, String> response = new HashMap<>();

		try (BufferedReader br = Files.newBufferedReader(myPath, StandardCharsets.UTF_8)) {

			HeaderColumnNameMappingStrategy<TeaStudentCsvPositioning> strategy = new HeaderColumnNameMappingStrategy<>();
			strategy.setType(TeaStudentCsvPositioning.class);

			CsvToBean csvToBean = new CsvToBeanBuilder(br).withType(TeaStudentCsvPositioning.class)
					.withMappingStrategy(strategy).withIgnoreLeadingWhiteSpace(true).build();
			List<TeaStudentCsvPositioning> students = csvToBean.parse();

			response = teaFildeVerifyService.verifyByStudentTable(students);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/verify/schoolfile")
	public ResponseEntity<?> verifySchoolTable(@RequestParam("file") MultipartFile file)
			throws URISyntaxException, IOException, BadRequestException {

		log.debug("REST request to validate the school file");

		if (file == null) {
			throw new BadRequestException("Please upload a valid file");
		}

		File createFile = File.createTempFile("TeaStudentFile", ".csv");
		file.transferTo(createFile);
		createFile.deleteOnExit();

		Path myPath = Paths.get(createFile.getAbsolutePath());
		Map<String, String> response = new HashMap<>();

		try (BufferedReader br = Files.newBufferedReader(myPath, StandardCharsets.UTF_8)) {

			HeaderColumnNameMappingStrategy<TeaSchoolCsvPositioning> strategy = new HeaderColumnNameMappingStrategy<>();
			strategy.setType(TeaSchoolCsvPositioning.class);

			CsvToBean csvToBean = new CsvToBeanBuilder(br).withType(TeaSchoolCsvPositioning.class)
					.withMappingStrategy(strategy).withIgnoreLeadingWhiteSpace(true).build();
			List<TeaSchoolCsvPositioning> schools = csvToBean.parse();

			response = teaFildeVerifyService.verifyBySchoolTable(schools);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
