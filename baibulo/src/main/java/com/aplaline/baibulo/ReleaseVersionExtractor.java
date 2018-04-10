package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

/**
 * Static version extractor always returning "release" version 
 */
public class ReleaseVersionExtractor implements VersionExtractor {
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		return "release";
	}
}
