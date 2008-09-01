package org.xidea.jsi.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.xidea.jsi.JSIExportorFactory;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;

/**
 * 貌似没有这个必要，使用 JSIFilter 即可。
 * 留给用户当文档看待吧。
 * @author jindw
 */
class ExportServlet extends GenericServlet {
	private JSIExportorFactory exportorFactory = new DefaultJSIExportorFactory();
	@Override
	public void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		JSIRoot root = getJSIRoot();
		DefaultJSILoadContext context = new DefaultJSILoadContext();
		String[] imports = req.getParameterValues("import");
		HttpServletResponse response = (HttpServletResponse) resp;
		for (String param : imports) {
			String[] items = param.split("[,\\s]");
			for (String item : items) {
				root.$import(item,context);
			}
		}
		String result = exportorFactory.createSimpleExplorter().export( context,null);
		response.getWriter().print(result);
	}
	private JSIRoot getJSIRoot(){
		return null;//(JSIRoot) this.getServletContext().getAttribute(JSIFilter.GLOBAL_JSI_ROOT_KEY);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	

}
