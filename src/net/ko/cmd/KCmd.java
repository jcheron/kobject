package net.ko.cmd;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public abstract class KCmd implements KCmdInterface {
	protected PrintWriter getOut(HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF8");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		return out;
	}
}
