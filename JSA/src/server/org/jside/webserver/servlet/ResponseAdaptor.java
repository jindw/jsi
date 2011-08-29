package org.jside.webserver.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jside.webserver.RequestUtil;
import org.jside.webserver.WebServer;


abstract class ResponseAdaptor extends RequestAdaptor implements HttpServletResponse{


	public void addCookie(Cookie cookie) {
		try {
			base().addResponseHeader("Cookie:"+
					URLEncoder.encode(cookie.getName(), "UTF-8")+'='+
					URLEncoder.encode(cookie.getValue(), "UTF-8")
					);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public void addDateHeader(String arg0, long arg1) {
		addHeader(arg0, new Date(arg1).toString());
	}

	public void addHeader(String key, String value) {
		base().addResponseHeader(key+":"+value);
	}

	public void addIntHeader(String key, int value) {
		base().addResponseHeader(key+":"+value);
		
	}

	public boolean containsHeader(String key) {
		return getHeader(key) != null;
	}

	public String encodeRedirectURL(String url) {
		return url;
	}

	public String encodeRedirectUrl(String url) {
		return encodeRedirectURL(url);
	}

	public String encodeURL(String url) {
		return url;
	}

	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	public void sendError(int status) throws IOException {
		this.setStatus(status);
	}

	public void sendError(int status, String msg) {
		base().setStatus(status, msg);
	}

	public void sendRedirect(String href) throws IOException {
		RequestUtil.sendRedirect(href);
	}

	public void setDateHeader(String key, long value) {
		setHeader(key,new Date( value).toString());
		
	}

	public void setHeader(String key, String value) {
		base().setResponseHeader(key+":"+value);
		
	}

	public void setIntHeader(String key, int value) {
		setHeader(key,""+value);
		
	}

	public void setStatus(int status) {
		base().setStatus(status, "");
	}

	public void setStatus(int status, String msg) {
		this.sendError(status, msg);
	}

	public void flushBuffer() throws IOException {
		base().getOutputStream().flush();
	}

	public int getBufferSize() {
		return 0;
	}

	public Locale getLocale() {
		return Locale.getDefault();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream(){
			@Override
			public void write(int b) throws IOException {
				base().getOutputStream().write(b);
			}
			public void write(byte b[], int off, int len) throws IOException {
				base().getOutputStream().write(b,off,len);
			}
			
		};
	}

	
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(new OutputStreamWriter(base().getOutputStream(),"UTF-8"),true);
	}

	public boolean isCommitted() {
		return base().isAccept();
	}

	public void reset() {
		
	}

	public void resetBuffer() {
		
	}

	public void setBufferSize(int arg0) {
		
	}

	public void setCharacterEncoding(String encoding) {
		base().setEncoding(encoding);
	}

	public void setContentLength(int length) {
		this.addIntHeader("Content-Length", length);
	}

	public void setContentType(String arg0) {
		base().setContentType(arg0);
	}

	public void setLocale(Locale arg0) {
	}


}
