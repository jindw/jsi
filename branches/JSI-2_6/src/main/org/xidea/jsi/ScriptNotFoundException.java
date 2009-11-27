package org.xidea.jsi;

public class ScriptNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScriptNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptNotFoundException(String message) {
		super(message);
	}

}
