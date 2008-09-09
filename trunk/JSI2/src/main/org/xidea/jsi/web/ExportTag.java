package org.xidea.jsi.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.TagSupport;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DefaultJSIExportorFactory;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.JSIUtil;

public class ExportTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 13434L;
	private JSIExportor exportor = JSIUtil.getExportor(JSIExportor.TYPE_SIMPLE);

	@Override
	public int doEndTag() throws JspException {
		JSIRoot root = getJSIRoot();
		DefaultJSILoadContext context = new DefaultJSILoadContext();
		String[] imports = this.pageContext.getRequest().getParameterValues("import");;
		this.getBodyContent().getString();
		
		for (String param : imports) {
			String[] items = param.split("[,\\s]");
			for (String item : items) {
				root.$import(item, context);
			}
		}
		String result = exportor.export(context,
				null);
		try {
			this.pageContext.getOut().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}

	private JSIRoot getJSIRoot() {
		return null;// (JSIRoot)
					// this.getServletContext().getAttribute(JSIFilter.GLOBAL_JSI_ROOT_KEY);
	}

	@Override
	public BodyContent getBodyContent() {
		// TODO Auto-generated method stub
		return super.getBodyContent();
	}

}
