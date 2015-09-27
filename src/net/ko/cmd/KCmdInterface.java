package net.ko.cmd;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface KCmdInterface {
	public int run(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
