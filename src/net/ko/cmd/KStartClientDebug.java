package net.ko.cmd;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ko.debug.KDebugClient;
import net.ko.http.objects.KRequest;

public class KStartClientDebug extends KCmd {

	@Override
	public int run(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean activate = KRequest.GETPOST("clientDebug", request, false);
		KDebugClient.setActive(activate);
		PrintWriter out = getOut(response);
		if (activate)
			out.print("Client debug activé");
		else
			out.print("Client debug désactivé");
		return 0;
	}

}
