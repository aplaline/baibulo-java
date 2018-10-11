package com.aplaline.baibulo;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Version extractor that delegates in chain to other version extractors
 * and returns the first found version 
 */
public class CompoundVersionExtractor implements VersionExtractor {
	private final List<VersionExtractor> extractors;
	
	public CompoundVersionExtractor(VersionExtractor ... extractors) {
		this.extractors = Arrays.asList(extractors);
	}
	
	@Override
	public String extractVersionFromRequest(HttpServletRequest request) {
		for (int i = 0; i < extractors.size(); i++) {
			final String result = extractors.get(i).extractVersionFromRequest(request);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
