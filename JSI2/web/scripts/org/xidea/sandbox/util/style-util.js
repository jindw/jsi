/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: style-util.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */


/**
 * 页面显示方面的功能函数集，目前还不成熟，现在放js包里，只是觉得这个东西比较常用。
 * @public
 */
var StyleUtil = {

  /**
   * 设置透明度
   * @public
   * @member StyleUtil
   * @static
   */
  setOpacity:function(node,opacity){
    if(BrowserInfo.isIE()){
      if(node.nodeName.toLowerCase() == "tr"){
        // FIXME: is this too naive? will we get more than we want?
        var tds = node.getElementsByTagName("td");
        for(var x=0; x<tds.length; x++){
          tds[x].style.filter = "Alpha(Opacity="+opacity*100+")";
        }
      }
      node.style.filter = "Alpha(Opacity="+opacity*100+")";
    }else if(BrowserInfo.isMozilla()){
      node.style.opacity = opacity; // ffox 1.0 directly supports "opacity"
      node.style.MozOpacity = opacity;
    }else if(BrowserInfo.isSafari()){
      node.style.opacity = opacity; // 1.3 directly supports "opacity"
      node.style.KhtmlOpacity = opacity;
    }else{
      node.style.opacity = opacity;
    }
  },
  /**
   * 获取元素当前透明度
   * @public
   * @member StyleUtil
   * @static
   */
  getOpacity: function(node){
    if(BrowserInfo.isIE()){
        var opac = (node.filters && node.filters.alpha &&
            typeof node.filters.alpha.opacity == "number"
            ? node.filters.alpha.opacity : 100) / 100;
    }else{
      var opac = node.style.opacity || node.style.MozOpacity ||
        node.style.KhtmlOpacity || 1;
    }
    return opac >= 0.999999 ? 1.0 : opac;	//	float
  },
  /**
   * 获取滚动条的顶端位置
   * @public
   * @member StyleUtil
   * @static
   */
  getScrollTop : function(element){
    return getScrollLength(element,'scrollTop');
  },
  /**
   * 获取滚动条的左端位置
   * @public
   * @member StyleUtil
   * @static
   */
  getScrollLeft : function(element){
    return getScrollLength(element,'scrollLeft');
  },
  /**
   * 获取滚动条高度
   * @public
   * @member StyleUtil
   * @static
   */
  getScrollHeight : function(element){
    return getScrollLength(element,'scrollHeight');
  },
  /**
   * 获取滚动条宽度
   * @public
   * @member StyleUtil
   * @static
   */
  getScrollWidth : function(element){
    return getScrollLength(element,'scrollWidth');
  },
  /**
   * 获取窗口可见区域高度
   * @public
   * @member StyleUtil
   * @static
   */
  getWindowHeight : function ()
  {
    if (window.innerHeight >=0){
      return window.innerHeight;
    }
    if (document.compatMode == 'CSS1Compat'){
      return document.documentElement.clientHeight;
    }
    if (document.body){
      return document.body.clientHeight;
    }
  },
  /**
   * 获取窗口可见区域宽度
   * @public
   * @member StyleUtil
   * @static
   */
  getWindowWidth : function()
  {
    if (window.innerWidth >=0){//!= undefined
      return window.innerWidth; 
    }
    if (document.compatMode == 'CSS1Compat'){
      return document.documentElement.clientWidth; 
    }
    if (document.body){
      return document.body.clientWidth; 
    }
  },
  /**
   * 给指定元素设置class 属性
   * @public
   * @member StyleUtil
   * @static
   */
  setClass : function(node,className){
    node.className = className;
  },
  /**
   * 给指定元素添加class 属性
   * @public
   * @member StyleUtil
   * @static
   */
  addClass : function(node,className){
    this.removeClass(node,className)
    node.className += (" " +className);
  },
  /**
   * 给指定元素移除class 属性
   * @public
   * @member StyleUtil
   * @static
   */
  removeClass : function(node,className){
    var oldName = node.className;
    if(oldName.indexOf(className)>=0){
      if(oldName == className){
        node.className = "";
      }else{
        node.className = oldName.replace(new RegExp('(^|\\s+)'+className+'(\\s+|$)'), " ");
      }
    }
  },
  /**
   * 给指定元素设置style文本属性
   * @public
   * @member StyleUtil
   * @static
   */
  setStyleText : function (node, text) {
	if(typeof node.style.cssText == 'string'){
	 	node.style.cssText = text;
	}else{
		node.setAttribute("style", text);
	}
  },
  /**
   * 给指定元素设置style属性集
   * @public
   * @member StyleUtil
   * @static
   */
  setStyleMap : function (node, valueMap) {
    node = node.style;
    for(var n in valueMap){
      node[n] = valueMap[n];
    }
  },
  /**
   * 设置元素是否可选择
   * @public
   * @member StyleUtil
   * @static
   */
  setSelectable: function(element,selectable){
    // summary: disable selection on a node
    if(BrowserInfo.isGecko()){
      element.style.MozUserSelect = selectable?"":"none";
    }else if(BrowserInfo.isKhtml()){
      element.style.KhtmlUserSelect = selectable?"":"none";
    }else if(BrowserInfo.isIE()){
      if(selectable){
        element.detachEvent("onselectstart",returnFalse);
      }else{
        element.attachEvent("onselectstart",returnFalse);
      }
      //element.unselectable = selectable?"off":"on";
    }else{
      return false;
    }
    return true;
  }
};
/*
 * @internal
 */
function returnFalse(){return false;}
/*
 * @internal
 */
function getScrollLength(element,attribute){
  if(element){
    return parseInt(element[attribute], 10);
  }else{
    return parseInt(document.documentElement[attribute] || document.body[attribute], 10)
  }
}