package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Zoom Info configuration
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
@Getter
@Component
public class ZoomInfoConfigImpl implements ZoomInfoConfig {

	@Autowired
	private Environment environment;

	/**
	 * ZoomInfo Username
	 *
	 * @return Returns username for ZoomInfo API account
	 */
	public String getUsername() {
		// By default, we are using username as eugene@risk-q.com
		String result = "eugene@risk-q.com";

		if (environment.containsProperty("zoominfo.client.username")) {
			result = environment.getProperty("zoominfo.client.username");
		}

		return result;
	}

	/**
	 * ZoomInfo Private key
	 *
	 * @return Returns Private Key for ZoomInfo API account
	 */
	@Override
	public String getPrivateKey() {
		// By default, we are using Private key registered to eugene@risk-q.com
		String result = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKRpqOAv3Q29lAhEPV2agsjcspaY2bGQyVA3+XA64CQUwkFRTu8rYkvSw6S1joWSqQMuqjKGq+N1jxNv1Ncl1BgJlXOE5RTvtLhQQC9fwQb+vj8QQdosh1WAIEiYHW9sFlXS1N+F56GR1Bik/orxyjy0cJeVygUIygex6b3C9BGafBb6hMp8D4YTuS71n2PZy8iOjCDyaIm2f16y+35fx28O3lIFZUeSXvPGE/szSwK/8Yb6WQxEbriE0/1a37GVNzvUuUAZAnfFROnWf2nNDpfKlzpYAgj54RhjdhCwIMXQfGk9mI/lYiCHfbj5Br7l6RmnzGRxmgR8+a/cluv7DHAgMBAAECggEBAJwpTgD9Rj8Mu/kXI9fhpfhfr81U9EIdYJ1aihJogNq01re0nuiq8PwEIDURHp6i2D1jUcqM1Xvs9vmi3oQAGwcK5Vd03OHg/BkdpC81AVkfTxo3ZFoM5n3RaB1gm6D99+jtuqBLc4UIBpIm2fJl3tKFiV5DF8Oc7DSpWMQzhTfX2jU/CMjyF0PLI8AdR1exhMek1gLIoDq24GFyIQKB65PEeqT1DrtqDeiudM83t4Apj9fXgEchFHs9sn/wYrLaM4LLPmQ6IIZYB9c7aec/yJdTEjGF2ehwrUnL5sqIGkH3LZpriX6wQFwwn15aIhTW6d+2q3NtYuQU7TBTBWL9iaECgYEA9mcY8b7fRDRfLkEXUuNBhk5AURPnYI86Oicqy4Qi8ruNAfQZAJ7s4jvenGAE/L2Oe2OjjreKAp/yYwLaQIvD/JVRMxb/tzaYmaqNlTQrzS4QSkDTneyqD5Ik8YWc9rR/+noZRoOA0WBtAGN0y/cJuRX64TPnm5x3oFUdSE+o6aUCgYEA0ieDTx0ZdiiFkNW8D3fkCqVdgtDphh/MGtefpyYV/HJji46reQDP1b/5IminXTU3NSHd//xVwMMQbn19GqRatfxm8vK2k2DEPyr/UqzbwSvfHjBKL4X+BDvwPIj2aLbgjjD39C0rb1yXOAFv6N8PhMOHbTbjvaNbMr9o3Ux9bPsCgYA0xZfeDpcasGzCJ0arheh+rzuvIagoUgSSd9j+hWpGukxU4saID4GEYnYm35mx15cwbgKVilGQhlkPQ4ki0cxDyGb/nileP98m6hQwpF/NLdRnUsL/y7QQaahn4cngPIA8UPIg3AE21oQB1Usqc9otPbvhh5BPD4+/UvtWhVkT0QKBgCTwzSQa+V7LwK6041nUuUEl0Eoubt4kuLv/KxE2oKa/dMATVBDxE5d/J0vlGYZI1GuCiIN215wKrdi+Nj94pHlY5L/P6kf9uoFgDYF64SYTkCS+WEwCQaR85L2/VZESlGZqNSu2xOvOWsZ9ei51B0VPGX8ZJu8ux9kjzEsaJvIBAoGBALkECYaVjSC8sf9CzIqHSDOYACrrtUR/oJPW9HMxFq1EIT/PtJJQiQ63xfg+oYEgnzPkD2CbNftAz17g8iyElCL+DRx9pYUml+vFQo/AXL+RvC8xHyf7xSzYiDqSy3/WLN/TwmqzR6rBqk0IXR4+g/JBr0M6KUEOQKMIw49d0sWH";

		if (environment.containsProperty("zoominfo.key.private")) {
			result = environment.getProperty("zoominfo.key.private");
		}

		return result;
	}

	/**
     * ZoomInfo Client ID for
	 *
	 * @return Returns Client ID for ZoomInfo API account
     */
	@Override
	public String getClientId() {
		// By default, we are using Private key registered to eugene@risk-q.com
		String result = "e14ece76-d583-48bc-a3ab-39ab47aeaf93";

		if (environment.containsProperty("zoominfo.client.id")) {
			result = environment.getProperty("zoominfo.client.id");
		}

		return result;
	}

}
