package cn.jside.jsi.tools.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.export.JSAExportorFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.impl.DataRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;

public class JSIExporterTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExport() throws UnsupportedEncodingException, IOException {
		String source = loadData("test.xml");
		JSIRoot root = new DataRoot(source);
		DefaultLoadContext loadContext = new DefaultLoadContext();
		String[] export = root.loadText("", "export").split("[,\\s]+");
		for (int i = 0; i < export.length; i++) {
			root.$import(export[i], loadContext);
		}
		HashMap<String, String[]> params = new HashMap<String, String[]>();
		params.put("lineSeperator",new String[]{"\r\n\r\n"});
		String result = new JSAExportorFactory().createExplorter(DefaultExportorFactory.TYPE_EXPORT_CONFUSE,params).export(loadContext);
		Assert.assertTrue("不能压出外部变量啊！！", JSAToolkit.getInstance()
				.createJavaScriptCompressor().analyse(result).getUnknowVars().isEmpty());
		System.err.println(result);
	}

	@Test
	public void testExport2() throws UnsupportedEncodingException, IOException {
		String source = loadData("test2.xml");
		JSIRoot root = new DataRoot(source);
		JSILoadContext loadContext = new DefaultLoadContext();
		String[] export = root.loadText("", "export").split("[,\\s]+");
		for (int i = 0; i < export.length; i++) {
			root.$import(export[i], loadContext);
		}

		Map<String, String> config= new HashMap<String, String>();
		//"confuse",new JavaScriptCompressorConfig(),, false
		HashMap<String, String[]> params = new HashMap<String, String[]>();
		params.put("lineSeperator",new String[]{"\r\n\r\n"});
		String result = new JSAExportorFactory().createExplorter(DefaultExportorFactory.TYPE_EXPORT_CONFUSE,params).export(loadContext);
		Collection<String> unknow = JSAToolkit.getInstance()
				.createJavaScriptCompressor().analyse(result).getUnknowVars();
		System.err.println(unknow);
		Assert.assertTrue("不能压出外部变量啊！！", unknow.isEmpty());
		System.out.println(result);
	}

	private String loadData(String file) throws UnsupportedEncodingException,
			IOException {
		java.io.InputStreamReader in = new java.io.InputStreamReader(this
				.getClass().getResourceAsStream(file), "utf-8");
		char[] buf = new char[1024];
		int count;
		StringWriter out = new StringWriter();
		while ((count = in.read(buf)) >= 0) {
			out.write(buf, 0, count);
		}
		return out.toString();

	}
}
