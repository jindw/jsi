package org.xidea.jsi;

public interface JSIDependence {
	
	public abstract JSIPackage getTargetPackage();

	public abstract boolean isAfterLoad();
	
	public abstract String getThisObjectName();


	public abstract String getTargetFileName();

	public abstract String getTargetObjectName();

}