package org.xidea.jsi.web;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * JSICDN,通过URL 按需装载脚本库
 * path:/cdn/org.jside:Tween+org.jside:format+org.xidea.test.*
 * @author jindw
 */
public class JSICDNServlet extends GenericServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest)req;
		String uri = request.getRequestURI();
		String cp = uri.substring(uri.lastIndexOf('/'));
		cp.split("[^\\w\\.\\:\\$]");
	}

	
}
