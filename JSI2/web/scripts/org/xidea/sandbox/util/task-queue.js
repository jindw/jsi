/*
 * JavaScript Integration Framework
 * License LGPL(您可以在任何地方免费使用,但请不要吝啬您对框架本身的改进)
 * http://www.xidea.org/project/jsi/
 * @author jindw
 * @version $Id: task-queue.js,v 1.2 2008/02/19 13:30:13 jindw Exp $
 */

/**
 * 任务队列
 * @public
 * @constructor
 */
function TaskQueue(){
  this.initialize();
}
TaskQueue.prototype.initialize = function(){
  this.queue = [];
  /**
   * @private 
   * @typeof int
   */
  this.index = 0;
  /**
   *
   * -1:初始状态,0:正常运行,1:挂起状态,2:失败,3:成功
   */
  this.state = -1;
  this.waitTime = 1;
  this.exceptionList = [];
  this.listenersMap = {};
}
TaskQueue.prototype.suspend = function(){
  if(this.state == 0){
    this.state = 1;
    //alert("suspend"+this.state)
  }else{
    throw new Error('error state:'+this.state);
  }
}
TaskQueue.prototype.start = function(){
  if(this.state >= 0){
    return;
  }
  this.state = 0;
  this.fireEvent("start");
  if(this.index<this.queue.length){
    var queue = this;
    this._resume = function(){
      queue.resume();
    }
    this.resume();
  }else{
    this.fireEvent("success");
    this.fireEvent("finish");
  }
}
TaskQueue.prototype.resume = function(){
  //alert(queue.state)
  var _resume = this._resume;
  if(this.state == 1){
    this.state = 0;
    //println(this.index);
    if(_resume && this.checkState()){
      setTimeout(_resume,0);
    }
  }else if(_resume){
    //alert(this.state)
    try{
      this._resume = null;
      this.run();
    }catch(e){
      //$log.error(this,e);
      throw e;
      //this.exceptionList[this.index] = e;
    }finally{
      this.index++;
      //println(this.index);
      if(this.state == 0){
        if(this.checkState()){
          setTimeout(_resume,this.waitTime);
        }
      }
      this._resume = _resume;
    }
  }
};
TaskQueue.prototype.run = function(){
  if(this.state>0){
    //alert(2)
    return;
  }
  var task = this.queue[this.index];
  //alert(task + this.index+"/" + this.queue.length);
  if(task instanceof TaskQueue){
 
    this.suspend();//挂起状态
    task.start();
  }else{// if(task.apply){//for function task
    task.apply(this);
  }
}
/**
 * @return 继续运行
 */
TaskQueue.prototype.checkState = function(){
  if(this.isFinished()){
    if(this.exceptionList.length){
      this.state = 2;
      this.fireEvent("failure");
    }else{
      this.state = 3;
      this.fireEvent("success");
    }
    this.fireEvent("finish");
    if(this.parentTask){
      this.parentTask.resume();
    }
    return false;
  }else{
    this.fireEvent("step");
    return true;
  }
}
TaskQueue.prototype.isFinished = function(){
  return this.index>=this.queue.length;
}
TaskQueue.prototype.isSuccess = function(){
  return this.success;
}
TaskQueue.prototype.isFailure = function(){
  return this.success === false;
}

var eventList = ['start','step','success','failure','finish'];
function buildFunctions(event){
  var event2 = event.substr(0,1).toUpperCase()+event.substr(1)+"Listener";
  TaskQueue.prototype["set"+event2] = function(){
    this.listenersMap[event] = listener?[listener]:null;
  };
  TaskQueue.prototype["add"+event2] = function(listener){
    if(this.listenersMap[event] ){
      this.listenersMap[event].push(listener);
    }else{
      this.listenersMap[event] = [listener];
    }
  };
  TaskQueue.prototype["clear"+event2] = function(){
    this.listenersMap[event] = null;
  };
  TaskQueue.prototype["remove"+event2] = function(listener){
    var list = this.listenersMap[event];
    for(var i = list.length-1;i>=0;i--){
      if(list[i] == listener){
        list.splice(i,1);
      }
    }
   }
}
for(var i = eventList.length-1;i>=0;i--){
  var event = eventList[i];
  buildFunctions(event);
}
TaskQueue.prototype.fireEvent = function(event){
  var list = this.listenersMap[event];
  if(list){
    for(var i=0;i<list.length;i++){
      list[i].apply(this);
    }
  }
}
TaskQueue.prototype.addTask = function(task,index){
  if(task instanceof TaskQueue ){
    task.parentTask = this;
  }else if(!(task instanceof Function)){
    throw new Error("task must be a function or TaskQueue instance, but not:"+task);
  }
  if(index == null){
    this.queue.push(task);
  }else{
    //this.queue.splice(index,0,task);
  }
}
TaskQueue.prototype.addNext = function(task){
  if(this.parentTask){
    var parentQueue = this.parentTask.queue;
    for(var i = parentQueue.length-1;i>=0;i--){
      if(this == parentQueue[i]){
        this.parentTask.addTask(this,i+1);
        break;
      }
    }
  }
}







var emptyFunction = Function.prototype;
var sharedHttpRequest = null;
function getRequestStatus(httpRequest){
  try{
    return httpRequest.status;
  }catch(e){
    getRequestStatus = getRequestStatus2;
    return -1;
  }
}
function getRequestStatus2(httpRequest){
  if(httpRequest.readyState == 4){
    try{
      return httpRequest.status;
    }catch(e){
      return -1;
    }
  }
}
function abortRequest(httpRequest){
  //if IE,do not do abort
  //httpRequest.abort();
  //}else{
  //httpRequest.open("get","about:blank",false);
  //httpRequest.abort();
}
function getResult(httpRequest){
  var status = getRequestStatus(httpRequest);
  if(status >= 200 && status < 300 || status == 304 || !status){
    if(/\/xml/.test(httpRequest.getResponseHeader("Content-Type"))){
      return httpRequest.responseXML;
    }else{
      return httpRequest.responseText;
    }
  }
}
function getXMLHttpRequest(){
  try{
//    if(sharedHttpRequest){
//        sharedHttpRequest.onreadystatechange = emptyFunction;
//    }
    return sharedHttpRequest ||// (alert(1))||
      new XMLHttpRequest();
  }finally{
    sharedHttpRequest = null;
  }
}
function returnXMLHttpRequest(httpRequest){
  httpRequest.onreadystatechange = emptyFunction;
  if(httpRequest.readyState < 4){//404
    abortRequest(httpRequest);
  }
  sharedHttpRequest = httpRequest;
}
function loadRuner(){
  LoadTask.bind(this,this.urlList)();
}




function LoadTask(resource){
  this.initialize();
  if(resource instanceof Array){
    resource = resource.slice(0);
  }else{
    resource = [];
    resource.push.apply(resource,arguments);
  }
  this.urlList = resource;
  this.resultList = [];
  this.addTask(loadRuner);
}
function TaskQueueBase(){
}
TaskQueueBase.prototype = TaskQueue.prototype;
LoadTask.prototype = new TaskQueueBase();
/**
 * 接受结果。
 * @public
 * @param url 当前请求的URL
 * @param httpRequest 当前请求的XMLHttpRequest对象
 */
LoadTask.prototype.accept = function(url,httpRequest){
  if(httpRequest){
    this.resultList.push(getResult(httpRequest));
  }
}

LoadTask.bind = function(task,urls){
  task.suspend();
  //alert(this.queue.length)
  var url;
  var index = 0;
  var httpRequest;
  /*
   * @param success 是否成功执行
   */
  function doLoad(success){
    //if(!httpRequest){alert(11)}
    try{
      if(url){
        var accept = task.accept(url,httpRequest);
      }
    }finally{
      if(httpRequest){
        
        returnXMLHttpRequest(httpRequest);
      }
    }
    if(accept || !success){
      httpRequest = null;
      //alert('resume'+url)
      task.resume();
      task = null;
    }else{
      //alert("begin"+url)
      begin();
    }
  }
  function callback(){
    //if(!httpRequest){alert(11)}
    if(httpRequest.readyState > 2){
      if(httpRequest.readyState == 4 || getRequestStatus(httpRequest) == 404 ){//ie may be exception
        httpRequest.onreadystatechange = emptyFunction;
        doLoad(true);
        //if(httpRequest.abort){//ie bug
        //httpRequest.abort();
      }
    }
  }
  function begin(){
    url= urls[index++];
    //println(url)
    if(url){
      try{
        httpRequest = getXMLHttpRequest();
        httpRequest.onreadystatechange = emptyFunction;//??why?
        //if(!httpRequest){alert(11)}
        httpRequest.open("get",url,true);
        httpRequest.onreadystatechange = callback;
        //for ie 404 will throw exception 
        httpRequest.send(null);
      }catch(e){
        $log.info(url,e);
        doLoad(false);
      }
    }else{
      doLoad(false);
    }
  }
  return begin;
}
/**
 * 脚本装载任务队列。
 * @param <string>path 同$import 函数中的path。
 *
 */
function ScriptLoadTask(path){
  this.initialize();
  this.addTask(function(){
    scriptLoader(this,path);
  });
}
ScriptLoadTask.prototype = new TaskQueueBase();
/**
 * 生成一个新的装载函数，绑定装载路径为：path
 * @public
 * @param <string>path 同$import 函数中的path。
 */
ScriptLoadTask.bind = function(path){
  return function(){
    loadScript(this,path);
  }
}
function loadScript(taskQueue,path){
  taskQueue.suspend();
  $import(path,function(){
    taskQueue.resume();
  },{})
}
