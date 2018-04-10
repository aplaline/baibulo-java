package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

/**
 * Version extractor interface 
 */
public interface VersionExtractor {
	String extractVersionFromRequest(HttpServletRequest request);
}
