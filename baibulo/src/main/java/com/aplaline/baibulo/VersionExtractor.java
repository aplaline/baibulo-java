package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

public interface VersionExtractor {
	String extractVersionFromRequest(HttpServletRequest request);
}
