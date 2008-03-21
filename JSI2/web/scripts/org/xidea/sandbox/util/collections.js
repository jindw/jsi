/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: collections.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */


/**
 * 可以将各种数据类型适配为统一的Iterator，用于迭代操作
 * @public
 * @constructor
 * @arguments <Iterator|Enumerator|Array|(Object,Object....)>
 */
function Iterator(){
  if(arguments.length == 1){
    var obj = arguments[0];
    if(obj instanceof Iterator){
      return obj;
    }else if(obj instanceof Array){
      this.values = obj;
    }else{
      return new IteratorAdapter(obj);
    }
  }else{
    this.values = arguments;
  }
  this.index = 0;
}
Iterator.prototype.hasNext = function(){
  return this.index<this.values.length;
}
Iterator.prototype.next = function(){
  return this.values[this.index++];
}

function IteratorAdapter(obj){
  this.base = obj;
  if(typeof obj.next == 'function'){
    this.next = this.$next;
    this.hasNext = this.$hasNext;
  }else if(obj.hasMoreElements){
    this.next = this.$nextElement;
    this.hasNext = this.$hasMoreElements;
  }else if(obj.moveNext && obj.item){
    this.next = this.$item;
    this.hasNext = this.$notEnd;
  }else if(obj instanceof Object){
    var arr = [];
    for(var n in obj){
      arr.push({key:n,value:obj[n]});
    }
    return new Iterator(arr);
  }else{
    throw new Error("unknow type");
  }
  
  if(!this.hasNext){
    if(this.next){
      this.value = this.next();
      this.next = this.$next$next;
      this.hasNext = this.$next$hasNext;
    }else{
      throw new Error("can not adapter:"+obj);
    }
  }
}
IteratorAdapter.prototype.$next$next = function(){
  try{
    return this.value;
  }finally{
    return this.value = this.$next$();
  }
}
IteratorAdapter.prototype.$next$hasNext = function(){
  return this.value!=null;
}
/**
 * Java Iterator style next
 */
IteratorAdapter.prototype.$next = function(){
  return this.base.next();
}
/**
 * Java Iterator style hasNext
 */
IteratorAdapter.prototype.$hasNext = function(){
  return this.base.hasNext();
}
/**
 * Java Enumeration style hasNext
 */
IteratorAdapter.prototype.$hasMoreElements = function(){
  return this.base.hasMoreElements();
}
/**
 * Java Enumeration style next
 */
IteratorAdapter.prototype.$nextElement = function(){
  return this.base.nextElement();
}
/**
 * JScript Enumerator style next
 */
IteratorAdapter.prototype.$item = function(){
  try{
    return this.base.item();
  }finally{
    this.base.moveNext();
  }
}
/**
 * JScript Enumerator style hasNext
 */
IteratorAdapter.prototype.$notEnd = function(){
  return !this.base.atEnd();
}












/**
 * 栈表
 * @public 
 * @constructor
 */
function StackMap(p){
  if(StackMap.prototype == null){
    StackMap.prototype = p;
    try{
      return new StackMap(p);
    }finally{
      StackMap.prototype = null;
    }
  }else{
    return StackMap.create(p);
  }
}
StackMap.prototype = null;
StackMap.tryFirst = function(){};
StackMap.create = function(p){
  function tc(){};
  tc.prototype = p;
  return new tc();
};
