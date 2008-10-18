package org.xidea.jsel;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.xidea.template.Expression;
import org.xidea.template.ExpressionFactory;

public class Java6JSExpressionFactory implements ExpressionFactory {
	private ScriptEngine engine;

	public Java6JSExpressionFactory() {
		ScriptEngineManager manager = new ScriptEngineManager();
		this.engine = manager.getEngineByExtension("js");
		try {
			this.engine.eval(new InputStreamReader(
					Java6JSExpressionFactory.class
							.getResourceAsStream("el.js")));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	public Expression createExpression(String value) {
		try {
			final String el = (String) ((Invocable) engine).invokeFunction(
					"__compile_EL__",  value);
			return new Expression() {
				public Object evaluate(Map<Object, Object> context) {
					try {
						Map map = (Map)context;
						Object value = engine.eval(el, new SimpleBindings(map));
						return ((Invocable) engine).invokeFunction(
								"__JS2JAVA__",  value);
					}  catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					}
					return null;

				}
			};
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		}

		return null;
	}
}