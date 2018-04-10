package com.aplaline.baibulo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticContentManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(StaticContentManager.class);

	public interface VersionExtractor {
		String extractVersionFromRequest(HttpServletRequest request);
	}

	public static class CompoundVersionExtractor implements VersionExtractor {
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

	public static class CookieVersionExtractor implements VersionExtractor {
		private static final String VERSION_COOKIE_NAME = "__version";
		private static final Logger log = LoggerFactory.getLogger(CookieVersionExtractor.class);

		@Override
		public String extractVersionFromRequest(HttpServletRequest request) {
			final Cookie[] cookies = request.getCookies();
			if (cookies == null) {
				log.info("No cookies found");
				return null;
			}
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(VERSION_COOKIE_NAME)) {
					log.info("Found " + VERSION_COOKIE_NAME + " with value " + cookies[i].getValue());
					return cookies[i].getValue();
				}
			}
			return null;
		}

		public static void setVersionCookie(HttpServletResponse response, String version) {
			response.addCookie(new Cookie(VERSION_COOKIE_NAME, version));
		}
	}

	public static final class QueryString {
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
	
	public static class QueryStringVersionExtractor implements VersionExtractor {
		public static final String VERSION_PARAM_NAME = "version";

		@Override
		public String extractVersionFromRequest(HttpServletRequest request) {
			final QueryString qs = new QueryString(request.getQueryString());
			return qs.get(VERSION_PARAM_NAME);
		}
	}
	
	public static class RefererHeaderVersionExtractor implements VersionExtractor {
		private static final String REFERER_HEADER_NAME = "Referer";
		private static final Logger log = LoggerFactory.getLogger(RefererHeaderVersionExtractor.class);
		
		@Override
		public String extractVersionFromRequest(HttpServletRequest request) {
			if (request.getHeader(REFERER_HEADER_NAME) != null) {
				URI referer = null;
				try {
					referer = new URI(request.getHeader(REFERER_HEADER_NAME));
				} catch (URISyntaxException e) {
					log.warn("Unable to extract version from existing referrer header: " + request.getHeader(REFERER_HEADER_NAME), e);
					return null;
				}
				final QueryString qs = new QueryString(referer.getRawQuery());
				return qs.get(QueryStringVersionExtractor.VERSION_PARAM_NAME);
			}
			return null;
		}
	}
	
	public static class ReleaseVersionExtractor implements VersionExtractor {
		@Override
		public String extractVersionFromRequest(HttpServletRequest request) {
			return "release";
		}
	}
	
	public static class VersionHeaderVersionExtractor implements VersionExtractor {
		private static final String VERSION_HEADER_NAME = "Version";
		
		@Override
		public String extractVersionFromRequest(HttpServletRequest request) {
			return request.getHeader(VERSION_HEADER_NAME);
		}
	}
	

	private final VersionExtractor get = new CompoundVersionExtractor(
			new QueryStringVersionExtractor(),
			new VersionHeaderVersionExtractor(),
			new RefererHeaderVersionExtractor(),
			new CookieVersionExtractor(),
			new ReleaseVersionExtractor()
	);

	private final VersionExtractor put = new CompoundVersionExtractor(
			new QueryStringVersionExtractor(),
			new VersionHeaderVersionExtractor()
	);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Retrieving data using GET...");
		retrieve(req, resp);
		log.info("Done");
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Storing data using PUT... " + req.getPathInfo() + ", context: " + req.getContextPath() + ", servlet: " + req.getServletPath());
		store(req, resp);
		log.info("Done");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Storing data using POST...");
		store(req, resp);
		log.info("Done");
	}

	private void retrieve(HttpServletRequest req, HttpServletResponse resp) throws FileNotFoundException, IOException {
		String version = get.extractVersionFromRequest(req);
		if (version == null) version = "release";
		CookieVersionExtractor.setVersionCookie(resp, version);
		Path file = FileSystems.getDefault().getPath("/tmp/", req.getServletPath(), req.getPathInfo(), version);
		if (file.toFile().exists()) {
			InputStream input = new FileInputStream(file.toFile());
			OutputStream output = resp.getOutputStream();
			copyStreamToStream(input, output);
		} else {
			resp.setStatus(HttpServletResponse.SC_CREATED);
			resp.getOutputStream().println("Resource " + req.getServletPath() + req.getPathInfo() + " not found in version " + version);
			resp.getOutputStream().flush();
		}
	}

	private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException, FileNotFoundException {
		String version = put.extractVersionFromRequest(req);
		Path folder = FileSystems.getDefault().getPath("/tmp/", req.getServletPath(), req.getPathInfo());
		Path file = FileSystems.getDefault().getPath("/tmp/", req.getServletPath(), req.getPathInfo(), version);
		Files.createDirectories(folder);
		InputStream input = req.getInputStream();
		OutputStream output = new FileOutputStream(file.toFile());
		copyStreamToStream(input, output);
		resp.setStatus(HttpServletResponse.SC_CREATED);
		resp.getOutputStream().println("Resource " + req.getServletPath() + req.getPathInfo() + " created in version " + version);
		resp.getOutputStream().flush();
	}

	private void copyStreamToStream(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = input.read(buffer)) > -1) output.write(buffer, 0, len);
			output.flush();
		} finally {
			output.close();
		}
	}
}
