package net.ko.http.controls;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.http.objects.KRequest;
import net.ko.utils.KDateUtils;

/**
 * Servlet implementation class KDtpServlet
 */
@WebServlet(name="KDtpServlet", urlPatterns = { "*.KDatePicker" })
public class KDtpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KDtpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String d=KRequest.GETPOST("d", request, new KDateUtils().toString());
		String sd=KRequest.GETPOST("sd", request);
		String id=KRequest.GETPOST("id", request, "id");
		String type=KRequest.GETPOST("type", request, "date");
		String inputId=KRequest.GETPOST("inputId", request, "inputId");
		KDatePicker dtp=null;
		if(sd!=null)
			dtp=new KDatePicker(d,sd,type);
		else
			dtp=new KDatePicker(d,d,type);
		dtp.addOn(id, inputId);
		PrintWriter out=response.getWriter();
		out.print(dtp);
	}

}
