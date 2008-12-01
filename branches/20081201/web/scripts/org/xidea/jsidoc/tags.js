/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: tags.js,v 1.2 2008/02/19 12:54:00 jindw Exp $
 */


var accessTag = {
    'public':{},
    'protected':{},
    'private':{},
    'internal':{},
    'friend':{}
};


var flagTag = {
    'abstract':{},
    'final':{},
    'static':{},
    'fileoverview':{alias:['filedoc']},
    'deprecated':{},
    'constructor':{alias:['class']}
};


var valueTag = {
    'author':{},
    'access':{},
    'version':{},
    'arguments': {alias:['args']},
    //The type Of the object same as javascript instanceof op
    'instanceof':{},
    //The type Of the object same as javascript typeof op
    'typeof':{},
    //@return <Type> information (type tag is deprecated just to keep the same with some old jsidoc)
    'return':{},
    'returnType':{alias:['type']},
    'name': {},
    'owner': {alias:['member']},
    //Used to show that a class is a subclass of another class. JSIDoc is often quite good at picking this up on its own, but in some situations this tag is required.
    'extend':{alias:['extends']}
}


var valuesTag = {
    'throw':{alias:['exception']},
    'param':{alias:['argument']},
    'see':{}
}
