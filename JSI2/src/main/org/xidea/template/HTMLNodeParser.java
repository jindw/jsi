package org.xidea.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HTMLNodeParser implements XMLNodeParser {
	public static final Pattern HTML_LEAF = Pattern.compile(
			"^(?:meta|link|img|br|hr|input)$", Pattern.CASE_INSENSITIVE);

	private static final String XHTMLNS = "http://www.w3.org/1999/xhtml";
	private static final String EL_INPUT = "input";

	private static final Map<String, String> BOOLEAN_ATTBUTE_MAP = new HashMap<String, String>();
	static {
		BOOLEAN_ATTBUTE_MAP.put("checked", "checked");
		BOOLEAN_ATTBUTE_MAP.put("selected", "selected");
		BOOLEAN_ATTBUTE_MAP.put("disabled", "disabled");

	}

	private static final String ATTRIBUTE_SELECTED = "selected";
	private static final String ATTRIBUTE_CHECKED = "checked";

	private static final String ATTRIBUTE_TYPE = "type";
	private static final String TYPE_CHECKBOX = "checkbox";
	private static final String TYPE_RADIO = "radio";

	private static final String EL_SELECT = "select";
	private static final String EL_OPTION = "option";

	private static final Object SELECT_KEY = new Object();

	private static final String NAME = "name";
	private static final String VALUE = "value";

	private XMLParser parser;

	public HTMLNodeParser(XMLParser parser) {
		this.parser = parser;
	}

	public boolean parseNode(Node node, ParseContext context) {
		String namespace = node.getNamespaceURI();
		if (namespace == null || XHTMLNS.equals(namespace)) {
			if (node instanceof Element) {
				Element el = (Element) node;
				String localName = el.getLocalName();
				if (EL_INPUT.equals(localName)) {
					return parseCloneBooleanInput(el, context);
				} else if (EL_SELECT.equals(localName)) {
					context.put(SELECT_KEY, el);
					this.parser.parseNode(el, context);
					context.remove(SELECT_KEY);
					return true;
				} else if (EL_OPTION.equals(localName)) {
					return parseCloneBooleanInput(el, context);
				} else {
					return parseElement(node, context, null);
				}
			}
		}
		return false;
	}

	private boolean parseCloneBooleanInput(Element element, ParseContext context) {
		element = (Element) element.cloneNode(false);
		String type = element.getAttribute(ATTRIBUTE_TYPE);
		if (EL_OPTION.equals(element.getLocalName())) {
			Element selectNode = (Element) context.get(SELECT_KEY);
			if (!element.hasAttribute(ATTRIBUTE_SELECTED)) {
				if (selectNode.hasAttribute(NAME)
						&& selectNode.hasAttribute(VALUE)) {
					String name = selectNode.getAttribute(NAME);
					String value = selectNode.getAttribute(VALUE);
					final Expression valueEL;
					if (value.startsWith("${") && value.endsWith("}")) {
						value = value.substring(2, value.length() - 1);
						valueEL = this.parser.parseEL(value);
					} else if (value != null) {
						valueEL = new ConstantExpression(value);
					} else {
						valueEL = this.parser.parseEL(name);
					}
					List<Object> attributes = new ArrayList<Object>();
					final Expression collectionEL = this.parser.parseEL(name);
					attributes
							.add(new Object[] {
									Template.IF_TYPE,
									new ContainsStringExpression(collectionEL,
											valueEL) });
					attributes.add(" " + ATTRIBUTE_SELECTED + "=\""
							+ BOOLEAN_ATTBUTE_MAP.get(ATTRIBUTE_SELECTED)
							+ "\"");
					attributes.add(END);

					return parseElement(element, context, attributes);
				}
			}
		} else if (TYPE_CHECKBOX.equals(type) || TYPE_RADIO.equals(type)) {
			if (element.hasAttribute(ATTRIBUTE_CHECKED)) {
				return parseElement(element, context, null);
			} else if (element.hasAttribute(NAME)
					&& element.hasAttribute(VALUE)) {
				String name = element.getAttribute(NAME);
				String value = element.getAttribute(VALUE);
				final Expression valueEL;
				if (value.startsWith("${") && value.endsWith("}")) {
					value = value.substring(2, value.length() - 1);
					valueEL = this.parser.parseEL(value);
				} else {
					valueEL = new ConstantExpression(value);
				}
				List<Object> attributes = new ArrayList<Object>();

				final Expression collectionEL = this.parser.parseEL(name);

				attributes.add(new Object[] { Template.IF_TYPE,
						new ContainsStringExpression(collectionEL, valueEL) });
				attributes.add(" " + ATTRIBUTE_CHECKED + "=\""
						+ BOOLEAN_ATTBUTE_MAP.get(ATTRIBUTE_CHECKED) + "\"");
				attributes.add(END);
				element.removeAttribute(ATTRIBUTE_CHECKED);

				return parseElement(element, context, attributes);
			}
		} else {
			if (!element.hasAttribute(VALUE) && element.hasAttribute(NAME)) {
				element.setAttribute(VALUE, "${" + element.getAttribute(NAME)
						+ "}");
				return parseElement(element, context, null);
			}
		}
		return parseElement(element, context, null);
	}

	private void appendBooleanAttribute(Attr node, ParseContext context) {
		String name = node.getName();
		String value = node.getValue();
		String trueValue = BOOLEAN_ATTBUTE_MAP.get(name);
		if (trueValue != null) {
			value = value.trim();
			if (value.length() == 0 || "false".equals(value)) {
				return;
			} else {
				trueValue = " " + name + "=\"" + trueValue + "\"";
				if (value.startsWith("${") && value.endsWith("}")) {
					value = value.substring(2, value.length() - 1);
					final Expression el = this.parser.parseEL(value);
					context.append(new Object[] { Template.IF_TYPE, el });
					context.append(trueValue);
					context.append(END);
				} else {
					context.append(trueValue);
				}
				return;
			}
		}
		this.parser.parseNode(node, context);

	}

	private boolean parseElement(Node node, ParseContext context,
			List<Object> exts) {
		Element el = (Element) node;
		NamedNodeMap attributes = node.getAttributes();
		String tagName = el.getTagName();
		context.append("<" + tagName);
		for (int i = 0; i < attributes.getLength(); i++) {
			appendBooleanAttribute((Attr) attributes.item(i), context);
			// this.parser.parseNode(attributes.item(i), context);
		}
		if (exts != null) {
			for (Object item : exts) {
				context.append(item);
			}
		}
		if (HTML_LEAF.matcher(tagName).find()) {
			context.append("/>");
			return true;
		}
		context.append('>');
		Node next = node.getFirstChild();
		if (next != null) {
			do {
				this.parser.parseNode(next, context);
			} while ((next = next.getNextSibling()) != null);
		}
		context.append("</" + tagName + '>');
		return true;
	}
}
