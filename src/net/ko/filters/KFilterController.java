package net.ko.filters;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;
import net.ko.mapping.KFilterMappings;

/**
 * Servlet Filter implementation class KFilterController
 */
@WebFilter(urlPatterns = { "*.do" }, dispatcherTypes = { DispatcherType.REQUEST })
public class KFilterController implements Filter {
	private Ko ko;

	/**
	 * Default constructor.
	 */
	public KFilterController() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		kstart(httpRequest.getServletContext());
		KFilterMappings filters = KoHttp.kfilters();
		if (filters != null) {
			if (!filters.process(httpRequest, (HttpServletResponse) response))
				chain.doFilter(request, response);
		} else
			chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	private void kstart(ServletContext context) {
		if (ko == null) {
			KoHttp.kstart(context, true);
			ko = KoHttp.getInstance();
		}
	}
}
