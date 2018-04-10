package com.aplaline.baibulo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieVersionExtractor implements VersionExtractor {
	private static final String VERSION_COOKIE_NAME = "__version";
	private static final Logger log = LoggerFactory.getLogger(CookieVersionExtractor.class);

	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		final Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.info("No cookies found");
			return null;
		}
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals(VERSION_COOKIE_NAME)) {
				log.info("Found " + VERSION_COOKIE_NAME + " with value " + cookies[i].getValue());
				return cookies[i].getValue();
			}
		}
		return null;
	}

	public static void setVersionCookie(HttpServletResponse response, String version) {
		response.addCookie(new Cookie(VERSION_COOKIE_NAME, version));
	}
}