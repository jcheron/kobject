package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.http.objects.KRequest;
import net.ko.utils.KStrings;

/**
 * Servlet implementation class KSCssMapper
 */
@WebServlet(name="KSCssMapper", urlPatterns = { "*.kcss" })
public class KSCssMapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KSCssMapper() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri=request.getServletPath().replace(".kcss", ".css");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/css");
		PrintWriter out=response.getWriter();
		out.flush();
		
		String responseText="";
		if(Ko.styleSheets.containsKey(uri)){
			responseText=Ko.styleSheets.get(uri);
		}else{
			responseText=KRequest.includeResponse(uri, request, response);
			KStrings cssVars=Ko.kCssVars();
			for(String key:cssVars){
				String k=cssVars.get(key)+"";
				responseText=responseText.replaceAll("\\["+key+"\\]", k);
			}
			Ko.styleSheets.put(uri, responseText);
		}
		response.setContentLength(responseText.getBytes().length);
		out.write(responseText);
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


}
