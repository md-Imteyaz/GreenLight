package com.gl.platform.web.rest;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.gl.platform.domain.JhiUser;
import com.gl.platform.repository.JhiUserRepository;
import com.gl.platform.security.SecurityUtils;
import com.gl.platform.service.GlUserQueryService;
import com.gl.platform.service.GlUserService;
import com.gl.platform.service.InstitutionUserQueryService;
import com.gl.platform.service.MailService;
import com.gl.platform.service.UserService;
import com.gl.platform.service.dto.ForgetPasswordRequestDTO;
import com.gl.platform.service.dto.GlUserCriteria;
import com.gl.platform.service.dto.GlUserDTO;
import com.gl.platform.service.dto.InstitutionUserCriteria;
import com.gl.platform.service.dto.InstitutionUserDTO;
import com.gl.platform.service.dto.JhiUserDTO;
import com.gl.platform.service.dto.PasswordChangeDTO;
import com.gl.platform.service.dto.ResetPasswordDTO;
import com.gl.platform.service.dto.ResetProfilePasswordDTO;
import com.gl.platform.web.rest.errors.EmailAlreadyUsedException;
import com.gl.platform.web.rest.errors.EmailNotFoundException;
import com.gl.platform.web.rest.errors.GlUserNotPresentException;
import com.gl.platform.web.rest.errors.InternalServerErrorException;
import com.gl.platform.web.rest.errors.InvalidPasswordException;
import com.gl.platform.web.rest.errors.JhiUserNotPresentException;
import com.gl.platform.web.rest.errors.LoginAlreadyUsedException;
import com.gl.platform.web.rest.errors.OTPInvalidException;
import com.gl.platform.web.rest.errors.OTPVerifiedException;
import com.gl.platform.web.rest.errors.OtpExpiredException;
import com.gl.platform.web.rest.errors.PasswordAlreadyUsedException;
import com.gl.platform.web.rest.errors.TryCatchException;
import com.gl.platform.web.rest.errors.UserAlreadyActivatedException;
import com.gl.platform.web.rest.errors.UserNotActivatedException;
import com.gl.platform.web.rest.vm.KeyAndPasswordVM;
import com.gl.platform.web.rest.vm.ManagedUserVM;
import com.itextpdf.text.pdf.qrcode.WriterException;

import io.github.jhipster.service.filter.LongFilter;
import javassist.NotFoundException;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

	private final Logger log = LoggerFactory.getLogger(AccountResource.class);

	private final JhiUserRepository userRepository;

	private final UserService userService;

	private final MailService mailService;

	private final InstitutionUserQueryService institutionUserQueryService;

	@Autowired
	private GlUserService glUserService;

	@Autowired
	private GlUserQueryService glUserQueryService;

	public AccountResource(JhiUserRepository userRepository, UserService userService, MailService mailService,
			InstitutionUserQueryService institutionUserQueryService) {

		this.userRepository = userRepository;
		this.userService = userService;
		this.mailService = mailService;
		this.institutionUserQueryService = institutionUserQueryService;
	}

	/**
	 * POST /register : register the user.
	 *
	 * @param managedUserVM the managed user View Model
	 * @throws InvalidPasswordException  400 (Bad Request) if the password is
	 *                                   incorrect
	 * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already
	 *                                   used
	 * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already
	 *                                   used
	 */
	@PostMapping("/register")
	@Timed
	@ResponseStatus(HttpStatus.CREATED)
	public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
		log.debug("Request to register the user");
		if (!checkPasswordLength(managedUserVM.getPassword())) {
			throw new InvalidPasswordException();
		}
		userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {
			throw new LoginAlreadyUsedException();
		});
		userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {
			throw new EmailAlreadyUsedException();
		});
		JhiUser user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
		mailService.sendActivationEmail(user);
	}

	/**
	 * GET /activate-admin : activate the registered admin for the particular
	 * University.
	 *
	 * @param key the activation key
	 * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be
	 *                          activated
	 */
	@GetMapping("/activate-admin")
	@Timed
	public void activateAdminAccount(@RequestParam(value = "key") String key) {

		log.debug("Request to activate the registered admin for university");
		Optional<JhiUser> user = userService.adminRegistration(key);
		if (!user.isPresent()) {
			throw new InternalServerErrorException("No user was found for this activation key");
		}
		JhiUser responseUser = userRepository.save(user.get());
		if (responseUser.getActivationKey().equalsIgnoreCase(key)) {
			log.debug("User Activation key present");
			/*
			 * Integer otp=userService.generateOTP(responseUser.getActivationKey());
			 * 
			 * if (otp==2) { //No user present with activation key throw new
			 * JhiUserNotPresentException(); }
			 * 
			 * if (otp==3) { //no jhi user present with activation key throw new
			 * GlUserNotPresentException(); }
			 */
		}

	}

	/**
	 * GET /activate : activate the registered user.
	 *
	 * @param key the activation key
	 * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be
	 *                          activated
	 */
	@GetMapping("/activate")
	@Timed
	public void activateAccount(@RequestParam(value = "key") String key) {
		log.debug("Request to activate the registered user");
		Optional<JhiUser> user = userService.activateRegistration(key);
		if (!user.isPresent()) {
			throw new InternalServerErrorException("No user was found for this activation key");
		}
		JhiUser responseUser = userRepository.save(user.get());
		if (responseUser.isActivated()) {
			log.debug("User Activated Successfully");
		}
	}

	/**
	 * GET /resetPassword : The user gets the mail and from the reset forgot
	 * passwords for all the registered user.
	 *
	 * @param key the username
	 * @throws NotFoundException
	 * @throws RuntimeException  500 (Internal Server Error) if the user couldn't be
	 *                           present
	 */
	@GetMapping("/account/forgot-password/send-otp")
	@Timed
	public Map<String, String> resetPassword(@RequestParam(value = "key") String key) throws NotFoundException {

		Map<String, String> response = new HashMap<>();
		Optional<JhiUser> existingUser = userService.getUserByActivationKey(key);
		if (!existingUser.isPresent()) {
			throw new NotFoundException("No active request found with this key");
		}

		if (existingUser.get().isActivated()) {

			Map<String, String> result = userService.generateOTPForgotPassword(key);

			if (result.containsKey("code")) {
				String otp = result.get("code");
				if (otp.equals("2")) {
					// No user present with activation key
					throw new JhiUserNotPresentException();
				}

				if (otp.equals("3")) {
					// no gl user present with activation key
					throw new GlUserNotPresentException();
				}
			}

			if (result.get("type").equalsIgnoreCase("google")) {
				response.put("type", "google");
				return response;
			} else if (result.get("type").equalsIgnoreCase("default")) {
				response.put("type", "default");
				if (result.containsKey("numberHint")) {
					response.put("numberHint", result.get("numberHint"));
				}
			}else if(result.get("type").equalsIgnoreCase("email")) {
				response.put("type", "email");
			}

		} else {
			throw new UserNotActivatedException();
		}

		return response;

	}

	/**
	 * POST /account/forgot-password-update Update the password with OTP for
	 * Updating password of users
	 * 
	 * @param forgotPasswordRequestDTO
	 * @return Response as OTP Verified and Password Updated successfully.
	 * 
	 */
	@PostMapping("/account/forgot-password-update")
	@Timed
	public ResponseEntity<?> resetUserPassword(@RequestBody ForgetPasswordRequestDTO forgotPasswordRequestDTO) {

		log.debug("REST request to update the password:");

		String result = userService.resetForgotPassword(forgotPasswordRequestDTO);

		if (result == "Invalid Verification Code") {
			throw new OTPInvalidException();
		}
		if (result == "Verification code Expired") {
			throw new OtpExpiredException();
		}
		if (result == "User not Present") {
			throw new JhiUserNotPresentException();
		}

		Map<String, String> response = new HashMap<String, String>();
		response.put("message", result);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * Based on the username, the user gets and OTP again to his mobile number
	 * 
	 * @param username
	 * @return resends and OTP to the registered mobile number
	 * @throws NotFoundException
	 */
	@PostMapping("/account/forgot-password/resend-otp")
	@Timed
	public ResponseEntity<?> resetPasswordWithOTP(@RequestParam("key") String key) throws NotFoundException {

		log.debug("User Requested for OTP again resetPasswordWithOTP(username = {})", key);
		Map<String, String> result = userService.generateOTPForgotPassword(key);
		if (result.containsKey("code") && (result.get("code").equals("2") || result.get("code").equals("3"))) {
			throw new NotFoundException("No record found with the key");
		}
		Map<String, String> response = new HashMap<String, String>();
		response.put("message", "Verification code sent successfully");
		if (result.containsKey("numberHint")) {
			response.put("numberHint", result.get("numberHint"));
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * POST /account/reset-password/{username} : Send an email to reset the password
	 * of the user
	 * 
	 * @pathVariable username the mail of the user
	 * @throws JhiUserNotFoundException 400 (Bad Request) if the email address is
	 *                                  not registered
	 */
	@PostMapping(path = "/account/reset-password/{username}")
	@Timed
	public void passwordReset(@PathVariable String username, @RequestParam("email") String email) {

		log.info("Entering passwordReset(username={}, email={}", username, email);

		Optional<JhiUser> existingUser = userRepository.findOneByLogin(username);
		if (existingUser.isPresent()) {
			if(existingUser.get().isActivated()) {
				log.debug("Exiting user is present:");
				userService.forgotPasswordReset(existingUser.get(), email);
			} else {
				throw new UserNotActivatedException();
			}
			
		} else {
			throw new JhiUserNotPresentException();
		}
	}

	/**
	 * GET /authenticate : check if the user is authenticated, and return its login.
	 *
	 * @param request the HTTP request
	 * @return the login if the user is authenticated
	 */
	@GetMapping("/authenticate")
	@Timed
	public String isAuthenticated(HttpServletRequest request) {
		log.debug("REST request to check if the current user is authenticated");
		return request.getRemoteUser();
	}

	/**
	 * GET /account : get the current user.
	 *
	 * @return the current user
	 * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be
	 *                          returned
	 */
	@GetMapping("/account")
	@Timed
	public ResponseEntity<?> getAccount() {

		log.debug("REST Request to get the current user:");
		JhiUserDTO jhiUserDTO = userService.getUserWithAuthorities().map(JhiUserDTO::new)
				.orElseThrow(() -> new InternalServerErrorException("User could not be found"));

		Map<String, Object> responseData = new HashMap<String, Object>();
		GlUserCriteria criteria = new GlUserCriteria();

		LongFilter filterByUserId = new LongFilter();
		filterByUserId.setEquals(jhiUserDTO.getId());
		criteria.setUserId(filterByUserId);

		List<GlUserDTO> glUsers = glUserQueryService.findByCriteria(criteria);

		List<InstitutionUserDTO> institutionUsers = new ArrayList<>();

		for (GlUserDTO glUser : glUsers) {
			InstitutionUserCriteria institutionUserCriteria = new InstitutionUserCriteria();
			LongFilter filterByGlUserId = new LongFilter();
			filterByGlUserId.setEquals(glUser.getId());
			institutionUserCriteria.setUserId(filterByGlUserId);
			institutionUsers.addAll(institutionUserQueryService.findByCriteria(institutionUserCriteria));
		}

		jhiUserDTO.setPasswordHash(null);

		responseData.put("jhiUser", jhiUserDTO);
		Object glUserObject = glUserService.getListOfMapObject(glUsers);
		responseData.put("glUsers", glUserObject);
		responseData.put("institutionUsers", institutionUsers);

		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	/**
	 * POST /account : update the current user information.
	 *
	 * @param userDTO the current user information
	 * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already
	 *                                   used
	 * @throws RuntimeException          500 (Internal Server Error) if the user
	 *                                   login wasn't found
	 */
	@PostMapping("/account")
	@Timed
	public void saveAccount(@Valid @RequestBody JhiUserDTO userDTO) {
		log.debug("REST Request to update user saveAccount(userDTO = {})", userDTO);
		final String userLogin = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new InternalServerErrorException("Current user login not found"));
		Optional<JhiUser> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
		log.debug("Existing user Object : {}", existingUser);
		if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
			throw new EmailAlreadyUsedException();
		}
		Optional<JhiUser> user = userRepository.findOneByLogin(userLogin);
		if (!user.isPresent()) {
			throw new InternalServerErrorException("User could not be found");
		}
		userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.getLangKey(),
				userDTO.getImageUrl());
	}

	/**
	 * POST /account/change-password : changes the current user's password
	 *
	 * @param passwordChangeDto current and new password
	 * @throws PasswordAlreadyUsedException
	 * @throws InvalidPasswordException     400 (Bad Request) if the new password is
	 *                                      incorrect
	 */
	@PostMapping(path = "/account/change-password")
	@Timed
	public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDTO passwordChangeDto)
			throws PasswordAlreadyUsedException {
		log.debug("REST request to change the current user's password:");
		if (!checkPasswordLength(passwordChangeDto.getNewPassword())) {
			throw new InvalidPasswordException();
		}
		Map<String, String> result = userService.changePassword(passwordChangeDto.getCurrentPassword(),
				passwordChangeDto.getNewPassword());

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * POST / account/change-reset-password : reset the password for the admin on
	 * typing the OTP and then login to the dashboard
	 * 
	 * @param passwordChangeDto
	 * @return
	 */
	@PostMapping("/account/change-reset-password")
	@Timed
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO passwordChangeDto) {

		log.debug("Request to reset the password:");
		String result = userService.resetPassword(passwordChangeDto);

		if (result == "Invalid Verification Code") {
			throw new OTPInvalidException();
		}
		if (result == "user actived") {
			throw new UserAlreadyActivatedException();
		}

		if (result == "Verification code Expired") {
			throw new OtpExpiredException();
		}

		if (result == "Already verified") {
			throw new OTPVerifiedException();
		}

		if (result == "Failed") {
			throw new TryCatchException();
		}

		if (result == "User not Present") {
			throw new JhiUserNotPresentException();
		}

		Map<String, String> response = new HashMap<String, String>();
		response.put("message", result);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * POST / change-profile-password : profile password update on OTP validation.
	 * change the password in the profile of the user based on the OTP received to
	 * the mobile
	 * 
	 * @param profilePasswordChangeDto
	 * @return
	 */
	@PostMapping("/change-profile-password")
	@Timed
	public ResponseEntity<?> resetProfilePassword(@RequestBody ResetProfilePasswordDTO profilePasswordChangeDto) {

		log.debug("REST Request for profile password update on OTP validation:");
		String result = userService.resetProfilePassword(profilePasswordChangeDto);
		log.debug("Result : {}", result);
		if (result == "Invalid Verification Code") {
			throw new OTPInvalidException();
		}

		if (result == "Verification code Expired") {
			throw new OtpExpiredException();
		}

		if (result == "Failed") {
			throw new TryCatchException();
		}

		Map<String, String> response = new HashMap<String, String>();
		response.put("message", result);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * POST /change-password/resend-otp : to update the password,OTP validation is
	 * mandatory
	 * 
	 * @return Sends OTP again to the user registered Mobile
	 * @param userID
	 */
	@PostMapping("/profile-change-password/resend-otp/{userId}")
	@Timed
	public ResponseEntity<?> resendChangePasswordOTP(@PathVariable Long userId) {

		int otpResent = userService.resendChangePasswordOTP(userId);

		if (otpResent == 1) {
			throw new OTPVerifiedException();
		}

		if (otpResent == 0) {
			throw new GlUserNotPresentException();
		}

		Map<String, String> response = new HashMap<String, String>();
		response.put("message", "Resent verification code to your mobile");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Based on the activation key, the user gets and OTP again to his mobile number
	 * 
	 * @param key
	 * @return resends and OTP to the registered mobile number
	 */
	@PostMapping("/account/resend-otp")
	@Timed
	public ResponseEntity<?> resetActivationOTP(@RequestParam("key") String key) {
		log.info("REST request to get the OTP again:");
		int randomPin = userService.generateOTP(key);

		if (randomPin == 1) {
			log.debug("OTP Verified:");
			throw new OTPVerifiedException();
		}

		Map<String, String> response = new HashMap<String, String>();
		response.put("message", "verification code sent successfully");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * POST /account/reset-password/init : Send an email to reset the password of
	 * the user
	 *
	 * @param mail the mail of the user
	 * @throws EmailNotFoundException 400 (Bad Request) if the email address is not
	 *                                registered
	 */
	@PostMapping(path = "/account/reset-password/init")
	@Timed
	public void requestPasswordReset(@RequestBody String mail) {
		log.debug("REST Request to send an email requestPasswordReset(mail = {})", mail);
		mailService
				.sendPasswordResetMail(userService.requestPasswordReset(mail).orElseThrow(EmailNotFoundException::new));
	}

	/**
	 * POST /account/reset-password/finish : Finish to reset the password of the
	 * user
	 *
	 * @param keyAndPassword the generated key and the new password
	 * @throws InvalidPasswordException 400 (Bad Request) if the password is
	 *                                  incorrect
	 * @throws RuntimeException         500 (Internal Server Error) if the password
	 *                                  could not be reset
	 */
	@PostMapping(path = "/account/reset-password/finish")
	@Timed
	public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
		if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
			throw new InvalidPasswordException();
		}
		Optional<JhiUser> user = userService.completePasswordReset(keyAndPassword.getNewPassword(),
				keyAndPassword.getKey());

		if (!user.isPresent()) {
			log.debug("no user was found for the reset key:");
			throw new InternalServerErrorException("No user was found for this reset key");
		}
	}

	private static boolean checkPasswordLength(String password) {
		return !StringUtils.isEmpty(password) && password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH
				&& password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
	}

	/**
	 * GET /account/info : get the current user.
	 *
	 * @return the current user
	 * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be
	 *                          returned
	 */
	@GetMapping("/account/info")
	@Timed
	public ResponseEntity<?> getAccountInfo() {

		log.debug("Rest request to get the account info");
		Map<String, Object> responseData = userService.getAccountInfo();
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
	
	
	
	
	@RequestMapping(value = "/generate/temp.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImageWithMediaType()
            throws IOException, WriterException {
		BufferedImage singlePixelImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	    Color transparent = new Color(0, 0, 0, 0);
	    singlePixelImage.setRGB(0, 0, transparent.getRGB());

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(singlePixelImage, "png", baos);
	    byte[] imageInBytes = baos.toByteArray();
	    baos.close();

	    return imageInBytes;
    }
}
