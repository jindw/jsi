package org.xidea.script.javascript;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineProxy implements ScriptEngine {
	private final static ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine base;

	public ScriptEngine getScriptEngine() {
		if(base == null){
			base = manager.getEngineByExtension("js");
		}
		return base;
	}

	public Bindings createBindings() {
		return getScriptEngine().createBindings();
	}

	public Object eval(Reader reader, Bindings n) throws ScriptException {
		return getScriptEngine().eval(reader, n);
	}

	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		return getScriptEngine().eval(reader, context);
	}

	public Object eval(Reader reader) throws ScriptException {
		return getScriptEngine().eval(reader);
	}

	public Object eval(String script, Bindings n) throws ScriptException {
		return getScriptEngine().eval(script, n);
	}

	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		return getScriptEngine().eval(script, context);
	}

	public Object eval(String script) throws ScriptException {
		return getScriptEngine().eval(script);
	}

	public Object get(String key) {
		return getScriptEngine().get(key);
	}

	public Bindings getBindings(int scope) {
		return getScriptEngine().getBindings(scope);
	}

	public ScriptContext getContext() {
		return getScriptEngine().getContext();
	}

	public ScriptEngineFactory getFactory() {
		return getScriptEngine().getFactory();
	}

	public void put(String key, Object value) {
		getScriptEngine().put(key, value);
	}

	public void setBindings(Bindings bindings, int scope) {
		getScriptEngine().setBindings(bindings, scope);
	}

	public void setContext(ScriptContext context) {
		getScriptEngine().setContext(context);
	}

}
