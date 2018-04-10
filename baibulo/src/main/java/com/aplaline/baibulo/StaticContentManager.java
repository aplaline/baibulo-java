package com.aplaline.baibulo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		String version = getVersionExtractor.extractVersionFromRequest(req);
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
		String version = putVersionExtractor.extractVersionFromRequest(req);
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
