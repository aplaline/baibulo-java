package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

public class VersionHeaderVersionExtractor implements VersionExtractor {
	private static final String VERSION_HEADER_NAME = "Version";
	
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		return request.getHeader(VERSION_HEADER_NAME);
	}
}
