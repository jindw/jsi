package org.xidea.template.parser;

import org.w3c.dom.Node;

public interface NodeParser {
	public static final Object[] END = new Object[0];
	public static final String TEMPLATE_NAMESPACE = "http://www.xidea.org/ns/template";

	public boolean parseNode(Node node, ParseContext context);
}
