package org.jside.jsi.tools;

import java.util.Collection;
import java.util.List;

public interface JavaScriptAnalysisResult {

	public abstract Collection<String> getLocalVars();

	public abstract Collection<String> getReservedVars();

	public abstract Collection<String> getExternalVars();

	public abstract Collection<String> getTopExternalVars();

	public abstract Collection<String> getUnknowVars();

	public abstract Collection<String> getFeatures();

	public abstract int getReferenceCount(String var);

	public abstract List<JavaScriptError> getErrors();

	public abstract boolean isUnsafe();

}