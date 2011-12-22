package org.xidea.jsi.impl.v2;

import java.util.HashMap;
import java.util.Map;


/**
 * @scope Session
 * @see DefaultScriptLoader
 * @author jindw
 */
class ScriptStatus {

	private Map<String, Boolean> flagMap = new HashMap<String, Boolean>();
	/**
	 * 测试是否已经装载，若未装载，也将其标记为装载
	 * @param object
	 * @return 是否已经装载
	 */
	public boolean load(String object) {
		if (flagMap == null) {
			return true;
		}else if (object == null) {
			flagMap = null;
			return false;
		} else {
			return flagMap.put(object, Boolean.TRUE) != null;
		}
	}
	/**
	 * @param object
	 * @return
	 */
	public boolean isLoaded(String object) {
		if (flagMap == null) {
			return true;
		}else if (object == null) {
			return false;
		} else {
			return flagMap.containsKey(object);
		}
	}
}
