/*
 * 杂项，任务队列，IO，编码...
 */
this.addScript('cookie.js','Cookie');

this.addScript("tween.js",'Tween');

this.addScript("tween-rule.js",'TweenRuleMap');
this.addScript("log.js",'log');


//HTML DOM 处理相关

this.addScript("browser-info.js",'BrowserInfo');
this.addScript("event-util.js",'EventUtil',
               "BrowserInfo");
this.addScript("style-util.js",'StyleUtil',
               "BrowserInfo");


this.addScript("task-queue.js",['TaskQueue','LoadTask','ScriptLoadTask']);

this.addScript("chinese-calendar.js",['getLunarString']);
this.addScript("commons.js",'CommonUtil');
this.addScript("collections.js",['Iterator','StackMap']);
