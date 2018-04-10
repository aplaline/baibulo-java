package com.aplaline.baibulo;

import javax.servlet.http.HttpServletRequest;

public class QueryStringVersionExtractor implements VersionExtractor {
	public static final String VERSION_PARAM_NAME = "version";

	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		final QueryString qs = new QueryString(request.getQueryString());
		return qs.get(VERSION_PARAM_NAME);
	}
}