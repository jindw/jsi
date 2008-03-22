package org.xidea.jsi;

import java.util.HashMap;
import java.util.Map;

public class ScriptStatus {

	private Map<String, Boolean> flagMap = new HashMap<String, Boolean>();

	/**
	 * @param object
	 * @return 是否已经装载
	 */
	public boolean testObject(String object) {
		if (flagMap == null) {
			return true;
		}else if (object == null) {
			flagMap = null;
			return false;
		} else {
			return flagMap.put(object, Boolean.TRUE) != null;
		}
	}

}
