package net.ko.cmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.framework.Ko;
import net.ko.framework.KoHttp;

public class KRestart extends KCmd {

	@Override
	public int run(HttpServletRequest request, HttpServletResponse response) {
		try {
			KoHttp.krestart(request.getServletContext(), request.getParameter("restart"));
			PrintWriter out = getOut(response);
			out.print("Rechargement terminé<script type='text/javascript'>setTimeout(\"$('_commandesResponse').innerHTML='';\",10000);</script>");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException | IOException e) {
			Ko.klogger().log(Level.SEVERE, "Erreur lors du rechargement", e);
		}
		return 0;
	}

}
