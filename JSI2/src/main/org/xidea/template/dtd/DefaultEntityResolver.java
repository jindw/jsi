package org.xidea.template.dtd;

import java.io.IOException;
import java.util.HashMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

public class DefaultEntityResolver implements EntityResolver2 {
	private static HashMap<String, String> DEFAULT_DTD_MAP = new HashMap<String, String>();
	static {
		DEFAULT_DTD_MAP.put("-//W3C//DTD XHTML 1.0 Transitional//EN",
				"xhtml1-transitional.dtd");
		DEFAULT_DTD_MAP.put("-//W3C//DTD XHTML 1.0 Strict//EN",
				"xhtml1-strict.dtd");
		DEFAULT_DTD_MAP.put("-//W3C//ENTITIES Latin 1 for XHTML//EN",
				"xhtml-lat1.ent");
		DEFAULT_DTD_MAP.put("-//W3C//ENTITIES Symbols for XHTML//EN",
				"xhtml-symbol.ent");
		DEFAULT_DTD_MAP.put("-//W3C//ENTITIES Special for XHTML//EN",
				"xhtml-special.ent");
	}

	public InputSource resolveEntity(String name, String publicId,
			String baseURI, String systemId) throws SAXException, IOException {
		return resolveEntity(publicId, systemId);
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		String path = DEFAULT_DTD_MAP.get(publicId);
		if (path == null) {
			return null;
		}
		InputSource source = new InputSource(this.getClass()
				.getResourceAsStream(path));
		source.setSystemId(systemId);
		return source;
	}

	public InputSource getExternalSubset(String name, String baseURI)
			throws SAXException, IOException {
		return null;
	}

}
