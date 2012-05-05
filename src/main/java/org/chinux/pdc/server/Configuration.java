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

	public Configuration() {
		this.blockAll = false;
		this.blockedIPs = new ArrayList<String>();
		this.blockedURLs = new ArrayList<String>();
		this.blockedMediaTypes = new ArrayList<String>();
		this.maxResSize = -1;
		this.l33t = false;
		this.rotateImages = false;
	}

	public Configuration(final boolean blockAll, final List<String> blockedIPs,
			final List<String> blockedURLs,
			final List<String> blockedMediaTypes, final int maxResSize,
			final boolean l33t, final boolean rotateImages) {
		this.blockAll = blockAll;
		this.blockedIPs = blockedIPs;
		this.blockedURLs = blockedURLs;
		this.blockedMediaTypes = blockedMediaTypes;
		this.maxResSize = maxResSize;
		this.l33t = l33t;
		this.rotateImages = rotateImages;

	}

	public boolean isBlockAll() {
		return this.blockAll;
	}

	public List<String> getBlockedIPs() {
		return this.blockedIPs;
	}

	public List<String> getBlockedURLs() {
		return this.blockedURLs;
	}

	public List<String> getBlockedMediaTypes() {
		return this.blockedMediaTypes;
	}

	public int getMaxResSize() {
		return this.maxResSize;
	}

	public boolean isL33t() {
		return this.l33t;
	}

	public boolean isRotateImages() {
		return this.rotateImages;
	}

	@Override
	public String toString() {
		return this.blockedIPs.toString() + this.blockedMediaTypes
				+ this.blockedURLs + this.maxResSize + this.l33t
				+ this.rotateImages + this.blockAll;
	}
}
