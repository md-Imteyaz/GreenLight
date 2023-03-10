package com.gl.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Greenlightapp.
 * <p>
 * Properties are configured in the application.yml file. See
 * {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private String pdfurl;

	private String pdfcrowdUser;

	private String pdfcrowdKey;

	private String blockchainURL;

	private String transcriptRequestURL;

	private String institutionIntegrationBrokerURL;

	private String hostURL;

	private String salesForceAuthURL;

	private String presignedUrlValidLength;

	private String salesForceRevokeURL;

	private String appClientSecret;

	private String appClientKey;

	private String s3BucketName;

	private String awsAccessKey;

	private String awsSecretKey;

	private String transcriptEdiType;

	private String contactUsEmail;

	private String sftpS3BucketName;

	private String hsRegSigPath;

	private String dellSnapKey;

	private String edreadyUrl;

	private String sarUploadUrl;

	private long shareUrlExpiration;

	private String mobileAppId;

	private String mobileAppKey;

	private String pushNotificationsUrl;

	private String ssnapUrl;

	private String ssnapRedirectUrl;

	private String ssnapRedirectApi;

	private String ssnapProfileApi;

	private String ssnapTokenApi;

	private String ssnapToken;

	private String ssnapClientId;

	private String nsapiProfileUrl;

	private String nsapiKey;

	private String bulkUploadCerts;

	private String zohoClientSecret;

	private String zohoClientId;

	private String zohoRefreshToken;

	private String jobNotificationEmail;

	private String edreadyRedirectUrl;

	private String adManagmentPath;
	
	public String getSsnapRedirectApi() {
		return ssnapRedirectApi;
	}

	public void setSsnapRedirectApi(String ssnapRedirectApi) {
		this.ssnapRedirectApi = ssnapRedirectApi;
	}

	public String getSsnapProfileApi() {
		return ssnapProfileApi;
	}

	public void setSsnapProfileApi(String ssnapProfileApi) {
		this.ssnapProfileApi = ssnapProfileApi;
	}

	public String getSsnapTokenApi() {
		return ssnapTokenApi;
	}

	public void setSsnapTokenApi(String ssnapTokenApi) {
		this.ssnapTokenApi = ssnapTokenApi;
	}

	public String getEdreadyUrl() {
		return edreadyUrl;
	}

	public void setEdreadyUrl(String edreadyUrl) {
		this.edreadyUrl = edreadyUrl;
	}

	public String getDellSnapKey() {
		return dellSnapKey;
	}

	public void setDellSnapKey(String dellSnapKey) {
		this.dellSnapKey = dellSnapKey;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAppClientSecret() {
		return appClientSecret;
	}

	public void setAppClientSecret(String appClientSecret) {
		this.appClientSecret = appClientSecret;
	}

	public String getAppClientKey() {
		return appClientKey;
	}

	public void setAppClientKey(String appClientKey) {
		this.appClientKey = appClientKey;
	}

	public String getPdfurl() {
		return pdfurl;
	}

	public void setPdfurl(String pdfurl) {
		this.pdfurl = pdfurl;
	}

	public String getPdfcrowdUser() {
		return pdfcrowdUser;
	}

	public void setPdfcrowdUser(String pdfcrowdUser) {
		this.pdfcrowdUser = pdfcrowdUser;
	}

	public String getPdfcrowdKey() {
		return pdfcrowdKey;
	}

	public void setPdfcrowdKey(String pdfcrowdKey) {
		this.pdfcrowdKey = pdfcrowdKey;
	}

	public String getSalesForceAuthURL() {
		return salesForceAuthURL;
	}

	public void setSalesForceAuthURL(String salesForceAuthURL) {
		this.salesForceAuthURL = salesForceAuthURL;
	}

	public String getSalesForceTokenURL() {
		return salesForceTokenURL;
	}

	public void setSalesForceTokenURL(String salesForceTokenURL) {
		this.salesForceTokenURL = salesForceTokenURL;
	}

	private String salesForceTokenURL;

	public String getBlockchainURL() {
		return blockchainURL;
	}

	public void setBlockchainURL(String blockchainURL) {
		this.blockchainURL = blockchainURL;
	}

	public String getHostURL() {
		return hostURL;
	}

	public void setHostURL(String hostURL) {
		this.hostURL = hostURL;
	}

	public String getPresignedUrlValidLength() {
		return presignedUrlValidLength;
	}

	public void setPresignedUrlValidLength(String presignedUrlValidLength) {
		this.presignedUrlValidLength = presignedUrlValidLength;
	}

	public String getSalesForceRevokeURL() {
		return salesForceRevokeURL;
	}

	public void setSalesForceRevokeURL(String salesForceRevokeURL) {
		this.salesForceRevokeURL = salesForceRevokeURL;
	}

	public String getS3BucketName() {
		return s3BucketName;
	}

	public void setS3BucketName(String s3BucketName) {
		this.s3BucketName = s3BucketName;
	}

	public String getTranscriptRequestURL() {
		return transcriptRequestURL;
	}

	public void setTranscriptRequestURL(String transcriptRequestURL) {
		this.transcriptRequestURL = transcriptRequestURL;
	}

	public String getInstitutionIntegrationBrokerURL() {
		return institutionIntegrationBrokerURL;
	}

	public void setInstitutionIntegrationBrokerURL(String institutionIntegrationBrokerURL) {
		this.institutionIntegrationBrokerURL = institutionIntegrationBrokerURL;
	}

	public String getTranscriptEdiType() {
		return transcriptEdiType;
	}

	public void setTranscriptEdiType(String transcriptEdiType) {
		this.transcriptEdiType = transcriptEdiType;
	}

	public String getContactUsEmail() {
		return contactUsEmail;
	}

	public void setContactUsEmail(String contactUsEmail) {
		this.contactUsEmail = contactUsEmail;
	}

	public String getSftpS3BucketName() {
		return sftpS3BucketName;
	}

	public void setSftpS3BucketName(String sftpS3BucketName) {
		this.sftpS3BucketName = sftpS3BucketName;
	}

	public String getHsRegSigPath() {
		return hsRegSigPath;
	}

	public void setHsRegSigPath(String hsRegSigPath) {
		this.hsRegSigPath = hsRegSigPath;
	}

	public long getShareUrlExpiration() {
		return shareUrlExpiration;
	}

	public void setShareUrlExpiration(long shareUrlExpiration) {
		this.shareUrlExpiration = shareUrlExpiration;
	}

	public String getSarUploadUrl() {
		return sarUploadUrl;
	}

	public void setSarUploadUrl(String sarUploadUrl) {
		this.sarUploadUrl = sarUploadUrl;
	}

	public String getMobileAppId() {
		return mobileAppId;
	}

	public void setMobileAppId(String mobileAppId) {
		this.mobileAppId = mobileAppId;
	}

	public String getMobileAppKey() {
		return mobileAppKey;
	}

	public void setMobileAppKey(String mobileAppKey) {
		this.mobileAppKey = mobileAppKey;
	}

	public String getPushNotificationsUrl() {
		return pushNotificationsUrl;
	}

	public void setPushNotificationsUrl(String pushNotificationsUrl) {
		this.pushNotificationsUrl = pushNotificationsUrl;
	}

	public String getSsnapUrl() {
		return ssnapUrl;
	}

	public void setSsnapUrl(String ssnapUrl) {
		this.ssnapUrl = ssnapUrl;
	}

	public String getSsnapRedirectUrl() {
		return ssnapRedirectUrl;
	}

	public void setSsnapRedirectUrl(String ssnapRedirectUrl) {
		this.ssnapRedirectUrl = ssnapRedirectUrl;
	}

	public String getSsnapClientId() {
		return ssnapClientId;
	}

	public void setSsnapClientId(String ssnapClientId) {
		this.ssnapClientId = ssnapClientId;
	}

	public String getSsnapToken() {
		return ssnapToken;
	}

	public void setSsnapToken(String ssnapToken) {
		this.ssnapToken = ssnapToken;
	}

	public String getNsapiProfileUrl() {
		return nsapiProfileUrl;
	}

	public void setNsapiProfileUrl(String nsapiProfileUrl) {
		this.nsapiProfileUrl = nsapiProfileUrl;
	}

	public String getNsapiKey() {
		return nsapiKey;
	}

	public void setNsapiKey(String nsapiKey) {
		this.nsapiKey = nsapiKey;
	}

	public String getBulkUploadCerts() {
		return bulkUploadCerts;
	}

	public void setBulkUploadCerts(String bulkUploadCerts) {
		this.bulkUploadCerts = bulkUploadCerts;
	}

	public String getZohoClientSecret() {
		return zohoClientSecret;
	}

	public void setZohoClientSecret(String zohoClientSecret) {
		this.zohoClientSecret = zohoClientSecret;
	}

	public String getZohoClientId() {
		return zohoClientId;
	}

	public void setZohoClientId(String zohoClientId) {
		this.zohoClientId = zohoClientId;
	}

	public String getZohoRefreshToken() {
		return zohoRefreshToken;
	}

	public void setZohoRefreshToken(String zohoRefreshToken) {
		this.zohoRefreshToken = zohoRefreshToken;
	}

	public String getJobNotificationEmail() {
		return jobNotificationEmail;
	}

	public void setJobNotificationEmail(String jobNotificationEmail) {
		this.jobNotificationEmail = jobNotificationEmail;
	}

	public String getEdreadyRedirectUrl() {
		return edreadyRedirectUrl;
	}

	public void setEdreadyRedirectUrl(String edreadyRedirectUrl) {
		this.edreadyRedirectUrl = edreadyRedirectUrl;
	}

	public String getAdManagmentPath() {
		return adManagmentPath;
	}

	public void setAdManagmentPath(String adManagmentPath) {
		this.adManagmentPath = adManagmentPath;
	}
}
