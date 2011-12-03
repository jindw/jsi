package org.xidea.jsi;

public class PackageSyntaxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PackageSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackageSyntaxException(String message) {
		super(message);
	}

}
