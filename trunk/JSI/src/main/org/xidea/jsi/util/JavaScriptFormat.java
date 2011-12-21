package org.xidea.jsi.util;

import org.mozilla.javascript.Decompiler;
import org.mozilla.javascript.UintMap;

public class JavaScriptFormat {
	public String format(String source){
		UintMap props = new UintMap();
		Decompiler.decompile(source, 0, props );
		return null;
	}
}
