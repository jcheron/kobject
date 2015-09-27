package net.ko.http.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KoHttp;

/**
 * Servlet implementation class KSAjaxRequest
 */
public class KSAjaxRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KSAjaxRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    String contentType = request.getContentType();  // get the incoming type
	    if (contentType == null) return;  // nothing incoming, nothing to do
	    response.setContentType(contentType);  // set outgoing type to be incoming type

	    PrintWriter out = response.getWriter();

	    BufferedReader in = request.getReader();
	    String content=in.toString();
	    if(KoHttp.allowAjaxIncludes){
	    	String ajax=KoHttp.kajaxIncludes(request, response);
	    	content=content.replaceFirst("(?i)</body>", ajax+"</body>");
	    }
	    out.print(content);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
