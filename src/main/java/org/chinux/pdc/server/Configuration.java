package org.chinux.pdc.server;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

	private boolean blockAll;
	private List<String> blockedIPs;
	private List<String> blockedURLs;
	private List<String> blockedMediaTypes;
	private int maxResSize;
	private boolean l33t;
	private boolean rotateImages;
	private boolean chainProxy;
	private boolean maxResEnabled;
	private Integer chainProxyPort;

	private String chainProxyHost;

	public Configuration() {
		this.blockAll = false;
		this.blockedIPs = new ArrayList<String>();
		this.blockedURLs = new ArrayList<String>();
		this.blockedMediaTypes = new ArrayList<String>();
		this.maxResSize = -1;
		this.l33t = false;
		this.rotateImages = false;
		this.chainProxy = false;
		this.maxResEnabled = false;
	}

	public Configuration(final boolean blockAll, final List<String> blockedIPs,
			final List<String> blockedURLs,
			final List<String> blockedMediaTypes, final int maxResSize,
			final boolean l33t, final boolean rotateImages,
			final boolean chainProxy, final boolean maxResFilter,
			final Integer chainProxyPort, final String chainProxyHost) {
		this.blockAll = blockAll;
		this.blockedIPs = blockedIPs;
		this.blockedURLs = blockedURLs;
		this.blockedMediaTypes = blockedMediaTypes;
		this.maxResSize = maxResSize;
		this.l33t = l33t;
		this.rotateImages = rotateImages;
		this.chainProxy = chainProxy;
		this.maxResEnabled = maxResFilter;
		this.chainProxyPort = chainProxyPort;
		this.chainProxyHost = chainProxyHost;

	}

	public List<String> getBlockedIPs() {
		return this.blockedIPs;
	}

	public List<String> getBlockedMediaTypes() {
		return this.blockedMediaTypes;
	}

	public List<String> getBlockedURLs() {
		return this.blockedURLs;
	}

	public String getChainProxyHost() {
		return this.chainProxyHost;
	}

	public Integer getChainProxyPort() {
		return this.chainProxyPort;
	}

	public int getMaxResSize() {
		return this.maxResSize;
	}

	public boolean isBlockAll() {
		return this.blockAll;
	}

	public boolean isChainProxy() {
		return this.chainProxy;
	}

	public boolean isL33t() {
		return this.l33t;
	}

	public boolean isMaxResEnabled() {
		return this.maxResEnabled;
	}

	public boolean isRotateImages() {
		return this.rotateImages;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		final StringBuilder builder = new StringBuilder();
		builder.append("blockAll=").append(this.blockAll).append(", ");
		if (this.blockedIPs != null) {
			builder.append("blockedIPs=")
					.append(this.blockedIPs.subList(0,
							Math.min(this.blockedIPs.size(), maxLen)))
					.append(", ");
		}
		if (this.blockedURLs != null) {
			builder.append("blockedURLs=")
					.append(this.blockedURLs.subList(0,
							Math.min(this.blockedURLs.size(), maxLen)))
					.append(", ");
		}
		if (this.blockedMediaTypes != null) {
			builder.append("blockedMediaTypes=")
					.append(this.blockedMediaTypes.subList(0,
							Math.min(this.blockedMediaTypes.size(), maxLen)))
					.append(", ");
		}
		builder.append("maxResSize=").append(this.maxResSize).append(", l33t=")
				.append(this.l33t).append(", rotateImages=")
				.append(this.rotateImages).append(", chainProxy=")
				.append(this.chainProxy).append(", maxResEnabled=")
				.append(this.maxResEnabled).append(", ");
		if (this.chainProxyPort != null) {
			builder.append("chainProxyPort=").append(this.chainProxyPort)
					.append(", ");
		}
		if (this.chainProxyHost != null) {
			builder.append("chainProxyHost=").append(this.chainProxyHost);
		}
		builder.append("\n");
		return builder.toString();
	}
}
