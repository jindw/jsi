/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: node-type.js,v 1.3 2008/02/19 13:39:03 jindw Exp $
 */

/*
 * 节点类型参考，当文档看待吧，使用时直接用常量更快。
 */
//$m style
var NODE_ELEMENT                =1 ;
var NODE_ATTRIBUTE              =2 ;
var NODE_TEXT                   =3 ;
var NODE_CDATA_SECTION          =4 ;
var NODE_ENTITY_REFERENCE       =5 ;
var NODE_ENTITY                 =6 ;
var NODE_PROCESSING_INSTRUCTION =7 ;
var NODE_COMMENT                =8 ;
var NODE_DOCUMENT               =9 ;
var NODE_DOCUMENT_TYPE          =10;
var NODE_DOCUMENT_FRAGMENT      =11;
var NODE_NOTATION               =12;
/**
 * 节点类型参考
 */
var NodeType = {
  ELEMENT                       :NODE_ELEMENT                ,
  ATTRIBUTE                     :NODE_ATTRIBUTE              ,
  TEXT                          :NODE_TEXT                   ,
  CDATA_SECTION                 :NODE_CDATA_SECTION          ,
  ENTITY_REFERENCE              :NODE_ENTITY_REFERENCE       ,
  ENTITY                        :NODE_ENTITY                 ,
  PROCESSING_INSTRUCTION        :NODE_PROCESSING_INSTRUCTION ,
  COMMENT                       :NODE_COMMENT                ,
  DOCUMENT                      :NODE_DOCUMENT               ,
  DOCUMENT_TYPE                 :NODE_DOCUMENT_TYPE          ,
  DOCUMENT_FRAGMENT             :NODE_DOCUMENT_FRAGMENT      ,
  NOTATION                      :NODE_NOTATION               
}

/*
1 //NODE_ELEMENT                 
2 //NODE_ATTRIBUTE               
3 //NODE_TEXT                    
4 //NODE_CDATA_SECTION           
5 //NODE_ENTITY_REFERENCE        
6 //NODE_ENTITY                  
7 //NODE_PROCESSING_INSTRUCTION  
8 //NODE_COMMENT                 
9 //NODE_DOCUMENT                
10//NODE_DOCUMENT_TYPE           
11//NODE_DOCUMENT_FRAGMENT       
12//NODE_NOTATION                
*/