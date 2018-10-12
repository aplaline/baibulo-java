package com.aplaline.baibulo;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class VersionHeaderVersionExtractorTest {
	@Test
	public void willReturnNullIfNoVersionSpecified() {
		VersionHeaderVersionExtractor extractor = new VersionHeaderVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willRetrieveVersionInformation() {
		VersionHeaderVersionExtractor extractor = new VersionHeaderVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader(VersionHeaderVersionExtractor.VERSION_HEADER_NAME)).thenReturn("TST-1234");
		
		String expected = "TST-1234";
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}
}
