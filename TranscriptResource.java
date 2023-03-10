package com.gl.platform.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.repository.UniversityRepository;
import com.gl.platform.service.AuthorizationService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.InstitutionService;
import com.gl.platform.service.S3StorageService;
import com.gl.platform.service.TranscriptCourseQueryService;
import com.gl.platform.service.TranscriptService;
import com.gl.platform.service.TranscriptSharedService;
import com.gl.platform.service.TranscriptsService;
import com.gl.platform.service.UserActivityQueryService;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.InstitutionDTO;
import com.gl.platform.service.dto.NamedInputStreamWrapper;
import com.gl.platform.service.dto.TranscriptCourseCriteria;
import com.gl.platform.service.dto.TranscriptCourseDTO;
import com.gl.platform.service.dto.TranscriptDTO;
import com.gl.platform.service.dto.TranscriptSharedDTO;
import com.gl.platform.service.dto.TranscriptStatus;
import com.gl.platform.service.dto.TranscriptVerification;
import com.gl.platform.service.dto.TranscriptsStatusReasonDTO;
import com.gl.platform.service.dto.UserActivityCriteria;
import com.gl.platform.service.dto.UserActivityDTO;
import com.gl.platform.web.rest.errors.BadRequestAlertException;
import com.google.common.hash.Hashing;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * REST controller for managing Transcript.
 */
@RestController
@RequestMapping("/api")
public class TranscriptResource {
	private final Logger log = LoggerFactory.getLogger(TranscriptResource.class);

	private static final String ENTITY_NAME = "transcripts";

	private final TranscriptService transcriptService;

	private final InstitutionService institutionService;

	private final TranscriptCourseQueryService transcriptCourseQueryService;

	@Autowired
	private TranscriptSharedService transcriptSharedService;

	@Autowired
	private UserActivityQueryService userActivityQueryService;

	@Autowired
	private GlUserService glUserService;

	@Autowired
	private S3StorageService s3StorageService;

	@Autowired
	private UniversityRepository universityRepository;

	@Autowired
	private TranscriptsService transcriptsService;

	@Autowired
	private AuthorizationService authorizationService;

	public TranscriptResource(TranscriptService transcriptService, InstitutionService institutionService,
			TranscriptCourseQueryService transcriptCourseQueryService) {
		this.transcriptService = transcriptService;
		this.transcriptCourseQueryService = transcriptCourseQueryService;
		this.institutionService = institutionService;
	}

	/**
	 * GET /transcript/full/{id} get all transcripts with the userId {id}
	 * 
	 *
	 * @param id : the userId
	 * @return the ResponseEntity with status 200 (OK) and the list of transcript
	 *         and institution body
	 */

	@GetMapping("/transcript/full/{id}")
	@Timed
	public ResponseEntity<?> getAllTranscriptsAndInstitutions(@PathVariable Long id) {

		log.debug("Request to transcript");

		List<Object> response = transcriptService.validateAndGetTranscriptFullByIdOptimized(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/transcript/{id}")
	@Timed
	public ResponseEntity<?> getAllTranscripts(@PathVariable Long id) {

		Optional<TranscriptDTO> transcripts = transcriptService.findOne(id);

		Map<String, Object> response = new HashMap<>();

		if (transcripts.isPresent()) {

			if (!authorizationService.isSupport() && !authorizationService.isReciever()
					&& !authorizationService.isGreenLightAdmin() && !authorizationService.isSiteAdmin()
					&& !authorizationService.ownedByUser(transcripts.get().getUserId())) {
				throw new AccessDeniedException("not the owner");
			}

			TranscriptDTO transcript = transcripts.get();

			List<TranscriptsStatusReasonDTO> statusReasons = universityRepository
					.findTranscriptsOnHoldByTranscriptId(transcript.getId());

			if (statusReasons != null && !statusReasons.isEmpty()) {
				transcript.setStatus(TranscriptStatus.HOLD.getValue());
			}

			transcripts.get().setTranscriptCourses(null);// setting courses null to send them separately in response
															// object
			String s3Path = "transcripts/" + transcripts.get().getId() + "/pdf_transcript";
			transcripts.get().setUrl(s3StorageService.generatePresignedUrl(s3Path));
			String s3ViewPath = "transcripts/" + transcripts.get().getId() + "/pdf_transcript_view";
			transcripts.get().setViewUrl(s3StorageService.generatePresignedUrl(s3ViewPath));
			String s3DownloadPath = "transcripts/" + transcripts.get().getId() + "/pdf_transcript_student";

			try (NamedInputStreamWrapper file = s3StorageService.retrieve(s3DownloadPath)) {
				if (file == null) {
					transcripts.get().setStudentUrl(s3StorageService.generatePresignedUrl(s3Path));
				} else {
					try {
						if (file != null) {
							file.close();
						}
					} catch (Exception ex) {
						log.error("error closing file", ex);
					}
					transcripts.get().setStudentUrl(s3StorageService.generatePresignedUrl(s3DownloadPath));
				}
			} catch (Exception ex) {
				transcripts.get().setStudentUrl(s3StorageService.generatePresignedUrl(s3DownloadPath));
			}
			response.put("transcript", transcript);
			GlUserDTO glUserDTO = glUserService.findOne(transcripts.get().getUserId());
			glUserDTO.setJhiPassword(null);
			response.put("glUser", glUserDTO);

			LongFilter filterByTranscriptId = new LongFilter();
			filterByTranscriptId.setEquals(transcripts.get().getId());
			TranscriptCourseCriteria criteria = new TranscriptCourseCriteria();
			criteria.setTranscriptId(filterByTranscriptId);

			List<TranscriptCourseDTO> courses = transcriptCourseQueryService.findByCriteria(criteria);
			response.put("transcriptCourses", courses);

			LongFilter filterByUserId = new LongFilter();
			filterByUserId.setEquals(transcripts.get().getUserId());
			UserActivityCriteria activityCriteria = new UserActivityCriteria();
			activityCriteria.setUserId(filterByUserId);

			StringFilter filterByType = new StringFilter();
			filterByType.setEquals("Transcript");
			activityCriteria.setActivityName(filterByType);

			List<UserActivityDTO> userActivities = userActivityQueryService.findByCriteria(activityCriteria);
			response.put("userActivities", userActivities);

			Optional<InstitutionDTO> institution = institutionService.findOne(transcripts.get().getInstituteId());
			if (institution.isPresent()) {
				response.put("institution", institution);
			}

		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/shares/{id}/transcript")
	@Timed
	public ResponseEntity<?> getTranscriptByShareId(@PathVariable Long id) {

		List<TranscriptSharedDTO> optTranscriptsShareDTO = transcriptSharedService.findByShareId(id);

		Map<String, Object> response = new HashMap<>();

		if (optTranscriptsShareDTO != null && optTranscriptsShareDTO.size() == 1) {

			TranscriptSharedDTO transcriptShareDTO = optTranscriptsShareDTO.iterator().next();

			String transcriptPath = "shared/" + transcriptShareDTO.getId();

			Optional<TranscriptDTO> optTranscripts = transcriptService.findOne(transcriptShareDTO.getTranscriptId());

			if (optTranscripts.isPresent()) {

				String s3Path = transcriptPath + "/pdf_transcript";

				TranscriptDTO transcript = optTranscripts.get();

				transcript.setTranscriptCourses(null);// setting courses null to send them separately in response object
				transcript.setUrl(s3StorageService.generatePresignedUrl(s3Path));

				String s3ViewPath = transcriptPath + "/pdf_transcript_view";

				transcript.setViewUrl(s3StorageService.generatePresignedUrl(s3ViewPath));

				response.put("transcript", transcript);

				LongFilter filterByTranscriptId = new LongFilter();
				filterByTranscriptId.setEquals(transcript.getId());
				TranscriptCourseCriteria criteria = new TranscriptCourseCriteria();
				criteria.setTranscriptId(filterByTranscriptId);

				List<TranscriptCourseDTO> courses = transcriptCourseQueryService.findByCriteria(criteria);
				response.put("transcriptCourses", courses);

				LongFilter filterByUserId = new LongFilter();
				filterByUserId.setEquals(transcript.getUserId());
				UserActivityCriteria activityCriteria = new UserActivityCriteria();
				activityCriteria.setUserId(filterByUserId);

				StringFilter filterByType = new StringFilter();
				filterByType.setEquals("Transcript");
				activityCriteria.setActivityName(filterByType);

				Optional<InstitutionDTO> institution = institutionService.findOne(transcript.getInstituteId());
				if (institution.isPresent()) {
					response.put("institution", institution);
				}
			}
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * POST /validatetranscriptsrequests : Validate transcript pdf file upload
	 *
	 * @param files
	 * @param type  {@link String} is the type of the credential
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         transcriptsDTO, or with status 400 (Bad Request) if the transcripts
	 *         has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException
	 */
	@PostMapping("/validatetranscriptsrequests")
	@Timed
	public @ResponseBody ResponseEntity<TranscriptVerification> validateTranscipt(
			@RequestParam("files") MultipartFile[] files) throws URISyntaxException, ParseException {

		log.debug("Came to Transcript Upload Method");

		if (files == null) {
			throw new BadRequestAlertException(" Transcript File missing", ENTITY_NAME, "idnull");
		} else {
			log.debug("Input Parameters: files list:{}", files.length);
		}

		log.info("Files Came through : {}", files.length);

		TranscriptVerification transcriptVerification = new TranscriptVerification();

		for (int i = 0; i < files.length; i++) {

			try {

				String transcript = IOUtils.toString(files[i].getInputStream(), "UTF-8");

				String transcriptSha256hex = Hashing.sha256().hashString(transcript, StandardCharsets.UTF_8).toString();
				transcriptVerification = transcriptsService.validateTranscript(transcriptSha256hex);

			} catch (IOException e) {
				log.error("Error occured access the file", e);
			}

		}

		return ResponseEntity.created(new URI("/api/validatetranscriptsrequests")).body(transcriptVerification);
	}

	/**
	 * to download manually
	 * 
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/edi/download/12381723612873")
	@Timed
	public ResponseEntity<?> getEDIFromS3() throws IOException {

		log.debug("Request to transcript");

		Map<String, String> response = transcriptService.downloadEDIFromSpeede();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
