package com.gl.platform;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transcript_requests", schema = "glp")
// @Table(name = "transcript_requests", schema = "glt")
public class CredentialRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "student_id")
	private String studentId;

	@Column(name = "status")
	private String status;

	@Column(name = "request_date")
	private Instant requestDate;

	@Column(name = "completion_date")
	private Instant completionDate;
	
	
	@Column(name = "additional_info")
	private String additionalInfo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Instant requestDate) {
		this.requestDate = requestDate;
	}

	public Instant getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Instant completionDate) {
		this.completionDate = completionDate;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	

}
