package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.utils.KString;

/**
 * Servlet implementation class KJsMapper
 */
@WebServlet(name="KJsMapper", urlPatterns = { "*.kjs" })
public class KJsMapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KJsMapper() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getServletPath();
		action=KString.getLastAfter(action, "/");
		if(action.equalsIgnoreCase("messages.kjs")){
			String responseText=Ko.kjsValidatorVars();
			if(responseText!=null&&!responseText.equals("")){
				response.setCharacterEncoding("UTF-8");
				response.setContentType("application/javascript");
				PrintWriter out=response.getWriter();
				out.flush();
				response.setContentLength(responseText.getBytes().length);
				out.write(responseText);
				out.close();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
