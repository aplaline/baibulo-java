package com.aplaline.baibulo;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefererHeaderVersionExtractor implements VersionExtractor {
	private static final String REFERER_HEADER_NAME = "Referer";
	private static final Logger log = LoggerFactory.getLogger(RefererHeaderVersionExtractor.class);
	
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		URI referer = parseUrl(request.getHeader(REFERER_HEADER_NAME));
		if (referer != null) {
			log.info(referer.toString());
			final QueryString qs = new QueryString(referer.getRawQuery());
			return qs.get(QueryStringVersionExtractor.VERSION_PARAM_NAME);
		}
		return null;
	}

	private URI parseUrl(String uri) {
		if (uri == null) {
			return null;
		}
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			log.warn("Unable to extract version from existing referrer header: " + uri, e);
			return null;
		}
	}
}
