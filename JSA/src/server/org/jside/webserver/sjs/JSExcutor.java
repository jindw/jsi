package org.jside.webserver.sjs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSExcutor {

	private static Log log = LogFactory.getLog(JSExcutor.class);
	private static ThreadLocal<JSExcutor> instance = new ThreadLocal<JSExcutor>();
	private ScriptableObject globalScope;
	private Scriptable localScope;

	public static JSExcutor getCurrentInstance() {
		JSExcutor ex = instance.get();
		if(ex == null){
			ex = new JSExcutor();
			instance.set(ex);
		}
		return ex;
	}

	public JSExcutor() {
		try {
			Context context = Context.enter();
			try {
				this.globalScope = ScriptRuntime.getGlobal(context);
				URL boot = JSExcutor.class.getResource("/boot.js");
				if (boot != null) {
					try {
						context.evaluateString(globalScope, loadText(boot),
								"/boot.js", 1, null);
					} catch (Exception e) {
						log.error("尝试加载JSI失败", e);
					}
					try {
						context.evaluateString(globalScope,
								"$import('org.jside.webserver.sjs.*')",
								"org.jside.webserver.sjs.*", 1, null);
					} catch (Exception e) {
						log.error("尝试加载默认JS对象集合失败", e);
					}
				}
			} finally {
				Context.exit();
			}
		} catch (Exception e) {
			log.error("初始化Rhino JS引擎失败", e);
		}
	}

	public Object eval(URL resource, Map<String, Object> globals)
			throws IOException {
		return eval(loadText(resource), globals);

	}

	public Object eval(Reader in, Map<String, Object> globals)
			throws IOException {
		return eval(loadText(in), globals);
	}

	private static String loadText(URL resource) throws IOException {
		InputStreamReader reader = new InputStreamReader(resource.openStream(),
				"UTF-8");
		try {
			return loadText(reader);
		} finally {
			reader.close();
		}

	}

	private static String loadText(Reader in) throws IOException {
		StringWriter out = new StringWriter();
		int count;
		char[] cbuf = new char[1024];
		while ((count = in.read(cbuf)) > -1) {
			out.write(cbuf, 0, count);
		}
		return out.toString();
	}
	public Object getGlobals(){
		return localScope;
	}

	public synchronized Object eval(String source, Map<String, Object> globals) {
		Context context = Context.enter();
		try {
			context.getWrapFactory().setJavaPrimitiveWrap(true);
			this.localScope = context.newObject(globalScope);
			if (globals != null) {
				for (String key : globals.keySet()) {
					localScope.put(key, localScope, globals.get(key));
				}
			}
			return context
					.evaluateString(localScope, source, "<file>", 1, null);
		} finally {
			Context.exit();
		}
	}

}
