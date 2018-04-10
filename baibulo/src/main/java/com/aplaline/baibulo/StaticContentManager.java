package com.aplaline.baibulo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticContentManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(StaticContentManager.class);

	private final VersionExtractor getVersionExtractor = new CompoundVersionExtractor(
			new QueryStringVersionExtractor(),
			new VersionHeaderVersionExtractor(),
			new RefererHeaderVersionExtractor(),
			new CookieVersionExtractor(),
			new ReleaseVersionExtractor()
	);

	private final VersionExtractor putVersionExtractor = new CompoundVersionExtractor(
			new QueryStringVersionExtractor(),
			new VersionHeaderVersionExtractor()
	);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		retrieve(request, response);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (isUploadEnabled()) {
			store(request, response);
		} else {
			sendUploadDisabledMessage(response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (isUploadEnabled()) {
			store(request, response);
		} else {
			sendUploadDisabledMessage(response);
		}
	}

	private void retrieve(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		String version = getVersionExtractor.extractVersionFromRequest(request);
		Path file = getFileForResourceInVersion(request, version);
		log.info("Retrieving " + file.getParent().toString() + " in version " + version);

		if (file.toFile().exists()) {
			CookieVersionExtractor.setVersionCookie(response, version);
			sendResourceInVersion(response, file);
		} else {
			sendResourceNotFoundMessage(response, version, file);
		}
	}

	private void store(HttpServletRequest request, HttpServletResponse response) throws IOException, FileNotFoundException {
		String version = putVersionExtractor.extractVersionFromRequest(request);
		Path file = getFileForResourceInVersion(request, version);
		log.info("Storing " + request.getServletPath() + request.getPathInfo() + " in " + file.toString());

		saveResourceToFile(request, file);
		sendResourceCreatedMessage(response, file, version);
	}

	private Path getFileForResourceInVersion(HttpServletRequest request, String version) {
		return FileSystems.getDefault().getPath(getRoot(), request.getServletPath(), request.getPathInfo(), version);
	}

	private void sendResourceInVersion(HttpServletResponse response, Path file) throws FileNotFoundException, IOException {
		response.setContentType(URLConnection.guessContentTypeFromName(file.getParent().toString()));
		try (InputStream input = new FileInputStream(file.toFile())) {
			OutputStream output = response.getOutputStream();
			copyInputStreamToOutputStream(input, output);
		}
	}

	private void saveResourceToFile(HttpServletRequest request, Path file) throws IOException, FileNotFoundException {
		Files.createDirectories(file.getParent());
		InputStream input = request.getInputStream();
		try (OutputStream output = new FileOutputStream(file.toFile())) {
			copyInputStreamToOutputStream(input, output);
		}
	}

	private void copyInputStreamToOutputStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1) output.write(buffer, 0, len);
		output.flush();
	}

	private void sendResourceNotFoundMessage(HttpServletResponse response, String version, Path file) throws IOException {
		sendStatusAndMessage(response, HttpServletResponse.SC_NOT_FOUND, "Resource " + file.getParent() + " not found in version " + version);
	}

	private void sendResourceCreatedMessage(HttpServletResponse response, Path file, String version) throws IOException {
		sendStatusAndMessage(response, HttpServletResponse.SC_CREATED, "Resource " + file.getParent().toString() + " created in version " + version);
	}

	private void sendUploadDisabledMessage(HttpServletResponse response) throws IOException {
		sendStatusAndMessage(response, HttpServletResponse.SC_FORBIDDEN, "Upload disabled");
	}

	private void sendStatusAndMessage(HttpServletResponse response, int status, String message) throws IOException {
		log.info(message);
		response.setStatus(status);
		response.getOutputStream().println(message);
		response.getOutputStream().flush();
	}

	private boolean isUploadEnabled() {
		final String result = getInitParameter("upload-enabled");
		return result == null || result.equals("true");
	}

	private String getRoot() {
		final String result = getInitParameter("root");
		if (result == null) return "/tmp";
		else return result;
	}
}
