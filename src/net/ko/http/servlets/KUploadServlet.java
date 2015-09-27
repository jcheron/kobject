package net.ko.http.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import net.ko.http.servlets.bean.KActionBean;
import net.ko.utils.KFileUtils;
import net.ko.utils.KString;

/**
 * Servlet implementation class KUploadServlet
 */
@WebServlet("/upload.frm")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
maxFileSize = 1024 * 1024 * 100, // 100MB
maxRequestSize = 1024 * 1024 * 500)
// 500MB
public class KUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String uploadDir = "uploadFiles";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public KUploadServlet() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		KActionBean bean = new KActionBean(request, response);
		PrintWriter out = bean.getOut();
		ServletContext application = bean.getApplication();
		boolean isRowMessage = false;
		String message = getParameter(request, "_messageMask");
		String valueMask = getParameter(request, "_valueMask");
		String strAccept = getParameter(request, "_accept");
		String uploadDir = getParameter(request, "_uploadDir", KUploadServlet.uploadDir);

		if (message == null) {
			message = "<div class='fileUpload'>{contextPathName}</div>";
		} else
			message = "<div class='fileUpload'>" + KString.decodeURIComponent(message) + "</div>";
		if (valueMask == null) {
			valueMask = "{contextPathName}";
		} else
			valueMask = KString.decodeURIComponent(valueMask);
		isRowMessage = message.contains("{") && message.contains("}");
		String fileNames = "";
		String appPath = request.getServletContext().getRealPath("");
		String savePath = appPath + File.separator + uploadDir;
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		for (Part part : request.getParts()) {
			String fileName = extractFileName(part);
			if (KString.isNotNull(fileName)) {
				File f = new File(savePath + File.separator + fileName);
				if (accept(strAccept, f)) {
					part.write(savePath + File.separator + fileName);
					if (isRowMessage) {
						String rowMessage = message.replace("{contextPathName}", application.getContextPath() + "/" + uploadDir + "/" + fileName);
						rowMessage = rowMessage.replace("{realPathName}", savePath + File.separator + fileName);
						rowMessage = rowMessage.replace("{fileName}", fileName);
						out.print(rowMessage);
					}
					String newFileName = valueMask.replace("{contextPathName}", application.getContextPath() + "/" + uploadDir + "/" + fileName);
					newFileName = newFileName.replace("{realPathName}", savePath + File.separator + fileName);
					newFileName = newFileName.replace("{fileName}", fileName);
					if ("".equals(fileNames))
						fileNames = newFileName;
					else
						fileNames += ";" + newFileName;
				} else
					out.print("<div class='errMessage'>L'extension du fichier " + fileName + " n'est pas autorisée</div>");
			}
		}
		if (!isRowMessage)
			out.print(message);
		boolean hasInput = false;
		String inputName = getParameter(request, "_inputName");
		hasInput = KString.isNotNull(inputName);

		if (hasInput) {
			out.print("<span id='span_" + inputName + "'></span>");
			out.print("<script>" +
					"var inputZone=Forms.DOM.insertIn('" + inputName + "',$('span_" + inputName + "'),'input');" +
					"inputZone.type='text';" +
					"inputZone.value='" + fileNames + "';" +
					"Forms.Utils.fireEvent(inputZone,'change');</script>");
			// out.print("<input type='hidden' id='" + inputName + "' name='" +
			// inputName + "' value='" + fileNames + "'>");
		}

	}

	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}

	private String getParameter(HttpServletRequest request, String key) throws IllegalStateException, IOException, ServletException {
		String value = null;
		Part myStringPart = request.getPart(key);
		if (myStringPart != null) {
			Scanner scanner = new Scanner(myStringPart.getInputStream());
			value = scanner.nextLine();
			scanner.close();
		}
		return value;
	}

	private String getParameter(HttpServletRequest request, String key, String defaultValue) throws IllegalStateException, IOException, ServletException {
		String value = defaultValue;
		Part myStringPart = request.getPart(key);
		if (myStringPart != null) {
			Scanner scanner = new Scanner(myStringPart.getInputStream());
			value = scanner.nextLine();
			scanner.close();
			if (KString.isNull(value))
				value = defaultValue;
		}
		return value;
	}

	private boolean accept(String strAccept, File f) {
		boolean accept = true;
		if (KString.isNotNull(strAccept)) {
			String[] extensions = strAccept.split("\\W");
			String ext = KFileUtils.getExtension(f);
			if (KString.isNotNull(ext))
				accept = (java.util.Arrays.asList(extensions).indexOf(ext) != -1);
		}
		return accept;
	}
}
