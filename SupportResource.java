package com.gl.platform.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.service.SupportService;
import com.gl.platform.service.dto.ContactUsDTO;
import com.gl.platform.service.dto.GlSupportTicketDTO;

/**
 * Controller Support Ticket.
 */
@RestController
@RequestMapping("/api")
public class SupportResource {

	@Autowired
	private SupportService supportService;

	@GetMapping("/supportcases/{id}")
	@Timed
	public GlSupportTicketDTO getTicket(String ticketNumber) {
		return new GlSupportTicketDTO();
	}

	@PostMapping("/supportcases")
	@Timed
	public ResponseEntity<GlSupportTicketDTO> createSupportTicket(@RequestBody GlSupportTicketDTO glSupportTicketDTO) {
		String ticketNumber = supportService.createSupportCase(glSupportTicketDTO);
		glSupportTicketDTO.setTicketNumber(ticketNumber);

		return ResponseEntity.ok().body(glSupportTicketDTO);
	}
	
	
	@PostMapping("/contactus")
	@Timed
	public ResponseEntity<ContactUsDTO> createContactUsRequest(@RequestBody ContactUsDTO contactUsDTO) {
		supportService.createContactUsRequest(contactUsDTO);
		return ResponseEntity.ok().body(contactUsDTO);
	}
	
}
