package org.xidea.jsi.impl.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIRuntime;

public class Console {
	private static final Log log = LogFactory.getLog(RuntimeSupport.class);

	private Console() {
	};

	public static void bind(JSIRuntime jr) {
		Console console = new Console();
		Object fn = jr
				.eval("var console = {};(function(impl){"
						+ "	function log(level,args){args,impl.log(null,level,[].join.call(args,' '))};"
						+ " var baseLevel = 0;"
						+ "	'trace,debug,info,warn,error,fatal'.replace(/\\w+/g,"
						+ "		function(k){var level = baseLevel++;"
						+ "			console[k] = function(){log(level,arguments)}"
						+ "		});			              "
						+ "	console.dir = function(o){for(var n in o){console.log(n,o[n]);}};"
						+ "	console.time = function(l){this['#'+l] = +new Date};"
						+ "	console.timeEnd = function(l){this.info(l + (new Date-this['#'+l]));};"
						+ "	console.assert = console.assert || function(l){if(!l){console.error('Assert Failed!!!')}};"
						+ " })");
		jr.invoke(null, fn, console);
	}

	// "trace,debug,info,warn,error,fatal"
	public boolean log(String title, int level, String msg) {
		List<String> jsName = getFileStack();
		msg += "[scriptName]:" + jsName;
		if (title == null) {
			title = jsName.get(0).substring(jsName.indexOf('/') + 1);
		}
		Log log = LogFactory.getLog(title);
		switch (level) {
		case 0:
			log.trace(msg);
			break;
		case 1:
			log.debug(msg);
			break;
		case 2:
			log.info(msg);
			break;
		case 3:
			log.warn(msg);
			break;
		case 4:
			log.error(msg);
			break;
		case 5:
			log.fatal(msg);
			break;
		default:
			log.info(msg);
		}
		return true;
	}

	private static List<String> getFileStack() {
		String firstFile = null;
		ArrayList<String> jsName = new ArrayList<String>();
		for (StackTraceElement s : new Exception().getStackTrace()) {
			String fileName = s.getFileName();
			if (fileName != null && !fileName.endsWith(".java")) {
				if (firstFile == null) {
					firstFile = fileName;
				}
				if (!firstFile.equals(fileName)) {
					int pos = jsName.size() - 1;
					int line = s.getLineNumber();
					if (pos >= 0) {
						String file = jsName.get(pos);
						if (file.startsWith(fileName)) {
							if (file.equals(fileName + "@-1")) {
								jsName.remove(pos);
							} else {
								if (line == -1) {
									continue;
								}
							}
						}
					}
					if (!log.isDebugEnabled()) {
						if (jsName.size() > 10) {
							break;
						} else if (jsName.size() > 3) {
							fileName = fileName.substring(fileName
									.lastIndexOf('/') + 1);
						}
					}
					jsName.add(fileName + '@' + line);
				}
			}
		}
		return jsName;
	}

}
