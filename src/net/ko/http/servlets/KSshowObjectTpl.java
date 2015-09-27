package net.ko.http.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.http.views.KHttpShow;
import net.ko.http.views.KHttpShowTpl;
import net.ko.http.views.KMaskSOp;
import net.ko.kobject.KObject;
import net.ko.utils.KTextFile;

/**
 * Servlet implementation class KSshowObject
 */
@WebServlet(name="KSshowObjectTpl", urlPatterns = { "*.show" })
public class KSshowObjectTpl extends KSAbstractViewTpl {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KSshowObjectTpl() {
        super();
        extension="show";
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadTemplateView(String fileName, String className,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			PrintWriter out=response.getWriter();
			String strTemplate=KTextFile.open(fileName);
			KMaskSOp mask=new KMaskSOp(strTemplate);
			String classNameByOp=mask.getSpecificOperation("className",request);
			if(classNameByOp!=null)
				className=classNameByOp;
			KDebugConsole.print("ClassName:"+className+",fileName:"+fileName, "TEMPLATE", "KSshowObjectTpl.loadTemplateView");
			Class<KObject> clazz = (Class<KObject>) Class.forName(className);
			
			String strDisplay=mask.getSpecificOperation("koDisplay",request);
			KHttpShowTpl showForm=new KHttpShowTpl(clazz.newInstance(), request,response, mask.get(0));
			
			if(strDisplay!=null)
				showForm.setKoDisplay(strDisplay);
			showForm.getDefaultComplete("",response);
			out.print(showForm.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template : "+fileName, e);

		}
		

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadDefaultView(String className,
			HttpServletRequest request, HttpServletResponse response) {
		try{
			KDebugConsole.print(className, "TEMPLATE", "KSshowObjectTpl.loadDefaultView");
			PrintWriter out=response.getWriter();
			Class<KObject> clazz = (Class<KObject>) Class.forName(className);
			KHttpShow showForm=new KHttpShow(clazz.newInstance(), request);
			showForm.setResponse(response,true);
			showForm.getDefaultComplete("",response);
			out.print(showForm.toString());
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Impossible de charger le template par défaut pour la classe : "+className, e);
		}
	}

}
