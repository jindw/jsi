package org.jside.jsi.tools.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;

public class MissedJSIPackage implements JSIPackage {
	public static final String NAME = "????";
	public static final String FILE_NAME = "???.js";
	public static Map<String, String> OBJECT_SCRIPT_MAP = new ContainsAllMap<String, String>();
	public static Map<String, List<String>> SCRIPT_OBJECT_MAP = new ContainsAllMap<String,  List<String>>();

	public MissedJSIPackage() {
	}

	public Map<String, String> getObjectScriptMap() {
		return OBJECT_SCRIPT_MAP;
	}

	public Map<String, List<String>> getScriptObjectMap() {
		return SCRIPT_OBJECT_MAP;
	}

	public void initialize() {

	}
	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad) {
	}

	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences) {
	}

	public Map<String, List<JSIDependence>> getDependenceMap() {
		return null;
	}

	public String getImplementation() {
		return null;
	}

	public Map<String, ScriptLoader> getLoaderMap() {
		return null;
	}

	public String getName() {
		return NAME;
	}

	public String loadText(String scriptName) {
		return null;
	}

	public void setImplementation(String implementation) {
	}
}
class ContainsAllSet<K> extends HashSet<K>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean contains(Object o) {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
}
class ContainsAllMap<K, V> extends HashMap<K, V>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Set<K> keys = new ContainsAllSet<K>(); 
	@Override
	public boolean containsKey(Object key) {
		return true;
	}
	@Override
	public boolean containsValue(Object value) {
		return true;
	}
	@Override
	public V get(Object key) {
		return null;
	}
	@Override
	public boolean isEmpty() {
		return false;
	}
	@Override
	public Set<K> keySet() {
		return keys;
	}
	
}
