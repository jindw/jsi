package org.xidea.jsi.web;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.DefaultJSILoadContext;
import org.xidea.jsi.impl.FileJSIRoot;

public class ExportTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 13434L;
	private static final Log log = LogFactory.getLog(ExportTag.class);
	private static final String IMPORT_FN = "$import";
	private static final String GLOBAL_JSI_ROOT_KEY = ExportTag.class.getName()
			+ ":ROOT";

	private final String scriptBase = "/scripts/";
	private boolean printSource;

	@Override
	public int doEndTag() throws JspException {
		try {
			String imports = this.getBodyContent().getString();
			JSIRoot root = getJSIRoot();
			DefaultJSILoadContext context = new DefaultJSILoadContext();
			int p1 = 0;
			while ((p1 = imports.indexOf(IMPORT_FN, p1)) > 0) {
				p1 = imports.indexOf('"', p1) + 1;
				if (p1 > 0) {
					int p2 = imports.indexOf('"', p1);
					if (p2 > 0) {
						root.$import(imports.substring(p1, p2), context);
					}
				}

			}
			JspWriter out = this.pageContext.getOut();
			if (printSource) {
				out.write("<script>\n");
				for (ScriptLoader loader : context.getScriptList()) {
					out.write(loader.getSource());
				}
				out.write(";\n");
				out.write("</script>");
			} else {
				for (ScriptLoader loader : context.getScriptList()) {

					out.write("<script src='");
					out.write(((HttpServletRequest)this.pageContext.getRequest())
							.getContextPath());
					out.write(scriptBase);
					out.write(loader.getPath());
					out.write("'></script>\n");

				}
			}

		} catch (IOException e) {
			log.debug(e);;
		}
		return SKIP_BODY;
	}

	private JSIRoot getJSIRoot() {
		ServletContext context = this.pageContext.getServletContext();
		JSIRoot root = (JSIRoot) context.getAttribute(GLOBAL_JSI_ROOT_KEY);
		if (root == null) {
			root = new FileJSIRoot(context.getRealPath(scriptBase), "utf-8");
			context.setAttribute(GLOBAL_JSI_ROOT_KEY, root);
		}
		return root;
	}

	public boolean isPrintSource() {
		return printSource;
	}

	public void setPrintSource(boolean printSource) {
		this.printSource = printSource;
	}

	public String getScriptBase() {
		return scriptBase;
	}

}
