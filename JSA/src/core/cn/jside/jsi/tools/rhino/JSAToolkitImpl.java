package cn.jside.jsi.tools.rhino;

import java.io.IOException;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;

import cn.jside.jsi.tools.JavaScriptCompressorAdaptor;

public class JSAToolkitImpl extends JSAToolkitImplBase {

	public static void main(String[] args) throws Exception {
	}

	public static JSAToolkit getInstance() {
		if (instance == null) {
			try {
				instance = new JSAToolkitImpl();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return instance;
	}

	private JSAToolkitImpl() throws IOException {
		super();
	}

}

class JSAToolkitImplBase extends JSAToolkit {
	protected static JSAToolkit instance;
	private JavaScriptCompressor compressor;
	public JSAToolkitImplBase() throws IOException {
	}
	public JavaScriptCompressor createJavaScriptCompressor() {
		if (compressor == null) {
			compressor = new JavaScriptCompressorAdaptor();
			compressor.setCompressorConfig(this.createJavaScriptCompressorConfig());
		}
		return compressor;
	}


}