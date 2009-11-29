/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: fn.js,v 1.5 2008/02/24 08:58:15 jindw Exp $
 */

function xmlReplacer(c){
    switch(c){
        case '<':
          return '&lt;';
        case '>':
          return '&gt;';
        case '&':
          return '&amp;';
        case "'":
          return '&#39;';
        case '"':
          return '&#34;';
    }
}

function loadText(url){
    //$log.info(url);
    var req = new XMLHttpRequest();
    req.open("GET",url,false);
    try{
        //for ie file 404 will throw exception 
        req.send(null);
        if(req.status >= 200 && req.status < 300 || req.status == 304 || !req.status){
            //return  req.responseText;
            return req.responseText;
        }else{
            $log.debug("load faild:",url,"status:",req.status);
        }
    }catch(e){
        $log.debug(e);
    }finally{
        req.abort();
    }
}