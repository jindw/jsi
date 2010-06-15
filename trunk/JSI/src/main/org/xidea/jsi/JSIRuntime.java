package org.xidea.jsi;

import java.net.URL;
import java.util.Map;

public interface JSIRuntime {

	public abstract Object eval(URL resource);
	
	public abstract Object eval(String source);

	public abstract Object eval(String code, String path,
			Map<String, Object> vars);

	public abstract Object invoke(Object thisObj, Object function,
			Object... args);

	public <T> T wrapToJava(final Object thiz, Class<T> clasz);

}