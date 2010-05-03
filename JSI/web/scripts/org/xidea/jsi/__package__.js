//不能有依赖，否则boot文件太大
this.addScript("optimize.js",["optimizePackage"]);
this.addScript("parse.js",["parse"]);
