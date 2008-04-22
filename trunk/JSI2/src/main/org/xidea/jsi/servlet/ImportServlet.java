package org.xidea.jsi.servlet;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ImportServlet extends GenericServlet {

	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		String imports = req.getParameter("import");
		HttpServletResponse response = (HttpServletResponse) resp;
		response.reset();response.resetBuffer();
	}

}
