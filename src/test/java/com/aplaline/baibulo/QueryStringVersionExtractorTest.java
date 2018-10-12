package com.aplaline.baibulo;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class QueryStringVersionExtractorTest {
	@Test
	public void willReturnNullIfNoQueryStringAvailable() {
		QueryStringVersionExtractor extractor = new QueryStringVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willReturnNullIfNoVersionSpecified() {
		QueryStringVersionExtractor extractor = new QueryStringVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getQueryString()).thenReturn("x=1&y=2");
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willRetrieveVersionInformation() {
		QueryStringVersionExtractor extractor = new QueryStringVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getQueryString()).thenReturn("x=1&y=2&version=TST-1234");
		
		String expected = "TST-1234";
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}
}
