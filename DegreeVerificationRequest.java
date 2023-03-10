package com.gl.platform.web.rest;

import java.util.Map;
import java.util.Set;

public class DegreeVerificationRequest {

	private String studentName;
	private String dob;
	private String institutionFrom;
	private String date;
	private Set<Map<String, Object>> degrees;
	private String presignedUrl;
	private String studentEmail;
	private String verifierEmail;
	private String blockchainHash;
	private String verifierName;
	private String institutionType;
	private Boolean multipleDegrees;
	private String degreeDate;
	private String companyName;
	
	public String getStudentName() {
		return studentName;
	}
	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	
	public String getInstitutionFrom() {
		return institutionFrom;
	}
	public void setInstitutionFrom(String institutionFrom) {
		this.institutionFrom = institutionFrom;
	}
	
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public Set<Map<String, Object>> getDegrees() {
		return degrees;
	}
	public void setDegrees(Set<Map<String, Object>> degrees) {
		this.degrees = degrees;
	}
	public String getPresignedUrl() {
		return presignedUrl;
	}
	public void setPresignedUrl(String presignedUrl) {
		this.presignedUrl = presignedUrl;
	}
	public String getStudentEmail() {
		return studentEmail;
	}
	public void setStudentEmail(String studentEmail) {
		this.studentEmail = studentEmail;
	}
	public String getVerifierEmail() {
		return verifierEmail;
	}
	public void setVerifierEmail(String verifierEmail) {
		this.verifierEmail = verifierEmail;
	}
	public String getBlockchainHash() {
		return blockchainHash;
	}
	public void setBlockchainHash(String blockchainHash) {
		this.blockchainHash = blockchainHash;
	}
	public String getVerifierName() {
		return verifierName;
	}
	public void setVerifierName(String verifierName) {
		this.verifierName = verifierName;
	}
	public String getInstitutionType() {
		return institutionType;
	}
	public void setInstitutionType(String institutionType) {
		this.institutionType = institutionType;
	}
	public Boolean getMultipleDegrees() {
		return multipleDegrees;
	}
	public void setMultipleDegrees(Boolean multipleDegrees) {
		this.multipleDegrees = multipleDegrees;
	}
	public String getDegreeDate() {
		return degreeDate;
	}
	public void setDegreeDate(String degreeDate) {
		this.degreeDate = degreeDate;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	@Override
	public String toString() {
		return "DegreeVerificationRequest [studentName=" + studentName + ", dob=" + dob + ", institutionFrom="
				+ institutionFrom + ", date=" + date + ", degrees=" + degrees + ", presignedUrl=" + presignedUrl
				+ ", studentEmail=" + studentEmail + ", verifierEmail=" + verifierEmail + ", blockchainHash="
				+ blockchainHash + ", verifierName=" + verifierName + ", institutionType=" + institutionType
				+ ", multipleDegrees=" + multipleDegrees + ", degreeDate=" + degreeDate + ", companyName=" + companyName
				+ "]";
	}
}
