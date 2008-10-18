package org.xidea.template;

import java.util.regex.Pattern;

import org.w3c.dom.Node;

public interface XMLNodeParser {
	public static final Object[] END = new Object[0];

	public static final Pattern TEMPLATE_NAMESPACE = Pattern.compile("^http://www.xidea.org/ns/template.*",
			Pattern.CASE_INSENSITIVE);

	public boolean parseNode(Node node, ParseContext context);
}
