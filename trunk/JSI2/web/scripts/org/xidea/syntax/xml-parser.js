/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: xml-parser.js,v 1.4 2008/02/25 13:51:19 jindw Exp $
 */

/**
 * @public
 * @constructor
 * @param <String> source
 */
function XMLParser(source){
    this.source = source;
}
/**

         (<!\[CDATA\[\s*.*\s*\]\]>)
         | (<!--\s*.*\s*?-->)
         | (<)*(\w+)*\s*(\w+)\s*=\s*(".*?"|'.*?'|\w+)(/*>)*
         | (</?)(.*?)(/?>)
 * partitions Regexp.
 * @protected
 */
 
var notCdataPattern = "[^\\]]|\\][^\\]]|\\]\\][^>]";
var idPattern = '[\\w_][\\w\\d-_\\.]*';
var namePattern = "(?:"+idPattern +"[:])?"+idPattern
var stringPattern = '"(?:\\\\.|[^"\\n\\r])*"|'+"'(?:\\\\.|[^'\\n\\r])*'";

var ltgtPattern = "^<|>$"
var attributePattern = new RegExp("("+namePattern+")(\\s*=\\s*)("+stringPattern+")",'g');
var attributeReplacer = 
"<span class='xidea-syntax-xml-attribute'>$1</span>$2\
<span class='xidea-syntax-xml-string'>$3</span>"
XMLParser.prototype = new SyntaxParser(
    {
          'xml-cdata':'<\\!\\[[\\w]*\\['+notCdataPattern+'\\]\\]>'//cdata
          ,'xml-processor':'<\\?(?:[^\\?]|\\?[^>])*\\?>'//processor
          ,'xml-comment':'<!--(?:[^-]|-[^-])*-->'//comment
          //,'xml-begin':'<'+namePattern+'(?:\\s+'+namePattern+'\\s*=\\s*'+stringPattern+')*\\s*/?>' //begin tag
          ,'xml-begin':'<'+idPattern+'[^<>]*>' //begin tag
          ,'xml-end':'</'+namePattern+'>'                         //end tag
          //,'xml-end2':'</.*>'                         //end tag
    },
    {
        /*
         * keywords Regexp.
         * default is for javascript 
         * @protected
         */
        'xml-begin':function(text){
            return text.replace(attributePattern,attributeReplacer);
        }
    }
);

/**
 * private
 */
XMLParser.prototype.defaultType = "xml-text";

/**
 * 计算XML文件的当前深度.
 * <p>default implements is scan the code for '{'|'}',if '{' <b>++depth</b> else <b>depth--</b></p>
 * @protected
 */
XMLParser.prototype.computeDepth = function(partition,dep){
    switch(partition.type){
        case 'xml-begin':
            this.depths.push([
                partition.begin,
                dep++,
                dep]);
            break;
        case 'xml-end':
            this.depths.push([partition.begin,
                dep--,
                dep]);
            break;
    }
    return dep;
};
/**
 * 猜测分区类型
 */
XMLParser.prototype.guessType = function(p){
    for(var n in this.partitionerMap){
        if(this.partitionerMap[n].test(p)){
            return n;
        }
    }
    return this.defaultType;
};