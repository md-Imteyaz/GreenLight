package com.gl.platform.web.rest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gl.platform.domain.Enrollment;
import com.gl.platform.domain.GlStudent;
import com.gl.platform.repository.EnrollmentRepository;
import com.gl.platform.repository.GlStudentRepository;
import com.gl.platform.service.DeltaFileService;
import com.gl.platform.service.S3StorageService;
import com.gl.platform.service.TrexDataLoadService;
import com.gl.platform.service.TrexDataLoaderManualService;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class TrexResource {

	@Autowired
	private TrexDataLoadService dataLoadingService;

	@Autowired
	private S3StorageService s3;

	@Autowired
	private DeltaFileService delta;

	@Autowired
	private static GlStudentRepository glStudentRepository;

	@Autowired
	private static EnrollmentRepository enrollmentRepo;

	@Autowired
	private TrexDataLoaderManualService trexService;

	private final Logger log = LoggerFactory.getLogger(TrexResource.class);

	private static final String ENTITY_NAME = TrexResource.class.getSimpleName().toLowerCase();

	@GetMapping("/get/enrollment")
	public String dataLoad() throws IllegalStateException, IOException {

		File file = new File("/home/yashu/Documents/studentWithEnrollmentsProdBackup.csv");

		try {
			FileWriter outputfile = new FileWriter(file);

			CSVWriter writer = new CSVWriter(outputfile);

			String[] header = { "studentNumber", "enrollmentUUID" };
			writer.writeNext(header);
			FileReader filereader = new FileReader("/home/yashu/Music/CorrectStudentsInBkpStudentNumber.csv");

			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			List<String[]> allData = csvReader.readAll();

			for (String[] row : allData) {
				for (String cell : row) {

					Optional<GlStudent> student = glStudentRepository.findBySchoolStudentId(cell);
					if (student.isPresent()) {
						Enrollment enrollment = enrollmentRepo.findByStudentId(student.get().getId());

						if (enrollment != null) {
							String[] data1 = { cell, enrollment.getEnrollmentUuid() };
							System.out.println("Loaded student number : " + cell);
							writer.writeNext(data1);
						}
					}
				}
			}
			writer.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return "Delta Files Generated Successfully";
	}

	@GetMapping("/dataload/{institution}/{filename}")
	public ResponseEntity<?> deltaLoad(@PathVariable String institution, @PathVariable String filename)
			throws IllegalStateException, IOException {
		log.debug("REST request to generate the csv files");
		if (institution == null && filename == null) {
			throw new BadRequestAlertException("Please select valid institution and file name", ENTITY_NAME,
					"institutionorfilenamenull");
		}
		Map<String, String> response = new HashMap<>();
		response = delta.getdeltaFiles(institution, filename);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/dataload/getobjects/{institution}")
	public ResponseEntity<?> getObjects(@PathVariable String institution) {
		log.debug("REST request to get list of objects and path of file");

		if (institution == null) {
			throw new BadRequestAlertException("Please select valid institution ", ENTITY_NAME,
					"institutionorfilenamenull");
		}
		Map<String, List<Map<String, String>>> response = new HashMap<>();
		List<Map<String, String>> objectsList = new ArrayList<>();
		Map<String, String> filePath = new HashMap<>();
		List<Map<String, String>> filePaths = new ArrayList<>();
		String path = null;
		if (institution.equals("misd")) {
			path = "users/greenlight/isd-xml-files/mesquiteisd/";
			objectsList = s3.getFiles(path);
			filePath.put("filePath", "glsftpbucket/" + path);
			filePaths.add(filePath);

			response.put("FilePath", filePaths);
			response.put("FileDetails", objectsList);
		} else if (institution.equals("gpisd")) {
			path = "users/gpisd/";

			filePath.put("filePath", "glsftpbucket/" + path);
			filePaths.add(filePath);

			objectsList = s3.getFiles(path);

			response.put("FilePath", filePaths);
			response.put("FileDetails", objectsList);
		} else if (institution.equals("disd")) {
			path = "users/disd/";

			filePath.put("filePath", "glsftpbucket/" + path);
			filePaths.add(filePath);

			response.put("FilePath", filePaths);
		} else if (institution.equals("texanscan")) {
			path = "users/texans/";

			filePath.put("filePath", "glsftpbucket/" + path);
			filePaths.add(filePath);

			response.put("FilePath", filePaths);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/trexfile/load")
	public ResponseEntity<String> createISD(@RequestParam("files") MultipartFile file,
			@RequestParam("universityId") Long universityId) throws BadRequestException, IOException, JAXBException {
		log.info("REST request to upload all the values from xml");
		if (file == null) {
			throw new BadRequestException("Please select valid file ");
		}
		String response = trexService.dataLoaderManual(file, universityId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/student/ext/load")
	public ResponseEntity<Map<String, Object>> loadStudentExt(@RequestParam("files") MultipartFile file,
			@RequestParam("universityId") Long universityId) throws BadRequestException, IOException {
		log.info("REST request to upload all the values from csv");
		if (file == null) {
			throw new BadRequestException("Please select valid file ");
		}
		Map<String, Object> response = trexService.studentExtFileDataLoad(file, universityId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
