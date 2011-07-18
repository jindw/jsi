package org.jside.jsi.tools.ui.frame;

import java.io.InputStream;
import java.util.Locale;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;
import org.jside.webserver.proxy.ProxyFilter;
import org.xidea.commons.i18n.TextResource;

public class JSFormatFilter implements ProxyFilter {
	private JavaScriptCompressor compressor=JSAToolkit.getInstance().createJavaScriptCompressor();
	private JavaScriptCompressorConfig config=JSAToolkit.getInstance().createJavaScriptCompressorConfig();
	{
		compressor.setCompressorConfig(config);
		config.setAscii(false);
	}
	public String filter(String content) {
		return compressor.format(content);
	}

	public String findEncoding(InputStream content) {
		String encoding = TextResource.create(content, Locale.CHINA, null).getEncoding();
		return encoding;
	}

}
