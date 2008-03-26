/*
 * Syntax Parser 语法解析相关
 */
this.addScript("syntax-parser.js",['SyntaxParser','ECMAParser','LineIterator']);
this.addScript("xml-parser.js",['XMLParser']);
//this.addScript("syntax-parser.js",'JSParser','ECMAParser');
this.addDependence("*", "SyntaxParser");
