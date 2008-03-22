package org.xidea.jsi;


public interface PackageParser {

	public static final String SET_IMPLEMENTATION = "setImplementation";
	public static final String ADD_SCRIPT = "addScript";
	public static final String ADD_DEPENDENCE = "addDependence";
	public abstract void parse(String source, JSIPackage pkg) throws UnsupportedSyntaxException;

}