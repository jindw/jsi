package org.xidea.jsi.impl;

import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.UnsupportedSyntaxException;



interface PackageParser {
	public static final String SET_IMPLEMENTATION = "setImplementation";
	public static final String ADD_SCRIPT = "addScript";
	public static final String ADD_DEPENDENCE = "addDependence";
	public abstract void parse(String source, JSIPackage pkg) throws UnsupportedSyntaxException;
}