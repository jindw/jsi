//,"NODE_ELEMENT","NODE_ATTRIBUTE","NODE_TEXT","NODE_CDATA_SECTION","NODE_ENTITY_REFERENCE","NODE_ENTITY","NODE_PROCESSING_INSTRUCTION","NODE_COMMENT","NODE_DOCUMENT","NODE_DOCUMENT_TYPE","NODE_DOCUMENT_FRAGMENT","NODE_NOTATION"]);
this.addScript("node-type.js",["NodeType"]);

this.addScript("mozilla-xml.js",['DOMParser','XMLSerializer','XSLTProcessor','XPathEvaluator','XPathResult'],
                "org.xidea.sandbox.util.BrowserInfo");

this.addScript("template.js",['Template','OutputContext'],
                null, 
                ["org.xidea.sandbox.io.Request","tag.js"]);

this.addScript("tag.js",['DefaultTag','AbstractTag','XHTMLTag','CoreTag']);    