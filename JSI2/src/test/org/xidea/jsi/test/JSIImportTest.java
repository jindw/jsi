package org.xidea.jsi.test;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;

import junit.framework.TestCase;

public class JSIImportTest extends TestCase {

	private JSIRoot root;
	private JSILoadContext loadContext;
	private String webimBase = "D:\\workspace\\WebIM\\web\\scripts";

	protected void setUp() throws Exception {
		super.setUp();
		root = new JSIRoot(webimBase, "utf-8");
		loadContext = new JSILoadContext();
		HttpServletRequest request = null;
		JspWriter out = null;
		String path = request.getParameter("path");
		int pos = path.lastIndexOf('/');
		String file = path.substring(pos+1);
		String pkg = path.substring(0,pos).replace('/', '.');
		java.io.InputStreamReader in = new java.io.InputStreamReader(request.getSession().getServletContext().getResourceAsStream(path),"utf-8");
		char[] buf = new char[1024];
		int count;
		while((count = in.read(buf))>=0){
			out.write(buf, 0, count);
		}

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testImport(){
		root.$import("com.baidu.webim.ui.UIConnector", loadContext);
		for (String file : loadContext.getFileList()) {
			System.out.println(file);
		}
	}
}
