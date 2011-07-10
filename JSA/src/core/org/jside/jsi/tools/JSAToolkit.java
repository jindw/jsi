package org.jside.jsi.tools;

import java.io.IOException;

public class JSAToolkit {
	private static JSAToolkit instance;

	public JSAToolkit() throws IOException {
	}

	public static JSAToolkit getInstance() {
		if (instance == null) {
			try {
				instance = (JSAToolkit) cn.jside.jsi.tools.rhino.JSAToolkitImpl
						.getInstance();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return instance;
	}

	public JavaScriptCompressorConfig createJavaScriptCompressorConfig() {
		return new JavaScriptCompressorConfig();
	}

	public native JavaScriptCompressor createJavaScriptCompressor();

	public static void main(String[] args) throws Exception {
		CompressAction.main(args);
	}
}
