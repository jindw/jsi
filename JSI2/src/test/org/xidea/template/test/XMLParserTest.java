package org.xidea.template.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsel.JSExpressionFactory;
import org.xidea.template.Template;
import org.xidea.template.XMLParser;
public class XMLParserTest {

	private XMLParser parser;
	@Before
	public void setUp() throws Exception {
		parser = new XMLParser();
	}
	public void test(String template,String value){
		Template t = new Template(template,parser);
		StringWriter out = new StringWriter();
		HashMap<Object, Object> model = new HashMap<Object, Object>();
		model.put("test",true);
		model.put("value",true);
		try {
			t.render(model, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertEquals(value,out.toString());
	}

	@Test
	public void testIf() throws IOException {
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>" +
				"<c:if test='${test}'>${value}</c:if>" +
				"</xml>",
				"<xml>true</xml>");
	}

	@Test
	public void testIfElse() throws IOException {
		parser.setExpressionFactory(new JSExpressionFactory());
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>" +
				"<c:if test='${test}'>${!value}</c:if>" +
				"<c:else test='${test}'>${value}</c:else>" +
				"</xml>",
				"<xml>false</xml>");
	}

	@Test
	public void testForElse() throws IOException {
		parser.setExpressionFactory(new JSExpressionFactory());
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>" +
				"<c:for var='value' items='${[1,2,3,4]}'>${value}</c:for>" +
				"<c:else test='${test}'>${value}</c:else>" +
				"</xml>",
				"<xml>1234</xml>");
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>" +
				"<c:for var='value' items='${[]}'>${value}</c:for>" +
				"<c:else test='${test}'>${value}</c:else>" +
				"</xml>",
				"<xml>true</xml>");
	}

}
