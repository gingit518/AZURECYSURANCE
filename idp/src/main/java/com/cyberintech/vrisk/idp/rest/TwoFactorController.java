package com.cyberintech.vrisk.idp.rest;

import com.cyberintech.vrisk.server.security.mfa.MultiFactorAuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Two Factor IdP controller
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2024-04-02
 */
@RestController
@RequestMapping(
	value = TwoFactorController.CONTROLLER_URI,
	produces = MediaType.APPLICATION_JSON_VALUE,
	name = "2Factor Authorization"
)
@Tag(name = "2Factor Authorization")
@Slf4j
public class TwoFactorController {

	static final String CONTROLLER_URI = "/2factor";

	@Autowired
	private MultiFactorAuthenticationService multiFactorAuthenticationService;

	@GetMapping(value = "/totp/qr-code/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] generateTOTPQRCode(@PathVariable("userId") Long userId) throws Exception {
		BufferedImage bufferedImage = multiFactorAuthenticationService.generateTOTPQRCode(userId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", baos);

		byte[] bytes = baos.toByteArray();

		return bytes;
	}

}
