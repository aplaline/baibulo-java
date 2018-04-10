package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

public class ReleaseVersionExtractor implements VersionExtractor {
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		return "release";
	}
}
