package net.ko.mapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IMappingControl {
	public boolean isValid(HttpServletRequest request,HttpServletResponse response);
	public void onInvalidControl(HttpServletRequest request,HttpServletResponse response);
	public boolean beforeProcessAction(HttpServletRequest request,HttpServletResponse response);
}
