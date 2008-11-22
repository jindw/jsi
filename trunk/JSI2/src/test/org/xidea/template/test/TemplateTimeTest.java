package org.xidea.template.test;

import static org.junit.Assert.*;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xidea.template.Template;
import org.xidea.template.parser.ParseContext;
import org.xidea.template.parser.XMLParser;

@SuppressWarnings("unchecked")
public class TemplateTimeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test2() throws Exception {
		System.out.println(String[].class);
		System.out.println(Object[][].class);
		System.out.println(String[].class.getSuperclass());
		System.out.println(Object[][].class.getSuperclass());
		System.out.println(new String[0][0] instanceof Object[][]);

	}

	@Test
	public void test() throws Exception {
		ParseContext xcontext = new ParseContext();
		XMLParser parser = new XMLParser();
		parser.parse(this.getClass().getResource("asciitable.xhtml"), xcontext);
		Template xt1 = new Template1(xcontext.getResult());
		org.xidea.template.Template xt = new Template(xcontext.getResultTree());
		Template2 xt2 = new Template2(xcontext.getResultTree());
		HashMap context = new HashMap();
		context.put("data", Arrays.asList("0123456789ABCDEF".split("")));
		context.put("name", "test");
		context.put("border", "1px");
		String result1 = null;
		String result2 = null;
		String result3 = null;
		long t1 = 0;
		long t2 = 0;
		long t3 = 0;
		for (int i = 0; i < 5000; i++) {
			// result2 = testXMLTemplate(xt, context);
			long m1 = System.currentTimeMillis();
			result1 = testXMLTemplate(xt2, context);

			long m2 = System.currentTimeMillis();
			result2 = testXMLTemplate(xt, context);

			long m3 = System.currentTimeMillis();
			result3 = testXMLTemplate(xt1, context);

			long m4 = System.currentTimeMillis();
			t1 += m2 - m1;
			t2 += m3 - m2;
			t3 += m4 - m3;
		}

		System.out.println(result1);
		System.out.println("====================");
		System.out.println(result2);

		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t3);

		assertEquals(result1, result2);
		assertEquals(result3, result2);
	}

	public String testXMLTemplate(org.xidea.template.Template t, Map context)
			throws Exception {
		StringWriter out = new StringWriter();
		t.render(context, out);
		out.flush();
		return out.toString();
	}

}
