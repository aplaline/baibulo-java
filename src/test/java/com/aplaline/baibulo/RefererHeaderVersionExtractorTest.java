package com.aplaline.baibulo;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class RefererHeaderVersionExtractorTest {
	@Test
	public void willReturnNullIfNoVersionSpecified() {
		RefererHeaderVersionExtractor extractor = new RefererHeaderVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader(VersionHeaderVersionExtractor.VERSION_HEADER_NAME)).thenReturn("http://www.test.com/one?two=3");
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willRetrieveVersionInformation() {
		RefererHeaderVersionExtractor extractor = new RefererHeaderVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader(RefererHeaderVersionExtractor.REFERER_HEADER_NAME)).thenReturn("http://www.test.com/one?two=3&version=TST-1234");
		
		String expected = "TST-1234";
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}
}
