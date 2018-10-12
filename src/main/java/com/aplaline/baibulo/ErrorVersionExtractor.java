package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

/**
 * Version extractor that throws error.
 */
public class ErrorVersionExtractor implements VersionExtractor {
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		throw new RuntimeException("No version information found");
	}
}
