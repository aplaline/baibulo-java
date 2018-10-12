package com.aplaline.baibulo;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CookieVersionExtractorTest {
	@Test
	public void willReturnNullIfNoVersionSpecified() {
		CookieVersionExtractor extractor = new CookieVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willReturnNullIfNoVersionSpecifiedButOtherCookiesExist() {
		CookieVersionExtractor extractor = new CookieVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie[] cookies = new Cookie[] {
				new Cookie("a", "b")
		};
		when(request.getCookies()).thenReturn(cookies);
		
		String expected = null;
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}

	@Test
	public void willRetrieveVersionInformation() {
		CookieVersionExtractor extractor = new CookieVersionExtractor();

		HttpServletRequest request = mock(HttpServletRequest.class);
		Cookie[] cookies = new Cookie[] {
				new Cookie(CookieVersionExtractor.VERSION_COOKIE_NAME, "TST-1234")
		};
		when(request.getCookies()).thenReturn(cookies);
		
		String expected = "TST-1234";
		String actual = extractor.extractVersionFromRequest(request);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void willSetVersionCookie() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		final List<Cookie> cookies = new ArrayList<>();

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				cookies.add(invocation.getArgumentAt(0, Cookie.class));
				return null;
			}
		}).when(response).addCookie(Mockito.any(Cookie.class));
		CookieVersionExtractor.setVersionCookie(response, "TST-1234");
		
		assertEquals(cookies.get(0).getName(), CookieVersionExtractor.VERSION_COOKIE_NAME);
		assertEquals(cookies.get(0).getValue(), "TST-1234");
	}
}
