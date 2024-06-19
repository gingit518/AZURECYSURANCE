package com.cyberintech.vrisk.server.security.mfa;

import com.cyberintech.vrisk.server.model.jpa.domains.TwoFactorType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

/**
 * Result of file import
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2019-04-12
 */
@Setter
@Getter
public class MFACodeTokenDTO implements Serializable {

	private static final long serialVersionUID = 64274038991256349L;

	private String token;

	private String code;

	private Long userId;

	private TwoFactorType twoFactorType;

	private int attempt;

	private Date created;

	public MFACodeTokenDTO() {
		attempt = 0;
		created = new Date();
	}

	/**
	 * Create Code Token object
	 *
	 * @param token
	 * @param code
	 * @return
	 */
	public static MFACodeTokenDTO of(String token, String code) {
		MFACodeTokenDTO result = new MFACodeTokenDTO();
		result.setToken(token);
		result.setCode(code);

		return result;
	}

	/**
	 * Create Code Token object
	 *
	 * @param token
	 * @return
	 */
	public static MFACodeTokenDTO of(String token, Long userId) {
		MFACodeTokenDTO codeToken = of(token);
		codeToken.setUserId(userId);

		return codeToken;
	}

	/**
	 * Create Code Token object
	 *
	 * @param token
	 * @return
	 */
	public static MFACodeTokenDTO of(String token) {

		String code = "000000";
		Random random = new Random(System.currentTimeMillis());
		int codeInt = random.nextInt(999999);
		String codeIntString = Integer.toString(codeInt);
		code = code.substring(codeIntString.length()) + codeIntString;

		MFACodeTokenDTO result = new MFACodeTokenDTO();
		result.setToken(token);
		result.setCode(code);

		return result;
	}

}
