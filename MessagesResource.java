package com.gl.platform.web.rest;

import java.io.IOException;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gl.platform.service.MessageService;
import com.gl.platform.service.StudentUniversityDetailsDTO;
import com.gl.platform.service.UniversityStudentDetailsDTO;
import com.gl.platform.service.dto.MessageDTO;
import com.gl.platform.service.dto.MessageRequsetDTO;
import com.gl.platform.service.dto.MessageTrashCustomDTO;

@RestController
@RequestMapping("/api")
public class MessagesResource {
	
	private final Logger log = LoggerFactory.getLogger(MessagesResource.class);
	
	@Autowired
	private MessageService messageService;
	
	@GetMapping("/institute/to-list/{id}")
	@Timed
	public ResponseEntity<?> getStudentsByUserId(@PathVariable Long id) {
		log.debug("REST request to get Students and Admins");
		
		List<UniversityStudentDetailsDTO> resultStudents =
				messageService.getToListForInstitutionByUserId(id);
	
		return new ResponseEntity<> (resultStudents,HttpStatus.OK);
	}
	
	
	@GetMapping("/student/university-to-list/{userId}")
	@Timed
	public ResponseEntity<?> getUniversitiesByUserId(@PathVariable Long userId) {
		log.debug("REST request to get university by user Id : {}",userId);
		List<StudentUniversityDetailsDTO> result =  messageService.getToListForStudent(userId);
		return new ResponseEntity<> (result,HttpStatus.OK);
	}
		
	@PostMapping("/message")
	@Timed
	public @ResponseBody ResponseEntity<?> sendMessage(@RequestParam("message") String messageRequestDTO, @RequestParam(value = "attachments" ,required = false)
			 MultipartFile[] files) throws JsonParseException, JsonMappingException, IOException{
		
	
		log.debug("param 1 values: {}, param 2 : {}", messageRequestDTO,files );
		MessageRequsetDTO messageRequset = new ObjectMapper().readValue(messageRequestDTO, MessageRequsetDTO.class);
	
		
		MessageDTO result = messageService.save(messageRequset, files);
		return new ResponseEntity<> (result, HttpStatus.OK);
		
	}

	@PostMapping("/message/draft")
	@Timed
	public ResponseEntity<?> moveMessagesToDraft(@RequestParam("message") String messageRequestDTO,
			@RequestParam(value = "attachments" ,required = false) MultipartFile[] files) throws IOException{
		log.debug("REST request to move messages to draft");
		MessageRequsetDTO messageRequset = new ObjectMapper().readValue(messageRequestDTO, MessageRequsetDTO.class);
		
		Map<String, Object> response = messageService.moveMessagesToDraft(messageRequset, files);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	
	@GetMapping("/message/inbox/{userId}")
	@Timed
	public ResponseEntity<?> getInboxMessage(@PathVariable Long userId){
		log.debug("REST request to get inbox messages by userId : {}",userId);
		List<Object> result=messageService.getInboxMessages(userId);
		return new ResponseEntity<> (result,HttpStatus.OK);
		
	}
	
	
	@GetMapping("/message/sent/{userId}")
	@Timed
	public ResponseEntity<?> getSentMessage(@PathVariable Long userId){
		log.debug("REST request to get sent messages by userId :{}",userId);
		List<Object> result=messageService.getMessages(userId, "sent");
		return new ResponseEntity<> (result,HttpStatus.OK);
		
	}
	
	
	@GetMapping("/message/trash/{userId}")
	@Timed
	public ResponseEntity<?> getTrashMessage(@PathVariable Long userId){
		log.debug("REST request to get trash messages by userId : {}",userId);
		List<Object> result=messageService.getTrashMessages(userId);
		return new ResponseEntity<> (result,HttpStatus.OK);
		
	}

	
	
	
	@PostMapping("/message/inbox/trash")
	@Timed
	public ResponseEntity<?> moveMessagesToTrash(@RequestBody MessageTrashCustomDTO messageInfoDTO){
		Map<String, Object> response = messageService.moveMessagesToTrash(messageInfoDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	

	@PostMapping("/message/trash")
	@Timed
	public ResponseEntity<?> moveMessagesToTrashFromSent(@RequestBody MessageTrashCustomDTO messageInfoDTO){
		Map<String, Object> response = messageService.moveMessagesToTrashFromSent(messageInfoDTO);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	
	
	@GetMapping("message/draft/{userId}")
	@Timed
	public ResponseEntity<?> getDraftMessage(@PathVariable Long userId){
		log.debug("Rest request to get draft messages by userId : {}", userId);
		List<Object> result=messageService.getMessages(userId, "draft");
		return new ResponseEntity<> (result,HttpStatus.OK);
		
	}
}
