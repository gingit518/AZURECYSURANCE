package com.cyberintech.vrisk.server.service.integrations.marketing.zoominfo;

/**
 * Zoom Info configuration
 *
 * @author   Eugene A. Kalosha <ekalosha@dfusiontech.com>
 * @version  0.1.1
 * @since    2022-02-23
 */
public interface ZoomInfoConfig {

	/**
     * ZoomInfo Username
	 *
	 * @return Returns username for ZoomInfo API account
     */
	public String getUsername();

	/**
     * ZoomInfo Client ID for
	 *
	 * @return Returns Client ID for ZoomInfo API account
     */
	public String getClientId();

	/**
	 * ZoomInfo Private key
	 *
	 * @return Returns Private Key for ZoomInfo API account
	 */
	public String getPrivateKey();

}
