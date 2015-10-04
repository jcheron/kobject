package net.ko.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class HttpCacheFilter implements Filter {

	// ---------------------------------------- ATTRIBUTS
	private final static String HEADER_GET_KEY = "Cache-Control";
	private final static String HEADER_PRAGMA = "Pragma";
	private final static String HEADER_EXPIRES = "Expires";

	private String cacheLifeTimeInstruction = null;

	// ------------------------------------------ FONCTIONS

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (null != cacheLifeTimeInstruction) {
			((HttpServletResponse) res).setHeader(HEADER_GET_KEY, cacheLifeTimeInstruction);
			((HttpServletResponse) res).setHeader(HEADER_PRAGMA, null);
			//
			final int CACHE_DURATION_IN_SECOND = 60 * 60 * 24 * 20; // 5 days
			final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND * 1000;
			long now = System.currentTimeMillis();
			((HttpServletResponse) res).setHeader("Last-Modified", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(now));
			((HttpServletResponse) res).setDateHeader(HEADER_EXPIRES, now + CACHE_DURATION_IN_MS);
		} // end-if

		chain.doFilter(req, res);
	}

	public void init(FilterConfig config) throws ServletException {
		cacheLifeTimeInstruction = config.getInitParameter(HEADER_GET_KEY);
	}

	public void destroy() {
		cacheLifeTimeInstruction = null;
	}

}
