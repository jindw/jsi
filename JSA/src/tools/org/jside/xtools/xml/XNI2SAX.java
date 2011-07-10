//package org.jside.xtools.xml;
//
//import org.apache.xerces.xni.Augmentations;
//import org.apache.xerces.xni.NamespaceContext;
//import org.apache.xerces.xni.QName;
//import org.apache.xerces.xni.XMLAttributes;
//import org.apache.xerces.xni.XMLDTDContentModelHandler;
//import org.apache.xerces.xni.XMLDTDHandler;
//import org.apache.xerces.xni.XMLDocumentHandler;
//import org.apache.xerces.xni.XMLLocator;
//import org.apache.xerces.xni.XMLResourceIdentifier;
//import org.apache.xerces.xni.XMLString;
//import org.apache.xerces.xni.XNIException;
//import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
//import org.apache.xerces.xni.parser.XMLDTDSource;
//import org.apache.xerces.xni.parser.XMLDocumentSource;
//import org.xml.sax.AttributeList;
//import org.xml.sax.ContentHandler;
//import org.xml.sax.SAXException;
//import org.xml.sax.ext.Attributes2;
//import org.xml.sax.ext.Locator2;
//
//import com.sun.org.apache.xerces.internal.impl.Constants;
//import org.apache.xerces.util.XMLSymbols;
//
///**
// * Converts {@link XNI} events to {@link ContentHandler} events.
// * 
// * <p>
// * Deriving from {@link DefaultXMLDocumentHandler} to reuse its default
// * {@link org.apache.xerces.xni.XMLDocumentHandler} implementation.
// * 
// * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
// */
//public class XNI2SAX implements XMLDocumentHandler, XMLDTDHandler,
//XMLDTDContentModelHandler{
//
//	private ContentHandler fContentHandler;
//
//	private String fVersion;
//
//	/** Namespace context */
//	protected NamespaceContext fNamespaceContext;
//
//	/**
//	 * For efficiency, we reuse one instance.
//	 */
//	private final AttributesProxy fAttributesProxy = new AttributesProxy();
//
//	public void setContentHandler(ContentHandler handler) {
//		this.fContentHandler = handler;
//	}
//
//	public ContentHandler getContentHandler() {
//		return fContentHandler;
//	}
//
//	public void xmlDecl(String version, String encoding, String standalone,
//			Augmentations augs) throws XNIException {
//		this.fVersion = version;
//	}
//
//	public void startDocument(XMLLocator locator, String encoding,
//			NamespaceContext namespaceContext, Augmentations augs)
//			throws XNIException {
//		fNamespaceContext = namespaceContext;
//		fContentHandler.setDocumentLocator(new LocatorProxy(locator));
//		try {
//			fContentHandler.startDocument();
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void endDocument(Augmentations augs) throws XNIException {
//		try {
//			fContentHandler.endDocument();
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void processingInstruction(String target, XMLString data,
//			Augmentations augs) throws XNIException {
//		try {
//			fContentHandler.processingInstruction(target, data.toString());
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void startElement(QName element, XMLAttributes attributes,
//			Augmentations augs) throws XNIException {
//		try {
//			// start namespace prefix mappings
//			int count = fNamespaceContext.getDeclaredPrefixCount();
//			if (count > 0) {
//				String prefix = null;
//				String uri = null;
//				for (int i = 0; i < count; i++) {
//					prefix = fNamespaceContext.getDeclaredPrefixAt(i);
//					uri = fNamespaceContext.getURI(prefix);
//					fContentHandler.startPrefixMapping(prefix,
//							(uri == null) ? "" : uri);
//				}
//			}
//
//			String uri = element.uri != null ? element.uri : "";
//			String localpart = element.localpart;
//			fAttributesProxy.setAttributes(attributes);
//			fContentHandler.startElement(uri, localpart, element.rawname,
//					fAttributesProxy);
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void endElement(QName element, Augmentations augs)
//			throws XNIException {
//		try {
//			String uri = element.uri != null ? element.uri : "";
//			String localpart = element.localpart;
//			fContentHandler.endElement(uri, localpart, element.rawname);
//
//			// send endPrefixMapping events
//			int count = fNamespaceContext.getDeclaredPrefixCount();
//			if (count > 0) {
//				for (int i = 0; i < count; i++) {
//					fContentHandler.endPrefixMapping(fNamespaceContext
//							.getDeclaredPrefixAt(i));
//				}
//			}
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void emptyElement(QName element, XMLAttributes attributes,
//			Augmentations augs) throws XNIException {
//		startElement(element, attributes, augs);
//		endElement(element, augs);
//	}
//
//	public void characters(XMLString text, Augmentations augs)
//			throws XNIException {
//		try {
//			fContentHandler.characters(text.ch, text.offset, text.length);
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	public void ignorableWhitespace(XMLString text, Augmentations augs)
//			throws XNIException {
//		try {
//			fContentHandler.ignorableWhitespace(text.ch, text.offset,
//					text.length);
//		} catch (SAXException e) {
//			throw new XNIException(e);
//		}
//	}
//
//	static class LocatorProxy implements Locator2 {
//
//		//
//		// Data
//		//
//
//		/** XML locator. */
//		private final XMLLocator fLocator;
//
//		//
//		// Constructors
//		//
//
//		/** Constructs an XML locator proxy. */
//		public LocatorProxy(XMLLocator locator) {
//			fLocator = locator;
//		}
//
//		//
//		// Locator methods
//		//
//
//		/** Public identifier. */
//		public String getPublicId() {
//			return fLocator.getPublicId();
//		}
//
//		/** System identifier. */
//		public String getSystemId() {
//			return fLocator.getExpandedSystemId();
//		}
//
//		/** Line number. */
//		public int getLineNumber() {
//			return fLocator.getLineNumber();
//		}
//
//		/** Column number. */
//		public int getColumnNumber() {
//			return fLocator.getColumnNumber();
//		}
//
//		//
//		// Locator2 methods
//		//
//
//		public String getXMLVersion() {
//			return fLocator.getXMLVersion();
//		}
//
//		public String getEncoding() {
//			return fLocator.getEncoding();
//		}
//
//	}
//
//	static final class AttributesProxy implements AttributeList, Attributes2 {
//
//		//
//		// Data
//		//
//
//		/** XML attributes. */
//		private XMLAttributes fAttributes;
//
//		//
//		// Constructors
//		//
//
//		public AttributesProxy() {
//			//fAttributes = attributes;
//		}
//
//		//
//		// Public methods
//		//
//
//		/** Sets the XML attributes to be wrapped. */
//		public void setAttributes(XMLAttributes attributes) {
//			fAttributes = attributes;
//		} // setAttributes(XMLAttributes)
//
//		public XMLAttributes getAttributes() {
//			return fAttributes;
//		}
//
//		/*
//		 * Attributes methods
//		 */
//
//		public int getLength() {
//			return fAttributes.getLength();
//		}
//
//		public String getQName(int index) {
//			return fAttributes.getQName(index);
//		}
//
//		public String getURI(int index) {
//			// This hides the fact that internally we use null instead of empty
//			// string
//			// SAX requires the URI to be a string or an empty string
//			String uri = fAttributes.getURI(index);
//			return uri != null ? uri : XMLSymbols.EMPTY_STRING;
//		}
//
//		public String getLocalName(int index) {
//			return fAttributes.getLocalName(index);
//		}
//
//		public String getType(int i) {
//			return fAttributes.getType(i);
//		}
//
//		public String getType(String name) {
//			return fAttributes.getType(name);
//		}
//
//		public String getType(String uri, String localName) {
//			return uri.equals(XMLSymbols.EMPTY_STRING) ? fAttributes.getType(
//					null, localName) : fAttributes.getType(uri, localName);
//		}
//
//		public String getValue(int i) {
//			return fAttributes.getValue(i);
//		}
//
//		public String getValue(String name) {
//			return fAttributes.getValue(name);
//		}
//
//		public String getValue(String uri, String localName) {
//			return uri.equals(XMLSymbols.EMPTY_STRING) ? fAttributes.getValue(
//					null, localName) : fAttributes.getValue(uri, localName);
//		}
//
//		public int getIndex(String qName) {
//			return fAttributes.getIndex(qName);
//		}
//
//		public int getIndex(String uri, String localPart) {
//			return uri.equals(XMLSymbols.EMPTY_STRING) ? fAttributes.getIndex(
//					null, localPart) : fAttributes.getIndex(uri, localPart);
//		}
//
//		/*
//		 * Attributes2 methods
//		 */
//
//		public boolean isDeclared(int index) {
//			if (index < 0 || index >= fAttributes.getLength()) {
//				throw new ArrayIndexOutOfBoundsException(index);
//			}
//			return Boolean.TRUE.equals(fAttributes.getAugmentations(index)
//					.getItem(Constants.ATTRIBUTE_DECLARED));
//		}
//
//		public boolean isDeclared(String qName) {
//			int index = getIndex(qName);
//			if (index == -1) {
//				throw new IllegalArgumentException(qName);
//			}
//			return Boolean.TRUE.equals(fAttributes.getAugmentations(index)
//					.getItem(Constants.ATTRIBUTE_DECLARED));
//		}
//
//		public boolean isDeclared(String uri, String localName) {
//			int index = getIndex(uri, localName);
//			if (index == -1) {
//				throw new IllegalArgumentException(localName);
//			}
//			return Boolean.TRUE.equals(fAttributes.getAugmentations(index)
//					.getItem(Constants.ATTRIBUTE_DECLARED));
//		}
//
//		public boolean isSpecified(int index) {
//			if (index < 0 || index >= fAttributes.getLength()) {
//				throw new ArrayIndexOutOfBoundsException(index);
//			}
//			return fAttributes.isSpecified(index);
//		}
//
//		public boolean isSpecified(String qName) {
//			int index = getIndex(qName);
//			if (index == -1) {
//				throw new IllegalArgumentException(qName);
//			}
//			return fAttributes.isSpecified(index);
//		}
//
//		public boolean isSpecified(String uri, String localName) {
//			int index = getIndex(uri, localName);
//			if (index == -1) {
//				throw new IllegalArgumentException(localName);
//			}
//			return fAttributes.isSpecified(index);
//		}
//
//		/*
//		 * AttributeList methods
//		 */
//
//		public String getName(int i) {
//			return fAttributes.getQName(i);
//		}
//
//	}
//
//
//
//		private XMLDocumentSource fDocumentSource;
//
//		/** Sets the document source. */
//		public void setDocumentSource(XMLDocumentSource source) {
//			fDocumentSource = source;
//		}
//
//		/** Returns the document source. */
//		public XMLDocumentSource getDocumentSource() {
//			return fDocumentSource;
//		}
//
//		private XMLDTDSource fDTDSource;
//
//		// set the source of this handler
//		public void setDTDSource(XMLDTDSource source) {
//			fDTDSource = source;
//		}
//
//		// return the source from which this handler derives its events
//		public XMLDTDSource getDTDSource() {
//			return fDTDSource;
//		}
//
//		private XMLDTDContentModelSource fCMSource;
//
//		// set content model source
//		public void setDTDContentModelSource(XMLDTDContentModelSource source) {
//			fCMSource = source;
//		}
//
//		// get content model source
//		public XMLDTDContentModelSource getDTDContentModelSource() {
//			return fCMSource;
//		}
//
//		public void comment(XMLString arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void doctypeDecl(String arg0, String arg1, String arg2,
//				Augmentations arg3) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endCDATA(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endGeneralEntity(String arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startCDATA(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startGeneralEntity(String arg0, XMLResourceIdentifier arg1,
//				String arg2, Augmentations arg3) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void textDecl(String arg0, String arg1, Augmentations arg2)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void attributeDecl(String arg0, String arg1, String arg2,
//				String[] arg3, String arg4, XMLString arg5, XMLString arg6,
//				Augmentations arg7) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void elementDecl(String arg0, String arg1, Augmentations arg2)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endAttlist(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endConditional(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endDTD(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endExternalSubset(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endParameterEntity(String arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void externalEntityDecl(String arg0, XMLResourceIdentifier arg1,
//				Augmentations arg2) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void ignoredCharacters(XMLString arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void internalEntityDecl(String arg0, XMLString arg1,
//				XMLString arg2, Augmentations arg3) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void notationDecl(String arg0, XMLResourceIdentifier arg1,
//				Augmentations arg2) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startAttlist(String arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startConditional(short arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startDTD(XMLLocator arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startExternalSubset(XMLResourceIdentifier arg0,
//				Augmentations arg1) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startParameterEntity(String arg0,
//				XMLResourceIdentifier arg1, String arg2, Augmentations arg3)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void unparsedEntityDecl(String arg0, XMLResourceIdentifier arg1,
//				String arg2, Augmentations arg3) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void any(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void element(String arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void empty(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endContentModel(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void endGroup(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void occurrence(short arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void pcdata(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void separator(short arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startContentModel(String arg0, Augmentations arg1)
//				throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void startGroup(Augmentations arg0) throws XNIException {
//			// TODO Auto-generated method stub
//			
//		}
//}
