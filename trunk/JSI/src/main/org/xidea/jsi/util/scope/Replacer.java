package org.xidea.jsi.util.scope;

import java.util.Map;

public interface Replacer {

	public abstract boolean isUnsafe();

	public abstract Map<String, Integer> getReferenceCountMap();

	public abstract String findReplacedVar(String name);

	public abstract String findReplacedLabel(String name);

}