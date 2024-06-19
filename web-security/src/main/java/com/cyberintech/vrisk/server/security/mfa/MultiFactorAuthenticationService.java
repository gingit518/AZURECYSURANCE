package com.cyberintech.vrisk.server.security.mfa;

import com.cyberintech.vrisk.server.model.auth.UserDetailsImpl;
import com.cyberintech.vrisk.server.model.data.queue.MessageType;
import com.cyberintech.vrisk.server.model.data.queue.RiskQQueueMessage;
import com.cyberintech.vrisk.server.model.dto.user.UserRefDTO;
import com.cyberintech.vrisk.server.model.jpa.domains.AuditOperationType;
import com.cyberintech.vrisk.server.model.jpa.domains.IdpType;
import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import com.cyberintech.vrisk.server.model.jpa.domains.VItemType;
import com.cyberintech.vrisk.server.model.jpa.entity.AuditLogItemId;
import com.cyberintech.vrisk.server.model.jpa.entity.IdpUsers;
import com.cyberintech.vrisk.server.model.jpa.entity.Organizations;
import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import com.cyberintech.vrisk.server.repository.jpa.IdpUserRepository;
import com.cyberintech.vrisk.server.repository.jpa.UserRepository;
import com.cyberintech.vrisk.server.security.cache.IdPCacheService;
import com.cyberintech.vrisk.server.service.AuditLogService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.microsoft.graph.models.extensions.User;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * oAuth2 Multi Factor Authentication Service
 *
 * @author Eugene A. Kalosha <ekalosha@dfusiontech.com>
 */
@Service
@Slf4j
public class MultiFactorAuthenticationService {

	// private GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

	@Value("${application.queue.common:riskq-development}")
	private String QUEUE_NAME;

	@Value("${vrisk.idp.url}")
	private String idpUrl;

	@Value("${application.oauth.totp.application-name:ValuRisQ}")
	private String totpApplicationName;

	@Autowired
	private IdPCacheService idPCacheService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private AuditLogService auditLogService;

	@Autowired
	private IdpUserRepository idpUserRepository;

	@Autowired
	private RestTemplate restTemplate;

	public boolean isEnabled(String username) {
		return isEnabled(username, null);
	}

	/**
	 * Check is MFA Enabled for User by its Username
	 *
	 * @param username
	 * @param client
	 * @return
	 */
	@Transactional
	public boolean isEnabled(String username, ClientDetails client) {
		Users userDetails = userRepository.findFirstByEmailIgnoreCase(username).get();

		// Verify that Authorization Client Allowed to use MFA
		if (client != null && !client.getAuthorities().contains(new SimpleGrantedAuthority("MFA"))) {
			return false;
		}

		// If MFA set for user - use it
		if (userDetails.getUseMultiFactorAuth() != null && Boolean.TRUE.equals(userDetails.getUseMultiFactorAuth())) {
			return true;
		}

		// If MFA set for organization - it is mandatory for all the users
		Organizations organization = userDetails.getOrganization();
		if (organization != null && Boolean.TRUE.equals(organization.getUseMultiFactorAuth())) {
			return true;
		}

		return false;
	}

	/**
	 * Check is MFA Enabled for User by its Username
	 *
	 * @param username
	 * @param client
	 * @return
	 */
	@Transactional
	public TwoFactorType checkTwoFactor(String username, ClientDetails client) {
		TwoFactorType result = TwoFactorType.NONE;

		Users userDetails = userRepository.findFirstByEmailIgnoreCase(username).get();

		// Verify that Authorization Client Allowed to use MFA
		if (client != null && !client.getAuthorities().contains(new SimpleGrantedAuthority("MFA"))) {
			return result;
		}

		// If MFA set for user - use it
		if (Boolean.TRUE.equals(userDetails.getUseMultiFactorAuth())) {
			result = userDetails.getTwoFactorType() != null ? userDetails.getTwoFactorType() : TwoFactorType.TOTP;
		}

		// If MFA set for organization - it is mandatory for all the users
		Organizations organization = userDetails.getOrganization();
		if (organization != null && Boolean.TRUE.equals(organization.getUseMultiFactorAuth())) {
			result = userDetails.getTwoFactorType() != null ? userDetails.getTwoFactorType() : TwoFactorType.TOTP;
		}

		// Save TOTP Secret
		if (TwoFactorType.TOTP.equals(result) && StringUtils.isEmpty(userDetails.getTotpSecret())) {
			userDetails.setTotpSecret(Base32.random());
			userRepository.save(userDetails);
		}

		return result;
	}

	/**
	 * Get auth User by its Username
	 *
	 * @param username
	 * @param client
	 * @return
	 */
	@Transactional
	public Users getAuthUser(String username, ClientDetails client) {
		return userRepository.findFirstByEmailIgnoreCase(username).get();
	}

	/**
	 * Get auth User by its Username
	 *
	 * @return
	 */
	@Transactional
	public String getTOTPQRCodeUrl(Users user) {
		String result = idpUrl + String.format("/2factor/totp/qr-code/%s", user.getId());

		return result;
	}


	/**
	 * Audit user password login event
	 *
	 * @param authentication
	 * @return
	 */
	@Transactional
	public boolean auditUserAuthEvent(Authentication authentication, VItemType event) {
		Object principal = authentication.getPrincipal();

		UserDetailsImpl user = (UserDetailsImpl) principal;

		return auditUserAuthEvent(user.getUserId(), event);
	}


	/**
	 * Audit user password login event
	 *
	 * @param email
	 * @return
	 */
	@Transactional
	public UserDetails getIDPUserByIdentityAndType(String email, IdpType idpType) {
		// Check the email in common users DB table
		Optional<Users> userOptional = userRepository.findFirstByEmailIgnoreCase(email);
		Users user = null;
		// If there is no any user with given email..
		if (!userOptional.isPresent()) {
			//  ..then check is given email is allied to any user through idp_users table
			Optional<IdpUsers> idpUserOptional = this.idpUserRepository.findFirstByUserIdentityIgnoreCaseAndIdpId(email, idpType);

			// in case that there is no users allied to given email - throw exception
			if (!idpUserOptional.isPresent() || idpUserOptional.get().getUserId() == null) {
				throw new InvalidGrantException("Could not authenticate user: " + email + " . Make sure you are registered in the system.");
			} else {
				user = idpUserOptional.get().getUser();
			}
		} else {
			user = userOptional.get();
		}
		// Other way authorize
		UserDetails userDetails = UserDetailsImpl.of(user);

		return userDetails;
	}

	/**
	 * Generate QR code for TOTP
	 *
	 * @param userId
	 * @return
	 */
	@Transactional
	public BufferedImage generateTOTPQRCode(Long userId) throws Exception {

		Users user = userRepository.findById(userId).get();
		String userEmailEncoded = URLEncoder.encode(user.getEmail(), "UTF-8");
		String totpSecretEncoded = URLEncoder.encode(user.getTotpSecret(), "UTF-8");
		String applicationNameEncoded = URLEncoder.encode(totpApplicationName, "UTF-8");
		String barcodeText = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", applicationNameEncoded, userEmailEncoded, totpSecretEncoded, applicationNameEncoded);

		QRCodeWriter barcodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);

		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	/**
	 * Audit user password login event
	 *
	 * @param userId
	 * @param event
	 * @return
	 */
	@Transactional
	public boolean auditUserAuthEvent(Long userId, VItemType event) {

		Users userDetails = userRepository.findById(userId).get();
		UserRefDTO userRef = new UserRefDTO(userDetails);

		// Send JMS Message to the Queue
		if (VItemType.USER_PASSWORD_LOGIN.equals(event) || VItemType.USER_SMS_CODE_LOGIN.equals(event) || VItemType.USER_OTP_CODE_LOGIN.equals(event)
			|| VItemType.USER_GOOGLE_LOGIN.equals(event) || VItemType.USER_MICROSOFT_LOGIN.equals(event) || VItemType.USER_OKTA_LOGIN.equals(event)
		) {
			try {
				jmsTemplate.convertAndSend(QUEUE_NAME, RiskQQueueMessage.builder().type(MessageType.SIGNIN).userId(userId).createDate(new Date()).build());
			} catch (Exception exception) {
				log.error("## Failed to SEND JMS message: ", exception);
			}
		} else if (VItemType.USER_LOGOUT.equals(event)) {
			try {
				jmsTemplate.convertAndSend(QUEUE_NAME, RiskQQueueMessage.builder().type(MessageType.SIGNOUT).userId(userId).createDate(new Date()).build());
			} catch (Exception exception) {
				log.error("## Failed to SEND JMS message: ", exception);
			}
		}

		// Save Audit Log CREATE event
		auditLogService.audit(
			AuditOperationType.EVENT,
			event,
			userDetails.getId(),
			null,
			userRef,
			userDetails,
			collectAuditLogItems(userDetails)
		);

		return true;
	}

	/**
	 * Audit user password login event
	 *
	 * @param authentication
	 * @return
	 */
	public void auditUserPasswordLogin(Authentication authentication) {
		boolean result = auditUserAuthEvent(authentication, VItemType.USER_PASSWORD_LOGIN);
	}

	/**
	 * Audit user Google login event
	 *
	 * @param authentication
	 * @return
	 */
	public void auditUserGoogleLogin(Authentication authentication) {
		boolean result = auditUserAuthEvent(authentication, VItemType.USER_GOOGLE_LOGIN);
	}

	/**
	 * Audit user Microsoft login event
	 *
	 * @param authentication
	 * @return
	 */
	public void auditUserMicrosoftLogin(Authentication authentication) {
		boolean result = auditUserAuthEvent(authentication, VItemType.USER_MICROSOFT_LOGIN);
	}

	/**
	 * Audit user SMS code login event
	 *
	 * @param authentication
	 * @return
	 */
	public void auditSMSCodeLogin(Authentication authentication) {
		boolean result = auditUserAuthEvent(authentication, VItemType.USER_SMS_CODE_LOGIN);
	}

	/**
	 * Generate Code and save it to cache Wrapper
	 *
	 * @param token
	 * @return
	 */
	public String generateCode(String token, String username, TwoFactorType twoFactorType) {

		Users userDetails = userRepository.findFirstByEmailIgnoreCase(username).get();

		MFACodeTokenDTO codeToken = MFACodeTokenDTO.of(token, userDetails.getId());
		if (twoFactorType != null) codeToken.setTwoFactorType(twoFactorType);

		log.info(MessageFormat.format("Registered Code {0}, for user {1}", codeToken.getCode(), userDetails.getEmail()));

		// Save Code token
		idPCacheService.putCodeToken(codeToken);

		if (TwoFactorType.PHONE.equals(twoFactorType)) {
			sendCodeBySMS(codeToken.getCode(), userDetails);
		}

		return codeToken.getCode();
	}

	/**
	 * Send code by SMS
	 *
	 * @param code
	 * @param userDetails
	 * @return
	 */
	private boolean sendCodeBySMS(String code, Users userDetails) {

		String accountSid = environment.getProperty("twilio.sid");
		String accountToken = environment.getProperty("twilio.token");
		String fromNumber = environment.getProperty("twilio.from");
		String phoneNumber = userDetails.getMobilePhone();

		String fromNumberE164Format = verifyPhone(fromNumber);
		String phoneNumberE164Format = verifyPhone(phoneNumber);

		if (StringUtils.isEmpty(phoneNumberE164Format)) {
			throw new MFACodeException("Your phone number is empty. Please contact to administrator to verify your phone number.");
		}

		String smsMessage = MessageFormat.format("{0} is your verification code from Risk-Q", code);

		boolean result = true;
		try {
			Twilio.init(accountSid, accountToken);
			Message message = Message.creator(new PhoneNumber(phoneNumberE164Format), new PhoneNumber(fromNumberE164Format), smsMessage).create();
			String messageId = message.getSid();
		} catch (ApiException exception) {
			throw new MFACodeException("Failed to send SMS with verification code. Please contact support.");
		} catch (Throwable exception) {
			throw new MFACodeException("Something went wrong. Failed to send SMS with verification code.");
		}

		return result;
	}

	/**
	 * Verify Code by its token
	 *
	 * @param code
	 * @param token
	 * @return
	 */
	@Transactional
	public boolean verifyCode(String code, String token) {

		MFACodeTokenDTO codeToken = idPCacheService.getCodeToken(token);
		if (codeToken == null) {
			return false;
		}

		boolean result = false;

		if (TwoFactorType.TOTP.equals(codeToken.getTwoFactorType())) {
			Users user = userRepository.findById(codeToken.getUserId()).get();
			Totp totp = new Totp(user.getTotpSecret());
			if (totp.verify(code)) {
				result = true;

				if (!Boolean.TRUE.equals(user.getIsTotpVerified())) {
					user.setIsTotpVerified(true);
					userRepository.save(user);
				}
			}

			if (!result) {
				// Proceed with attempts count
				codeToken.setAttempt(codeToken.getAttempt() + 1);
				if (codeToken.getAttempt() <= 3) {
					idPCacheService.putCodeToken(codeToken);
					auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_OTP_CODE_FAILED);
					throw new MFACodeException(MessageFormat.format("Invalid Authentication OTP code. You have {0} attempts left.", 3 - codeToken.getAttempt()));
				} else {
					auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_OTP_CODE_FAILED);
					idPCacheService.removeCodeToken(codeToken.getToken());
					throw new MFACodeException("Too many Authentication attempts with the OTP code. Please try again later.");
				}
			} else {
				auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_OTP_CODE_LOGIN);
				idPCacheService.removeCodeToken(codeToken.getToken());
			}
		} else {
			result = code.equalsIgnoreCase(codeToken.getCode());

			if (!result) {
				// Proceed with attempts count
				codeToken.setAttempt(codeToken.getAttempt() + 1);
				if (codeToken.getAttempt() <= 3) {
					idPCacheService.putCodeToken(codeToken);
					auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_SMS_CODE_WRONG);
					throw new MFACodeException(MessageFormat.format("Invalid Authentication code. You have {0} attempts left.", 3 - codeToken.getAttempt()));
				} else {
					auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_SMS_CODE_FAILED);
					idPCacheService.removeCodeToken(codeToken.getToken());
					throw new MFACodeException("Too many Authentication attempts with the code. Please try again later.");
				}
			} else {
				auditUserAuthEvent(codeToken.getUserId(), VItemType.USER_SMS_CODE_LOGIN);
				idPCacheService.removeCodeToken(codeToken.getToken());
			}
		}

		return result;
	}

	/**
	 * Check is code expired
	 *
	 * @param token
	 * @param code
	 * @return
	 */
	public boolean checkExpiration(String token, String code) {
		return true;
	}

	/**
	 * Verify phone number
	 *
	 * @param phone
	 * @return
	 */
	public String verifyPhone(String phone) {
		String result = phone != null ? phone : "";

		result = result.replaceAll("[^\\d]+", "");

		// We must add "+" for all international numbers (except USA and Canada)
		if (StringUtils.isNotEmpty(result)) result = "+" + result;

		return result;
	}

	/**
	 * Collect items for Audit Log record
	 *
	 * @param userDetails
	 * @return
	 */
	private AuditLogItemId[] collectAuditLogItems(Users userDetails) {
		List<AuditLogItemId> logItems = new ArrayList<>(Arrays.asList(AuditLogItemId.of(VItemType.ORGANIZATION, userDetails.getId())));
		logItems.add(AuditLogItemId.of(VItemType.USER, userDetails.getId()));

		return logItems.stream().toArray(AuditLogItemId[]::new);
	}

	public String getMicrosoftUserIdentity(String accessToken) {
		String microsoftUserIdentity = null;

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + accessToken);
		final HttpEntity request = new HttpEntity(headers);

		// Build request url
		String apiUrl = "https://graph.microsoft.com/v1.0/me";

		// Performing request
		ResponseEntity<User> responce = restTemplate.exchange(apiUrl, HttpMethod.GET, request, new ParameterizedTypeReference<User>() {});

		User microsoftUser = responce.getBody();
		log.info("Microsoft user: {}", microsoftUser);
		if (microsoftUser != null && StringUtils.isNotEmpty(microsoftUser.userPrincipalName)) {
			microsoftUserIdentity = microsoftUser.userPrincipalName;
		}
//		restTemplate

		return microsoftUserIdentity;
	}

}
