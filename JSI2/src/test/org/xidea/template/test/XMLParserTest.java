package org.xidea.template.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.el.ExpressionFactoryImpl;
import org.xidea.template.Template;
import org.xidea.template.parser.XMLParser;

public class XMLParserTest {

	private XMLParser parser;

	@Before
	public void setUp() throws Exception {
		parser = new XMLParser();
		parser.setExpressionFactory(new ExpressionFactoryImpl());
	}

	public void test(String template, String value) {
		String result = renderTemplate(template);
		assertEquals(value, result);
	}

	private String renderTemplate(String template) {
		Template t = new Template(parser.parse(template));
		StringWriter out = new StringWriter();
		HashMap<Object, Object> model = new HashMap<Object, Object>();
		model.put("test", true);
		model.put("value", true);
		try {
			t.render(model, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String result = out.toString();
		return result;
	}

	@Test
	public void testAttrbutes() throws IOException {
		test("<xml attr=\"${test}\"/>", "<xml attr=\"true\"></xml>");
		test("<xml attr=\"${test}-\"/>", "<xml attr=\"true-\"></xml>");
		test("<xml attr=\"${false}\"/>", "<xml attr=\"false\"></xml>");
		test("<xml attr=\"${null}\"/>", "<xml></xml>");
		test("<xml attr=\"${''}\"/>", "<xml></xml>");
		test("<xml attr=\"${' ' }\"/>", "<xml attr=\" \"></xml>");
		test("<xml attr=\"${null}-\"/>", "<xml attr=\"null-\"></xml>");
		test("<xml attr=\"${''}-\"/>", "<xml attr=\"-\"></xml>");
	}

	@Test
	public void testIf() throws IOException {
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>"
				+ "<c:if test='${test}'>${value}</c:if>" + "</xml>",
				"<xml>true</xml>");
	}

	@Test
	public void testIfElse() throws IOException {
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>"
				+ "<c:if test='${test}'>${!value}</c:if>"
				+ "<c:else test='${test}'>${value}</c:else>" + "</xml>",
				"<xml>false</xml>");
	}

	@Test
	public void testFor() throws IOException {
		test(
				"<xml xmlns:c='http://www.xidea.org/ns/template'>"
						+ "<c:for var='value' items='${[1,2,3,4]}'>${for.index}${value}"
						+ "<c:if test='${for.lastIndex == for.index}'>${for.lastIndex}</c:if></c:for>"
						+ "<c:else test='${test}'>${value}</c:else>" + "</xml>",
				"<xml>011223343</xml>");
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>"
				+ "<c:for var='value' items='${[]}'>${value}</c:for>"
				+ "<c:else test='${test}'>${value}</c:else>" + "</xml>",
				"<xml>true</xml>");
	}

	@Test
	public void testForElse() throws IOException {
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>"
				+ "<c:for var='value' items='${[1,2,3,4]}'>${value}</c:for>"
				+ "<c:else test='${test}'>${value}</c:else>" + "</xml>",
				"<xml>1234</xml>");
		test("<xml xmlns:c='http://www.xidea.org/ns/template'>"
				+ "<c:for var='value' items='${[]}'>${value}</c:for>"
				+ "<c:else test='${test}'>${value}</c:else>" + "</xml>",
				"<xml>true</xml>");
	}

	@Test
	public void testInclude() throws IOException {
		URL res = this.getClass().getResource("include-test.xslt");
		String xslt = res.toString();
		test("<c:include xmlns:c='http://www.xidea.org/ns/template' path='"
				+ xslt + "'>" +
				"</c:include>",
		renderTemplate(xslt));
	}

	@Test
	public void testXsl() throws IOException {
		String xml = this.getClass().getResource("include-test.xml").toString();
		String xslt = this.getClass().getResource("include-test.xslt").toString();


		
		
		test("<c:include xmlns:c='http://www.xidea.org/ns/template' path='"
				+ xslt + "'>" + "</c:include>", renderTemplate(xslt));
		test(
				"<c:include xmlns:c='http://www.xidea.org/ns/template'"
						+ " name='thisValue' path='#thisValue' xpath='//meta' xslt='"
						+ xslt
						+ "'>"
						+ "<div><meta name='n1' value='v1'/><meta name='n2' value='v2'/></div></c:include>",
				"<div>n1n2</div>");
		test("<c:include xmlns:c='http://www.xidea.org/ns/template'"
				+ " name='thisValue' path='"+xml+"' xpath='//meta' xslt='#thisValue'>"
				+" <xsl:stylesheet version='1.0'"
				+"	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
				+"	<xsl:template match='/'>"
				+"		<div>"
				+"			<xsl:apply-templates select='//meta' />"
				+"		</div>"
				+"	</xsl:template>"
				+"	<xsl:template match='meta'>"
				+"		<xsl:value-of select='@name' />"
				+"	</xsl:template>"
				+"</xsl:stylesheet>"
				+ "</c:include>",
		"<div>n1n2</div>");
		test(
				"<c:include name='dataValue' path='#dataValue'" +
				" xmlns='http://www.w3c.org/xhtml' xmlns:c='http://www.xidea.org/ns/template'>"
				+"<c:include name='xmlValue' path='#xmlValue'>"
				+"<div><meta name='n1' value='v1'/>"
				+"<meta name='n2' value='v2'/>"
				+"</div></c:include>"
				+"<c:include"
						+ " name='thisValue' path='#xmlValue' xpath='//xhtml:meta' xslt='#thisValue'>"
						+" <xsl:stylesheet version='1.0'"
						+"  xmlns:xhtml='http://www.w3c.org/xhtml' "
						+"	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
						+"	<xsl:template match='/'>"
						+"		<div>"
						+"			<xsl:apply-templates select='//xhtml:meta' />"
						+"		</div>"
						+"	</xsl:template>"
						+"	<xsl:template match='xhtml:meta'>"
						+"		<xsl:value-of select='@name' />"
						+"	</xsl:template>"
						+"</xsl:stylesheet>"
						+ "</c:include></c:include>",
				"<div><meta name=\"n1\" value=\"v1\"/>"
				+"<meta name=\"n2\" value=\"v2\"/></div><div xmlns=\"http://www.w3c.org/xhtml\" xmlns:xhtml=\"http://www.w3c.org/xhtml\">n1n2</div>");
		
		
	}
}
