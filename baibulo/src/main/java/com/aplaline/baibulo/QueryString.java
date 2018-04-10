package com.aplaline.baibulo;

import java.util.HashMap;
import java.util.Map;

public final class QueryString {
	private final Map<String,String[]> params;

	@SuppressWarnings("deprecation")
	public QueryString(final String input) {
		if (input == null) {
			this.params = new HashMap<>();
		} else {
			this.params = javax.servlet.http.HttpUtils.parseQueryString(input);
		}
	}
	
	public String get(final String paramName) {
		if (params.containsKey(paramName)) {
			return params.get(paramName)[0];
		} else {
			return null;
		}
	}
}