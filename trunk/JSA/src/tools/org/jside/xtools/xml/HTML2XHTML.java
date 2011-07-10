//package org.jside.xtools.xml;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.io.Writer;
//import java.net.URI;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.cyberneko.html.parsers.DOMParser;
//import org.w3c.dom.Attr;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//import org.xidea.lite.parse.NodeParser;
//import org.xidea.lite.parse.ParseChain;
//import org.xidea.lite.parse.ParseContext;
//import org.xidea.lite.impl.DefaultXMLNodeParser;
//import org.xidea.lite.impl.ParseConfigImpl;
//import org.xidea.lite.impl.ParseContextImpl;
//import org.xidea.lite.impl.TextNodeParser;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import org.xml.sax.SAXNotRecognizedException;
//import org.xml.sax.SAXNotSupportedException;
//
//@SuppressWarnings("unchecked")
//public class HTML2XHTML implements NodeParser<Element> {
//	Map<String, String> features = new HashMap<String, String>();
//	private DOMParser htmlParser;// = new
//
//	// org.cyberneko.html.parsers.DOMParser();
//
//	public HTML2XHTML() {
//		try {
//			htmlParser = new DOMParser();
//			setup(htmlParser);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void setup(DOMParser config) throws SAXNotRecognizedException, SAXNotSupportedException {
//		//HTMLConfiguration config = new HTMLConfiguration();
//
//		config.setFeature("http://xml.org/sax/features/namespaces", true);
//		config.setFeature(
//				"http://cyberneko.org/html/features/insert-namespaces", true);
//		config.setFeature("http://cyberneko.org/html/features/insert-doctype",
//				true);
//		config
//				.setFeature(
//						"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
//						true);
//		config.setFeature(
//				"http://cyberneko.org/html/features/scanner/cdata-sections",
//				true);
//		config
//				.setFeature(
//						"http://cyberneko.org/html/features/scanner/script/strip-cdata-delims",
//						false);
//
//		config.setProperty("http://cyberneko.org/html/properties/names/elems",
//				"match");
//
//		config.setProperty("http://cyberneko.org/html/properties/names/attrs",
//				"no-change");
//		config.setProperty(
//				"http://cyberneko.org/html/properties/default-encoding", "gbk");
//	}
//
//	public ParseContext createParseContext(URI base) {
//		ParseContext pc = new ParseContextImpl(new ParseConfigImpl(base,null),null);
//		pc.getFeatureMap().putAll(features);
//		pc.addNodeParser(this);
//		pc.setReserveSpace(true);
//		return pc;
//	}
//
//	public boolean doTransform(URL in, Writer out) {
//		try {
//			Document doc = loadXML(in);
//			ParseContext pc = createParseContext(in.toURI());
//			Element root = doc.getDocumentElement();
//			//System.out.println(root.getFirstChild());
//			if (root.getTagName().equals("HTML")) {
//				Node node = root.getFirstChild();
//				while (node != null) {
//					if (node.getNodeName().equals("html")) {
//						root = (Element) node;
//						break;
//					}
//					switch (node.getNodeType()) {
//					case Node.COMMENT_NODE:
//						node = node.getNextSibling();
//						break;
//					default:
//						node = null;
//					}
//				}
//			}
//			root.setAttribute("xmlns:c", "http://www.xidea.org/ns/lite/core");
//			pc.parse(root);
//			String html = (String) pc.toList().get(0);
//			out
//					.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n");
//			out.write(html);
//			return true;
//		} catch (Exception e) {
//			StringWriter out2 = new StringWriter();
//			e.printStackTrace(new PrintWriter(out2, true));
//			try {
//				out.append(out2.toString());
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//			return false;
//		}
//	}
//
//	public Document loadXML(URL url) throws SAXException, IOException {
//		InputSource in = new InputSource(url.openStream());
//		synchronized (htmlParser) {
//			htmlParser.parse(in);
//			Document doc = htmlParser.getDocument();
//			return doc;
//		}
//	}
//
//	public static void main(String[] args) throws IOException {
//		Writer out = new PrintWriter(System.out, true);
//		new HTML2XHTML().doTransform(new URL("http://www.google.com"),
//				out);
//		out.flush();
//	}
//
//	public void parse(Element node, ParseContext context, ParseChain chain) {
//		String name = node.getTagName();
//		name = name.toLowerCase();
//		if (name.equals("script")) {
//			context.append("<" + name);
//			NamedNodeMap ns = node.getAttributes();
//			for (int i = 0; i < ns.getLength(); i++) {
//				context.parse(ns.item(i));
//			}
//			context.parse(">");
//			context.append("<![CDATA[");
//			context.append(node.getTextContent().replace("]]>",
//					"]]>]]<![CDATA[>"));
//			context.append("]]></" + name + ">");
//		} else {
//			chain.next(node);
//		}
//	}
//
//}
