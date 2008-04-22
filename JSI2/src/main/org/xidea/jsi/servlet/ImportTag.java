package org.xidea.jsi.servlet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class ImportTag extends BodyTagSupport {
	private String boot;
	private String compress;
	private String debug;

	@Override
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		super.release();
	}
}
