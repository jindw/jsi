package org.xidea.jsi.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ImportServlet extends GenericServlet {

	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		String imports = req.getParameter("import");
		HttpServletResponse response = (HttpServletResponse) resp;
		response.reset();response.resetBuffer();
	}

}
