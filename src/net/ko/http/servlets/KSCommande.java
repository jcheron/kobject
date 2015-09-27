package net.ko.http.servlets;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.cmd.KCmdInterface;
import net.ko.framework.Ko;
import net.ko.utils.KString;

/**
 * Servlet implementation class KSCommande
 */
@WebServlet(name="KSCommande", urlPatterns = { "*.cmd" })
public class KSCommande extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KSCommande() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		String action = request.getServletPath();
		action=KString.getLastAfter(action, "/");
		action=action.replace(".cmd", "");
		Class<? extends KCmdInterface> cmdClass;
		try {
			cmdClass = (Class<?extends KCmdInterface>) Class.forName("net.ko.cmd.K"+KString.capitalizeFirstLetter(action));
			KCmdInterface cmd = cmdClass.newInstance();
			cmd.run(request, response);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Ko.klogger().log(Level.SEVERE, "Commande inconnue : "+action, e);
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
