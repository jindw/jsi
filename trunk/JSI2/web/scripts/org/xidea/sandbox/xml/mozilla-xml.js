/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: mozilla-xml.js,v 1.3 2008/02/19 13:39:03 jindw Exp $
 */

var XSLTProcessor = window.XSLTProcessor;
var XPathEvaluator = window.XPathEvaluator;
var DOMParser = window.DOMParser;
var XMLSerializer = window.XMLSerializer;
var XPathResult = window.XPathResult;

// xpathResultConstants = 
if(!XPathResult){
  XPathResult = function(value,resultType){
    this.resultType = resultType;
    if(resultType > 7){
      this.singleNodeValue = value;
    }else if(resultType > 3){
      this.value = [];
      for(var i = 0;i<value.length;i++){
        this.value.push(value.item(i))
      }
    //}else if(resultType >5){//selectNodes
      this.snapshotLength = value.length;
    }
  }
  XPathResult.prototype = {
    /**
     * This function returns an object that implements the Node interface.
     * This function can raise an object that implements the XPathException interface or the DOMException interface.
     */
    iterateNext:function(){
      this.values[this.index++];
    },
    /**
     * This function returns an object that implements the Node interface.
     * The index parameter is a Number.
     * This function can raise an object that implements the XPathException interface. 
     */
    snapshotItem:function(index){
      this.values[index];
    },
    /*The value of the constant XPathResult.ANY_TYPE is */
    ANY_TYPE:0,
    /*The value of the constant XPathResult.NUMBER_TYPE is */
    NUMBER_TYPE:1,
    /*The value of the constant XPathResult.STRING_TYPE is */
    STRING_TYPE:2,
    /*The value of the constant XPathResult.BOOLEAN_TYPE is */
    BOOLEAN_TYPE:3,
    /*The value of the constant XPathResult.UNORDERED_NODE_ITERATOR_TYPE is */
    UNORDERED_NODE_ITERATOR_TYPE:4,
    /*The value of the constant XPathResult.ORDERED_NODE_ITERATOR_TYPE is */
    ORDERED_NODE_ITERATOR_TYPE:5,
    /*The value of the constant XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE is */
    UNORDERED_NODE_SNAPSHOT_TYPE:6,
    /*The value of the constant XPathResult.ORDERED_NODE_SNAPSHOT_TYPE is */
    ORDERED_NODE_SNAPSHOT_TYPE:7,
    /*The value of the constant XPathResult.ANY_UNORDERED_NODE_TYPE is */
    ANY_UNORDERED_NODE_TYPE:8,
    /*The value of the constant XPathResult.FIRST_ORDERED_NODE_TYPE is */
    FIRST_ORDERED_NODE_TYPE:9
  };
}
if(BrowserInfo.isIE()){
  if(!XSLTProcessor){//IE7-
    var xsltemplateProgid = ["Msxml2.XSLTemplate.6.0", "MSXML2.XSLTemplate.3.0"];
    var freeThreadedDOMDocumentProgid = ["MSXML2.FreeThreadedDOMDocument.6.0", "MSXML2.FreeThreadedDOMDocument.3.0"];
    var xmlWriterProgid = ["Msxml2.MXXMLWriter.6.0", "Msxml2.MXXMLWriter.3.0", "MSXML2.MXXMLWriter", "MSXML.MXXMLWriter", "Microsoft.XMLDOM"];
    var domProgid = ["Msxml2.DOMDocument.6.0", "Msxml2.DOMDocument.3.0", "MSXML2.DOMDocument", "MSXML.DOMDocument", "Microsoft.XMLDOM"];
    function createActiveXObject(progids){
      if(progids instanceof Array){
        for(var i = 0;i<progids.length;i++){
          try{
            var result = new ActiveXObject(progids[i]);
            progids = progids[i];
            return result;
          }catch(e){
          }
        }
      }else{
        return new ActiveXObject(progids);
      }
    }
    /**
     * Basic implementation of Mozilla's XSLTProcessor for IE. 
     * Reuses the same XSLT stylesheet for multiple transforms
     * @constructor
     */
    XSLTProcessor = function(){
        this.template = createActiveXObject(xsltemplateProgid);
    };

    /**
     * 引入 XSLT 样式表 styleSheet 为 XSLT stylesheet 的根结点。 
     * Imports the given XSLT DOM and compiles it to a reusable transform
     * <b>Note:</b> If the stylesheet was loaded from a URL and contains xsl:import or xsl:include elements,it will be reloaded to resolve those
     * @argument xslDoc The XSLT DOMDocument to import
     */
    XSLTProcessor.prototype.importStylesheet = function(xslDoc){
        var converted = createActiveXObject(freeThreadedDOMDocumentProgid);
        xslDoc.setProperty("SelectionLanguage", "XPath");
        xslDoc.setProperty("SelectionNamespaces", "xmlns:xsl='http://www.w3.org/1999/XSL/Transform'");
        // make included/imported stylesheets work if exist and xsl was originally loaded from url
        if(xslDoc.url && xslDoc.selectSingleNode("//xsl:*[local-namespaceURI() = 'import' or local-name() = 'include']") != null){
            converted.async = false;
            if (freeThreadedDOMDocumentProgid == "MSXML2.FreeThreadedDOMDocument.6.0") { 
                converted.setProperty("AllowDocumentFunction", true); 
                converted.resolveExternals = true; 
            }
            converted.load(xslDoc.url);
        } else {
            converted.loadXML(xslDoc.xml);
        }
        converted.setProperty("SelectionNamespaces", "xmlns:xsl='http://www.w3.org/1999/XSL/Transform'");
        var output = converted.selectSingleNode("//xsl:output");
        this.outputMethod = output ? output.getAttribute("method") : "html";
        this.template.stylesheet = converted;
        this.processor = this.template.createProcessor();
        // for getParameter and clearParameters
        this.paramsSet = new Array();
    };

    /**
     * 使用由importStylesheet()引入的样式表对结点source进行转换. 
     * Transform the given XML DOM and return the transformation result as a new DOM document
     * @argument sourceDoc The XML DOMDocument to transform
     * @return The transformation result as a DOM Document
     */
    XSLTProcessor.prototype.transformToDocument = function(sourceDoc){
      // fix for bug 1549749
      this.processor.input = sourceDoc;
      this.processor.transform();
      var oDoc = createActiveXObject(domProgid);
      oDoc.loadXML(this.processor.output||"");
      return oDoc;
    };
    /**
     * 使用由importStylesheet()引入的样式表对结点source进行转换，owner 是转换结果的 DOMDocument. 
     * Transform the given XML DOM and return the transformation result as a new DOM fragment.
     * <b>Note</b>: The xsl:output method must match the nature of the owner document (XML/HTML).
     * @argument sourceDoc The XML DOMDocument to transform
     * @argument ownerDoc The owner of the result fragment
     * @return The transformation result as a DOM Document
     */
    XSLTProcessor.prototype.transformToFragment = function (sourceDoc, ownerDoc) {
      this.processor.input = sourceDoc;
      this.processor.transform();
      var s = this.processor.output;
      var f = ownerDoc.createDocumentFragment();
      if (this.outputMethod == 'text') {
        f.appendChild(ownerDoc.createTextNode(s));
      } else if (ownerDoc.body && ownerDoc.body.innerHTML) {
        var container = ownerDoc.createElement('div');
        container.innerHTML = s;
        while (container.hasChildNodes()) {
          f.appendChild(container.firstChild);
        }
      }
      else {
        var oDoc = new ActiveXObject(domProgid);
        if (s.substring(0, 5) == '<?xml') {
          s = s.substring(s.indexOf('?>') + 2);
        }
        var xml = ''.concat('<my>', s, '</my>');
        oDoc.loadXML(xml);
        var container = oDoc.documentElement;
        while (container.hasChildNodes()) {
          f.appendChild(container.firstChild);
        }
      }
      return f;
    };

    /**
     * 设置 XSLT stylesheet 的参数。 
     * Set global XSLT parameter of the imported stylesheet
     * @argument namespaceURI The parameter namespace URI
     * @argument localName The parameter base localName
     * @argument value The new parameter value
     */
    XSLTProcessor.prototype.setParameter = function(namespaceURI, localName, value){
      // make value a zero length string if null to allow clearing
      value = value || "";
      namespaceURI = namespaceURI || "";
      if(namespaceURI){
        this.processor.addParameter(localName, value, namespaceURI);
      }else{
        this.processor.addParameter(localName, value);
      };
      // update updated params for getParameter 
      if(!this.paramsSet[namespaceURI]){
        this.paramsSet[namespaceURI] = {};
      }
      this.paramsSet[namespaceURI][localName] = value;
    };
    /**
     * 取得 XSLT stylesheet 的参数的值。 
     * Gets a parameter if previously set by setParameter. Returns null
     * otherwise
     * @argument localName The parameter base name
     * @argument value The new parameter value
     * @return The parameter value if reviously set by setParameter, null otherwise
     */
    XSLTProcessor.prototype.getParameter = function(namespaceURI, localName){
        namespaceURI = namespaceURI || "";
        if(this.paramsSet[namespaceURI]){
            return this.paramsSet[namespaceURI][localName];
        }else{
            return null;
        }
    };
    /**
     * 去除 XSLT stylesheet 中所有参数的值，这将导致 XSLT 使用默认的参数值。 
     * Clear parameters (set them to default values as defined in the stylesheet itself)
     */
    XSLTProcessor.prototype.clearParameters = function(){
      for(var namespaceURI in processor.paramsSet){
        for(var localName in processor.paramsSet[namespaceURI]){
          if(namespaceURI){
            this.processor.addParameter(localName, '', namespaceURI);
          }else{
            this.processor.addParameter(localName, '');
          };
        };
      };
      this.paramsSet = new Array();
    };
    /**
     * 去除 XSLT stylesheet 指定参数的值，这将导致 XSLT 使用默认的参数值。 
     */
    XSLTProcessor.prototype.removeParameter = function(namespaceURI, localName){
      var params = this.paramsSet[namespaceURI||''];
      if(params[localName]){
        delete params[localName];
        if(namespaceURI){
          this.processor.addParameter(localName, '', nsURI);
        }else{
          this.processor.addParameter(localName, '');
        };
      }
      
    }
    /**
     * 从 XSLTProcessor 中去除所有样式表和参数。
     */
    XSLTProcessor.prototype.reset = function(){
      //this.template ;
      this.processor = null;
      this.paramsSet = null;
    }
  }
    

  if(!DOMParser){
    DOMParser = function() { };
    DOMParser.parseFromString = DOMParser.prototype.parseFromString = function(sXml, contentType){
      var doc = createActiveXObject(domProgid);
      doc.loadXML(sXml);
      return doc;
    };
  }
  if(!XMLSerializer){
    /**
     * Utility class to serialize DOM Node objects to XML strings
     * @constructor
     */
    XMLSerializer = function(){};
    /**
     * Serialize the given DOM Node to an XML string
     * @param oNode the DOM Node to serialize
     */
    XMLSerializer.prototype.serializeToString = function(oNode) {
        return oNode.xml;
    };
  }
  if(!XPathEvaluator){
    XPathEvaluator = function(){}
    XPathEvaluator.prototype.evaluate = function(expression, contextNode, resolver, type, result){
      if(type>7){//ANY_UNORDERED_NODE_TYPE:8,FIRST_ORDERED_NODE_TYPE:9
        var value = contextNode.selectSingleNode(expression);
      }else{
         var value = contextNode.selectNodes(expression);
      }
      return new XPathResult(value,type);
    }
    XPathEvaluator.selectSingleNode = function(contextNode,expression){
      return contextNode.selectSingleNode(expression);
    };
    XPathEvaluator.selectNodes = function(contextNode,expression){
      return contextNode.selectNodes(expression);
    };
  }
}//end ie
else{
  if(XPathEvaluator){
    XPathEvaluator.selectSingleNode = function(contextNode,expression){
      var evaluator = new XPathEvaluator();
      var result = evaluator.evaluate(expression,contextNode,null,9,null);
      return result?result.singleNodeValue:null;
    };
    XPathEvaluator.selectNodes = function(contextNode,expression){
      //ORDERED_NODE_ITERATOR_TYPE:5,
      var evaluator = new XPathEvaluator();
      var result = evaluator.evaluate(expression,contextNode,null,5,null);
      var nodeList = [];
      var item;
      while(item = result.iterateNext()){
        nodeList.push(item);
      }
      nodeList.item = function(index){
        return this[index];
      }
      return nodeList;
    };
  }else if(document.selectSingleNode){
    XPathEvaluator = function(expression, contextNode, resolver, type, result){
      if(type>7){//ANY_UNORDERED_NODE_TYPE:8,FIRST_ORDERED_NODE_TYPE:9
        var value = XPathEvaluator.selectSingleNode(contextNode,expression);
      }else{
        var value = XPathEvaluator.selectNodes(contextNode,expression);
      }
      return new XPathResult(value,type);
    };
    XPathEvaluator.selectSingleNode = function(contextNode,expression){
      var doc = contextNode.ownerDocument;
      if(contextNode.selectSingleNode){
        return contextNode.selectSingleNode(expression);
      }else if(doc.selectSingleNode){
        return doc.selectSingleNode(expression, contextNode);
      }
    };
    XPathEvaluator.selectNodes = function(contextNode,expression){
      var doc = contextNode.ownerDocument;
      if(contextNode.selectNodes){
        return contextNode.selectNodes(expression);
      }else if(doc.selectNodes){
        return doc.selectNodes(expression, contextNode);
      }
    };
  }
  if(!DOMParser){
    /**
     * DOMParser is a utility class, used to construct DOMDocuments from XML strings
     * @constructor
     */
    if(BrowserInfo.isKhtml()||BrowserInfo.isOpera(7.2)){
      DOMParser = function() { };
      /** 
       * Construct a new DOM Document from the given XMLstring
       * @param sXml the given XML string
       * @param contentType the content type of the document the given string represents (one of text/xml, application/xml, application/xhtml+xml). 
       * @return a new DOM Document from the given XML string
       */
      DOMParser.prototype.parseFromString = function(sXml, contentType){
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("GET", "data:text/xml;charset=utf-8," + encodeURIComponent(sXml), false);
        xmlhttp.send(null);
        return xmlhttp.responseXML;
      };
    }
  }
}

